package com.fashion.backend.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcCatalogRepository implements CatalogRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public JdbcCatalogRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override public List<Map<String, Object>> categories() {
        return jdbc.queryForList("SELECT * FROM categories ORDER BY id");
    }
    @Override public Map<String, Object> category(int id) {
        return jdbc.queryForMap("SELECT * FROM categories WHERE id=?", id);
    }
    @Override public int createCategory(Map<String, Object> b) {
        return insert("INSERT INTO categories(name,slug,parent_id,description) VALUES(?,?,?,?)",
            b.get("name"), b.get("slug"), b.get("parentId"), b.get("description"));
    }
    @Override public void updateCategory(int id, Map<String, Object> b) {
        jdbc.update("UPDATE categories SET name=?,slug=?,parent_id=?,description=? WHERE id=?",
            b.get("name"), b.get("slug"), b.get("parentId"), b.get("description"), id);
    }
    @Override public void deleteCategory(int id) { jdbc.update("DELETE FROM categories WHERE id=?", id); }

    @Override public List<Map<String, Object>> products(String search, Integer categoryId, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.*,c.name category_name,MIN(v.sale_price) sale_price,MIN(v.price) min_price " +
            "FROM products p LEFT JOIN categories c ON c.id=p.category_id " +
            "LEFT JOIN product_variants v ON v.product_id=p.id WHERE p.is_active=1");
        List<Object> args = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND p.name LIKE ?");
            args.add("%" + search + "%");
        }
        if (categoryId != null) {
            sql.append(" AND p.category_id=?");
            args.add(categoryId);
        }
        sql.append(" GROUP BY p.id ORDER BY p.created_at DESC LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);
        return jdbc.queryForList(sql.toString(), args.toArray());
    }
    @Override public Map<String, Object> product(int id) {
        return jdbc.queryForMap(
            "SELECT p.*,c.name category_name FROM products p JOIN categories c ON c.id=p.category_id WHERE p.id=?", id);
    }
    @Override public List<Map<String, Object>> variantsForProduct(int id) {
        return jdbc.queryForList("SELECT * FROM product_variants WHERE product_id=? ORDER BY price,id", id);
    }
    @Override public List<Map<String, Object>> reviewsForProduct(int id) {
        return jdbc.queryForList(
            "SELECT r.*,u.fullname FROM reviews r JOIN users u ON u.id=r.user_id WHERE r.product_id=? ORDER BY r.created_at DESC", id);
    }
    @Override public List<Map<String, Object>> adminProducts() {
        return jdbc.queryForList(
            "SELECT p.*,c.name category_name,v.sku,v.size,v.color,v.price,v.sale_price,v.stock " +
            "FROM products p JOIN categories c ON c.id=p.category_id LEFT JOIN product_variants v ON v.id=(" +
            "SELECT MIN(v2.id) FROM product_variants v2 WHERE v2.product_id=p.id) ORDER BY p.id DESC");
    }
    @Override public int createProduct(Map<String, Object> b) {
        return insert("INSERT INTO products(category_id,name,slug,description,thumbnail,images,is_active) VALUES(?,?,?,?,?,?,?)",
            b.get("categoryId"), b.get("name"), b.get("slug"), b.get("description"),
            b.get("thumbnail"), jsonValue(b.get("images")), b.get("isActive") == null ? 1 : b.get("isActive"));
    }
    @Override public void updateProduct(int id, Map<String, Object> b) {
        jdbc.update(
            "UPDATE products SET category_id=?,name=?,slug=?,description=?,thumbnail=?,images=?,is_active=COALESCE(?,is_active) WHERE id=?",
            b.get("categoryId"), b.get("name"), b.get("slug"), b.get("description"),
            b.get("thumbnail"), jsonValue(b.get("images")), b.get("isActive"), id);
    }
    @Override public void deleteProduct(int id) { jdbc.update("DELETE FROM products WHERE id=?", id); }
    @Override public List<Map<String, Object>> variants() {
        return jdbc.queryForList(
            "SELECT v.*,p.name product_name FROM product_variants v JOIN products p ON p.id=v.product_id ORDER BY v.id DESC");
    }
    @Override public int createVariant(int productId, Map<String, Object> b) {
        return insert("INSERT INTO product_variants(product_id,sku,size,color,price,sale_price,stock) VALUES(?,?,?,?,?,?,?)",
            productId, b.get("sku"), b.get("size"), b.get("color"),
            b.get("price"), salePrice(b), b.getOrDefault("stock", 0));
    }
    @Override public void updateVariant(int id, int productId, Map<String, Object> b) {
        jdbc.update(
            "UPDATE product_variants SET product_id=?,sku=?,size=?,color=?,price=?,sale_price=?,stock=? WHERE id=?",
            productId, b.get("sku"), b.get("size"), b.get("color"),
            b.get("price"), salePrice(b), b.getOrDefault("stock", 0), id);
    }
    @Override public void deleteVariant(int id) { jdbc.update("DELETE FROM product_variants WHERE id=?", id); }
    @Override public Integer firstVariantId(int productId) {
        return jdbc.queryForObject("SELECT MIN(id) FROM product_variants WHERE product_id=?", Integer.class, productId);
    }

    private int insert(String sql, Object... values) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            return statement;
        }, key);
        Number id = key.getKey();
        if (id == null) throw new IllegalStateException("Database did not return the inserted row id");
        return id.intValue();
    }

    private Object salePrice(Map<String, Object> body) {
        Object value = body.get("salePrice");
        return value != null ? value : body.get("sale_price");
    }

    private Object jsonValue(Object value) {
        if (value == null || value instanceof String) return value;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("images phải là JSON hợp lệ", ex);
        }
    }
}
