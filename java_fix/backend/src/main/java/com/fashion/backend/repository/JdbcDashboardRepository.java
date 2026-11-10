package com.fashion.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class JdbcDashboardRepository implements DashboardRepository {
    private final JdbcTemplate jdbc;
    public JdbcDashboardRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @Override public Map<String,Object> summary() {
        return Map.of(
            "users",jdbc.queryForObject("SELECT COUNT(*) FROM users",Integer.class),
            "products",jdbc.queryForObject("SELECT COUNT(*) FROM products",Integer.class),
            "orders",jdbc.queryForObject("SELECT COUNT(*) FROM orders",Integer.class),
            "revenue",jdbc.queryForObject(
                "SELECT COALESCE(SUM(final_amount),0) FROM orders WHERE payment_status='paid'",Object.class),
            "lowStock",jdbc.queryForList(
                "SELECT p.name,v.sku,v.stock FROM product_variants v JOIN products p ON p.id=v.product_id WHERE v.stock<10 ORDER BY v.stock")
        );
    }
}
