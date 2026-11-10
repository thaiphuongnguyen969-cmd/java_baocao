package com.fashion.backend.repository;

import com.fashion.backend.entity.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Locale;

@Repository
public class JdbcUserRepository implements UserRepository {
    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public User findByEmail(String email) {
        return find(email, false);
    }

    @Override
    public User findByEmailIncludingPassword(String email) {
        return find(email, true);
    }

    private User find(String email, boolean withPassword) {
        try {
            String sql = withPassword
                ? "SELECT id,fullname,email,password,role,status FROM users WHERE LOWER(email)=?"
                : "SELECT id,fullname,email,NULL password,role,status FROM users WHERE LOWER(email)=?";
            Map<String, Object> row = jdbc.queryForMap(sql, email.toLowerCase());
            return new User(((Number) row.get("id")).intValue(), String.valueOf(row.get("fullname")),
                String.valueOf(row.get("email")), row.get("password") == null ? null : String.valueOf(row.get("password")),
                String.valueOf(row.get("role")), ((Number) row.get("status")).intValue());
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public boolean emailExists(String email) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE LOWER(email)=?", Integer.class, email.toLowerCase(Locale.ROOT));
        return count != null && count > 0;
    }

    @Override
    public boolean emailExistsExceptId(String email, int id) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE LOWER(email)=? AND id<>?", Integer.class, email.toLowerCase(Locale.ROOT), id);
        return count != null && count > 0;
    }

    @Override
    public void create(String fullname, String email, String password, String phone, String address, String role, Integer status) {
        jdbc.update(
            "INSERT INTO users(fullname,email,password,phone,address,role,status) VALUES(?,?,?,?,?,?,?)",
            fullname, email, password, phone, address, role, status);
    }

    @Override
    public void update(int id, String fullname, String email, String phone, String address, String role, Integer status) {
        jdbc.update(
            "UPDATE users SET fullname=?,email=?,phone=?,address=?,role=?,status=? WHERE id=?",
            fullname, email, phone, address, role, status, id);
    }

    @Override
    public void updatePassword(int id, String encodedPassword) {
        jdbc.update("UPDATE users SET password=? WHERE id=?", encodedPassword, id);
    }

    @Override
    public void delete(int id) {
        jdbc.update("DELETE FROM users WHERE id=?", id);
    }

    @Override
    public List<Map<String, Object>> findAllAdminRows() {
        return jdbc.queryForList(
            "SELECT id,fullname,email,phone,address,role,status,created_at FROM users ORDER BY id DESC");
    }
}
