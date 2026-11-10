package com.fashion.backend.controller;

import com.fashion.backend.service.CatalogService;
import com.fashion.backend.dto.request.ProductRequest;
import com.fashion.backend.dto.request.VariantRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:8090", "http://127.0.0.1:8090"})
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/api/categories")
    public List<Map<String, Object>> categories() { return catalogService.categories(); }

    @GetMapping("/api/admin/categories/{id}")
    public Map<String, Object> category(@PathVariable int id) { return catalogService.category(id); }

    @PostMapping("/api/admin/categories")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, Object> body) {
        int id = catalogService.createCategory(body);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "Tạo danh mục thành công", "id", id));
    }

    @PutMapping("/api/admin/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable int id, @RequestBody Map<String, Object> body) {
        catalogService.updateCategory(id, body);
        return ResponseEntity.ok(Map.of("message", "Cập nhật danh mục thành công"));
    }

    @DeleteMapping("/api/admin/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable int id) {
        catalogService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa danh mục"));
    }

    @GetMapping("/api/products")
    public List<Map<String, Object>> products(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        return catalogService.products(search, categoryId, page, size);
    }

    @GetMapping("/api/products/{id}")
    public Map<String, Object> product(@PathVariable int id) {
        return catalogService.product(id);
    }

    @GetMapping("/api/admin/products")
    public List<Map<String, Object>> adminProducts() { return catalogService.adminProducts(); }

    @PostMapping("/api/admin/products")
    public ResponseEntity<?> createProduct(@RequestBody ProductRequest request) {
        int id = catalogService.createProduct(request.toMap());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "Tạo sản phẩm thành công", "productId", id));
    }

    @PutMapping("/api/admin/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable int id, @RequestBody ProductRequest request) {
        catalogService.updateProduct(id, request.toMap());
        return ResponseEntity.ok(Map.of("message", "Cập nhật sản phẩm thành công"));
    }

    @DeleteMapping("/api/admin/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        catalogService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa sản phẩm"));
    }

    @GetMapping("/api/admin/variants")
    public List<Map<String, Object>> variants() { return catalogService.variants(); }

    @PostMapping("/api/admin/variants")
    public ResponseEntity<?> createVariant(@RequestBody VariantRequest request) {
        int id = catalogService.createVariant(request.toMap());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "Tạo biến thể thành công", "id", id));
    }

    @PutMapping("/api/admin/variants/{id}")
    public ResponseEntity<?> updateVariant(@PathVariable int id, @RequestBody VariantRequest request) {
        catalogService.updateVariant(id, request.toMap());
        return ResponseEntity.ok(Map.of("message", "Cập nhật biến thể thành công"));
    }

    @DeleteMapping("/api/admin/variants/{id}")
    public ResponseEntity<?> deleteVariant(@PathVariable int id) {
        catalogService.deleteVariant(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa biến thể"));
    }
}
