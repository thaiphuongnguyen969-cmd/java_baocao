package com.fashion.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcContentRepository implements ContentRepository {
    private final JdbcTemplate jdbc;

    public JdbcContentRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public List<Map<String,Object>> postCategories() {
        return jdbc.queryForList("SELECT * FROM post_categories ORDER BY id");
    }
    @Override public List<Map<String,Object>> posts(int size, int offset) {
        return jdbc.queryForList(
            "SELECT p.*,pc.name category_name,u.fullname author FROM posts p " +
            "JOIN post_categories pc ON pc.id=p.category_id JOIN users u ON u.id=p.user_id " +
            "WHERE p.is_published=1 ORDER BY p.created_at DESC LIMIT ? OFFSET ?", size, offset);
    }
    @Override public Map<String,Object> post(String slug) {
        return jdbc.queryForMap(
            "SELECT p.*,pc.name category_name,u.fullname author FROM posts p " +
            "JOIN post_categories pc ON pc.id=p.category_id JOIN users u ON u.id=p.user_id WHERE p.slug=?", slug);
    }
    @Override public void incrementPostViews(String slug) {
        jdbc.update("UPDATE posts SET views=views+1 WHERE slug=?", slug);
    }
    @Override public List<Map<String,Object>> postComments(int postId) {
        return jdbc.queryForList(
            "SELECT c.*,u.fullname FROM post_comments c JOIN users u ON u.id=c.user_id WHERE c.post_id=? ORDER BY c.created_at DESC", postId);
    }
    @Override public int createPost(Map<String,Object> b) {
        return insert("INSERT INTO posts(category_id,user_id,title,slug,thumbnail,summary,content,is_published) VALUES(?,?,?,?,?,?,?,?)",
            b.get("categoryId"),b.get("userId"),b.get("title"),b.get("slug"),b.get("thumbnail"),
            b.get("summary"),b.get("content"),b.getOrDefault("isPublished",1));
    }
    @Override public List<Map<String,Object>> adminPosts() {
        return jdbc.queryForList(
            "SELECT p.*,pc.name category_name,u.fullname author FROM posts p " +
            "JOIN post_categories pc ON pc.id=p.category_id JOIN users u ON u.id=p.user_id ORDER BY p.id DESC");
    }
    @Override public void updatePost(int id, Map<String,Object> b) {
        jdbc.update("UPDATE posts SET category_id=?,user_id=?,title=?,slug=?,thumbnail=?,summary=?,content=?,is_published=? WHERE id=?",
            b.get("categoryId"),b.get("userId"),b.get("title"),b.get("slug"),b.get("thumbnail"),
            b.get("summary"),b.get("content"),b.get("isPublished"),id);
    }
    @Override public void deletePost(int id) { jdbc.update("DELETE FROM posts WHERE id=?",id); }
    @Override public void createComment(int postId, Map<String,Object> b) {
        jdbc.update("INSERT INTO post_comments(post_id,user_id,parent_id,content) VALUES(?,?,?,?)",
            postId,b.get("userId"),b.get("parentId"),b.get("content"));
    }
    @Override public List<Map<String,Object>> reviews(int productId) {
        return jdbc.queryForList(
            "SELECT r.*,u.fullname FROM reviews r JOIN users u ON u.id=r.user_id WHERE r.product_id=? ORDER BY r.created_at DESC",productId);
    }
    @Override public int createReview(Map<String,Object> b) {
        return insert("INSERT INTO reviews(user_id,product_id,rating,comment) VALUES(?,?,?,?)",
            b.get("userId"),b.get("productId"),b.get("rating"),b.get("comment"));
    }
    @Override public List<Map<String,Object>> adminReviews() {
        return jdbc.queryForList(
            "SELECT r.*,u.fullname,p.name product_name FROM reviews r JOIN users u ON u.id=r.user_id " +
            "JOIN products p ON p.id=r.product_id ORDER BY r.created_at DESC");
    }
    @Override public void replyReview(int id, Map<String,Object> b) {
        jdbc.update("INSERT INTO review_replies(review_id,user_id,comment) VALUES(?,?,?) " +
            "ON DUPLICATE KEY UPDATE comment=?",
            id,b.get("userId"),b.get("comment"),b.get("comment"));
    }
    @Override public void updateReview(int id, Map<String,Object> b) {
        jdbc.update("UPDATE reviews SET user_id=?,product_id=?,rating=?,comment=? WHERE id=?",
            b.get("userId"),b.get("productId"),b.get("rating"),b.get("comment"),id);
    }
    @Override public void deleteReview(int id) { jdbc.update("DELETE FROM reviews WHERE id=?",id); }
    @Override public List<Map<String,Object>> adminPostCategories() {
        return jdbc.queryForList("SELECT * FROM post_categories ORDER BY id DESC");
    }
    @Override public int createPostCategory(Map<String,Object> b) {
        return insert("INSERT INTO post_categories(name,slug,description) VALUES(?,?,?)",
            b.get("name"),b.get("slug"),b.get("description"));
    }
    @Override public void updatePostCategory(int id, Map<String,Object> b) {
        jdbc.update("UPDATE post_categories SET name=?,slug=?,description=? WHERE id=?",
            b.get("name"),b.get("slug"),b.get("description"),id);
    }
    @Override public void deletePostCategory(int id) { jdbc.update("DELETE FROM post_categories WHERE id=?",id); }
    @Override public List<Map<String,Object>> adminPostComments() {
        return jdbc.queryForList(
            "SELECT c.*,u.fullname,p.title post_title FROM post_comments c JOIN users u ON u.id=c.user_id " +
            "JOIN posts p ON p.id=c.post_id ORDER BY c.created_at DESC");
    }
    @Override public int createPostComment(Map<String,Object> b) {
        return insert("INSERT INTO post_comments(post_id,user_id,parent_id,content) VALUES(?,?,?,?)",
            b.get("postId"),b.get("userId"),b.get("parentId"),b.get("content"));
    }
    @Override public void updatePostComment(int id, Map<String,Object> b) {
        jdbc.update("UPDATE post_comments SET content=? WHERE id=?",b.get("content"),id);
    }
    @Override public void deletePostComment(int id) { jdbc.update("DELETE FROM post_comments WHERE id=?",id); }

    private int insert(String sql, Object... values) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i=0;i<values.length;i++) statement.setObject(i+1, values[i]);
            return statement;
        }, key);
        Number id = key.getKey();
        if (id == null) throw new IllegalStateException("Database did not return the inserted row id");
        return id.intValue();
    }
}
