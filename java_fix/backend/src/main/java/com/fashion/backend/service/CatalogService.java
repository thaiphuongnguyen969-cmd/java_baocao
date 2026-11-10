package com.fashion.backend.service;

import java.util.List;
import java.util.Map;

public interface CatalogService {
    List<Map<String, Object>> categories();
    Map<String, Object> category(int id);
    int createCategory(Map<String, Object> body);
    void updateCategory(int id, Map<String, Object> body);
    void deleteCategory(int id);
    List<Map<String, Object>> products(String search, Integer categoryId, int page, int size);
    Map<String, Object> product(int id);
    List<Map<String, Object>> adminProducts();
    int createProduct(Map<String, Object> body);
    void updateProduct(int id, Map<String, Object> body);
    void deleteProduct(int id);
    List<Map<String, Object>> variants();
    int createVariant(Map<String, Object> body);
    void updateVariant(int id, Map<String, Object> body);
    void deleteVariant(int id);
}
