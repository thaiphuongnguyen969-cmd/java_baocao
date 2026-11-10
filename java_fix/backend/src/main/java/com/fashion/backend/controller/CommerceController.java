package com.fashion.backend.controller;

import com.fashion.backend.service.CommerceService;
import com.fashion.backend.dto.request.CheckoutRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8090", "http://127.0.0.1:8090"})
public class CommerceController {
    private final CommerceService service;
    public CommerceController(CommerceService service) { this.service=service; }

    @GetMapping("/coupons")
    public List<Map<String,Object>> coupons() { return service.coupons(); }
    @GetMapping("/admin/coupons")
    public List<Map<String,Object>> adminCoupons() { return service.adminCoupons(); }
    @PostMapping("/admin/coupons")
    public ResponseEntity<?> createCoupon(@RequestBody Map<String,Object> body) {
        int id=service.createCoupon(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Tạo mã giảm giá thành công","id",id));
    }
    @PutMapping("/admin/coupons/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable int id,@RequestBody Map<String,Object> body) {
        service.updateCoupon(id,body);
        return ResponseEntity.ok(Map.of("message","Cập nhật mã giảm giá thành công"));
    }
    @DeleteMapping("/admin/coupons/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable int id) {
        service.deleteCoupon(id);
        return ResponseEntity.ok(Map.of("message","Đã xóa mã giảm giá"));
    }

    @PostMapping("/cart/items")
    public ResponseEntity<?> addCart(@RequestBody Map<String,Object> body) {
        return ResponseEntity.ok(service.addCart(body));
    }
    @GetMapping("/cart/{sessionId}")
    public List<Map<String,Object>> cart(@PathVariable String sessionId) { return service.cart(sessionId); }
    @DeleteMapping("/cart/items/{id}")
    public ResponseEntity<?> removeCart(@PathVariable int id) {
        service.removeCart(id);
        return ResponseEntity.ok(Map.of("message","Đã xóa khỏi giỏ hàng"));
    }

    @GetMapping("/admin/orders")
    public List<Map<String,Object>> orders() { return service.orders(); }
    @GetMapping("/admin/orders/{id}")
    public Map<String,Object> order(@PathVariable int id) { return service.order(id); }
    @PatchMapping("/admin/orders/{id}/status")
    public ResponseEntity<?> statusOrder(@PathVariable int id,@RequestBody Map<String,String> body) {
        service.updateOrderStatus(id,body);
        return ResponseEntity.ok(Map.of("message","Cập nhật đơn hàng thành công"));
    }
    @DeleteMapping("/admin/orders/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable int id) {
        service.deleteOrder(id);
        return ResponseEntity.ok(Map.of("message","Đã xóa đơn hàng"));
    }
    @PostMapping("/orders/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.checkout(request.toMap()));
    }
    @GetMapping("/orders/track/{code}")
    public Map<String,Object> track(@PathVariable String code) { return service.track(code); }
}
