package com.fashion.backend.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcCommerceRepository implements CommerceRepository {
    private final JdbcTemplate jdbc;
    public JdbcCommerceRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    @Override public List<Map<String,Object>> activeCoupons() {
        return jdbc.queryForList("SELECT * FROM coupons WHERE expires_at>NOW() ORDER BY id DESC");
    }
    @Override public List<Map<String,Object>> adminCoupons() {
        return jdbc.queryForList("SELECT * FROM coupons ORDER BY id DESC");
    }
    @Override public int createCoupon(Map<String,Object> b) {
        return insert("INSERT INTO coupons(code,discount_type,discount_value,min_order_value,usage_limit,used_count,expires_at) VALUES(?,?,?,?,?,0,?)",
            b.get("code"),b.get("discountType"),b.get("discountValue"),b.get("minOrderValue"),b.get("usageLimit"),b.get("expiresAt"));
    }
    @Override public void updateCoupon(int id,Map<String,Object> b) {
        jdbc.update("UPDATE coupons SET code=?,discount_type=?,discount_value=?,min_order_value=?,usage_limit=?,expires_at=? WHERE id=?",
            b.get("code"),b.get("discountType"),b.get("discountValue"),b.get("minOrderValue"),b.get("usageLimit"),b.get("expiresAt"),id);
    }
    @Override public void deleteCoupon(int id) { jdbc.update("DELETE FROM coupons WHERE id=?",id); }
    @Override public Integer cartId(String sessionId) {
        try { return jdbc.queryForObject("SELECT id FROM carts WHERE session_id=? LIMIT 1",Integer.class,sessionId); }
        catch (EmptyResultDataAccessException ex) { return null; }
    }
    @Override public int createCart(String sessionId) {
        return insert("INSERT INTO carts(session_id) VALUES(?)",sessionId);
    }
    @Override public boolean cartItemExists(int cartId,int variantId) {
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM cart_items WHERE cart_id=? AND product_variant_id=?",
            Integer.class,cartId,variantId);
        return count!=null&&count>0;
    }
    @Override public void incrementCartItem(int cartId,int variantId,int quantity) {
        jdbc.update("UPDATE cart_items SET quantity=quantity+? WHERE cart_id=? AND product_variant_id=?",quantity,cartId,variantId);
    }
    @Override public void createCartItem(int cartId,int variantId,int quantity) {
        jdbc.update("INSERT INTO cart_items(cart_id,product_variant_id,quantity) VALUES(?,?,?)",cartId,variantId,quantity);
    }
    @Override public List<Map<String,Object>> cart(String sessionId) {
        return jdbc.queryForList(
            "SELECT ci.id,ci.quantity,v.id variant_id,v.size,v.color,v.price,v.sale_price,v.stock,"+
            "p.name product_name,p.thumbnail,p.id product_id FROM carts c "+
            "JOIN cart_items ci ON ci.cart_id=c.id JOIN product_variants v ON v.id=ci.product_variant_id "+
            "JOIN products p ON p.id=v.product_id WHERE c.session_id=?",sessionId);
    }
    @Override public void deleteCartItem(int id) { jdbc.update("DELETE FROM cart_items WHERE id=?",id); }
    @Override public List<Map<String,Object>> orders() {
        return jdbc.queryForList("SELECT * FROM orders ORDER BY created_at DESC");
    }
    @Override public Map<String,Object> order(int id) { return jdbc.queryForMap("SELECT * FROM orders WHERE id=?",id); }
    @Override public List<Map<String,Object>> orderItems(int id) {
        return jdbc.queryForList("SELECT * FROM order_items WHERE order_id=?",id);
    }
    @Override public void updateOrderStatus(int id,String orderStatus,String paymentStatus) {
        jdbc.update("UPDATE orders SET order_status=?,payment_status=? WHERE id=?",orderStatus,paymentStatus,id);
    }
    @Override public void deleteOrder(int id) { jdbc.update("DELETE FROM orders WHERE id=?",id); }
    @Override public Map<String,Object> variantForCheckout(int id) {
        return jdbc.queryForMap(
            "SELECT v.*,p.name product_name FROM product_variants v JOIN products p ON p.id=v.product_id WHERE v.id=? FOR UPDATE",id);
    }
    @Override public Map<String,Object> validCoupon(String code) {
        List<Map<String,Object>> rows=jdbc.queryForList(
            "SELECT * FROM coupons WHERE code=? AND expires_at>NOW() AND (usage_limit IS NULL OR used_count<usage_limit)",code);
        return rows.isEmpty()?null:rows.get(0);
    }
    @Override public void incrementCouponUse(int id) {
        jdbc.update("UPDATE coupons SET used_count=used_count+1 WHERE id=?",id);
    }
    @Override public int createOrder(Map<String,Object> b,String code,BigDecimal total,BigDecimal discount,
                                     BigDecimal shipping,BigDecimal finalAmount) {
        return insert("INSERT INTO orders(user_id,order_code,receiver_name,receiver_phone,receiver_address,total_amount,"+
            "discount_amount,shipping_fee,final_amount,payment_method,payment_status,order_status,note) "+
            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",b.get("userId"),code,b.get("receiverName"),b.get("receiverPhone"),
            b.get("receiverAddress"),total,discount,shipping,finalAmount,b.getOrDefault("paymentMethod","COD"),
            "pending","pending",b.get("note"));
    }
    @Override public void createOrderItem(int orderId,Map<String,Object> v,BigDecimal unitPrice,int quantity) {
        jdbc.update("INSERT INTO order_items(order_id,product_variant_id,product_name,size,color,price,quantity) VALUES(?,?,?,?,?,?,?)",
            orderId,v.get("id"),v.get("product_name"),v.get("size"),v.get("color"),unitPrice,quantity);
    }
    @Override public void decreaseStock(int variantId,int quantity) {
        jdbc.update("UPDATE product_variants SET stock=stock-? WHERE id=? AND stock>=?",quantity,variantId,quantity);
    }
    @Override public Map<String,Object> orderByCode(String code) {
        return jdbc.queryForMap("SELECT * FROM orders WHERE order_code=?",code);
    }
    private int insert(String sql,Object... values) {
        KeyHolder key=new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement=connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            for(int i=0;i<values.length;i++) statement.setObject(i+1,values[i]);
            return statement;
        },key);
        Number id=key.getKey();
        if(id==null) throw new IllegalStateException("Database did not return the inserted row id");
        return id.intValue();
    }
}
