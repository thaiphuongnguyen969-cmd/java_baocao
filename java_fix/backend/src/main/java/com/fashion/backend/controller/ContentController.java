package com.fashion.backend.controller;

import com.fashion.backend.service.ContentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8090", "http://127.0.0.1:8090"})
public class ContentController {
    private final ContentService service;

    public ContentController(ContentService service) { this.service = service; }

    @GetMapping("/post-categories")
    public List<Map<String,Object>> postCategories() { return service.postCategories(); }

    @GetMapping("/posts")
    public List<Map<String,Object>> posts(@RequestParam(defaultValue="1") int page,
                                          @RequestParam(defaultValue="10") int size) {
        return service.posts(page,size);
    }

    @GetMapping("/posts/{slug}")
    public Map<String,Object> post(@PathVariable String slug) { return service.post(slug); }

    @PostMapping("/admin/posts")
    public ResponseEntity<?> createPost(@RequestBody Map<String,Object> body) {
        int id=service.createPost(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Tạo bài viết thành công","id",id));
    }

    @GetMapping("/admin/posts")
    public List<Map<String,Object>> adminPosts() { return service.adminPosts(); }

    @PutMapping("/admin/posts/{id}")
    public ResponseEntity<?> updatePost(@PathVariable int id,@RequestBody Map<String,Object> body) {
        service.updatePost(id,body);
        return ResponseEntity.ok(Map.of("message","Cập nhật bài viết thành công"));
    }

    @DeleteMapping("/admin/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable int id) {
        service.deletePost(id);
        return ResponseEntity.ok(Map.of("message","Đã xóa bài viết"));
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<?> comment(@PathVariable int id,@RequestBody Map<String,Object> body) {
        service.createComment(id,body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Đã gửi bình luận"));
    }

    @GetMapping("/reviews/{productId}")
    public List<Map<String,Object>> reviews(@PathVariable int productId) { return service.reviews(productId); }

    @PostMapping("/reviews")
    public ResponseEntity<?> review(@RequestBody Map<String,Object> body) {
        service.createReview(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Cảm ơn bạn đã đánh giá"));
    }

    @PostMapping("/admin/reviews")
    public ResponseEntity<?> createReview(@RequestBody Map<String,Object> body) {
        service.createReview(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Đã thêm đánh giá"));
    }

    @GetMapping("/admin/reviews")
    public List<Map<String,Object>> adminReviews() { return service.adminReviews(); }

    @PostMapping("/admin/reviews/{reviewId}/reply")
    public ResponseEntity<?> replyReview(@PathVariable int reviewId,@RequestBody Map<String,Object> body) {
        service.replyReview(reviewId,body);
        return ResponseEntity.ok(Map.of("message","Đã phản hồi đánh giá"));
    }

    @PutMapping("/admin/reviews/{id}")
    public ResponseEntity<?> updateReview(@PathVariable int id,@RequestBody Map<String,Object> body) {
        service.updateReview(id,body);
        return ResponseEntity.ok(Map.of("message","Cập nhật đánh giá thành công"));
    }

    @DeleteMapping("/admin/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable int id) {
        service.deleteReview(id);
        return ResponseEntity.ok(Map.of("message","Đã xóa đánh giá"));
    }

    @GetMapping("/admin/post-categories")
    public List<Map<String,Object>> adminPostCategories() { return service.adminPostCategories(); }

    @PostMapping("/admin/post-categories")
    public ResponseEntity<?> createPostCategory(@RequestBody Map<String,Object> body) {
        int id=service.createPostCategory(body);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message","Tạo danh mục bài viết thành công","id",id));
    }

    @PutMapping("/admin/post-categories/{id}")
    public ResponseEntity<?> updatePostCategory(@PathVariable int id,@RequestBody Map<String,Object> body) {
        service.updatePostCategory(id,body);
        return ResponseEntity.ok(Map.of("message","Cập nhật danh mục bài viết thành công"));
    }

    @DeleteMapping("/admin/post-categories/{id}")
    public ResponseEntity<?> deletePostCategory(@PathVariable int id) {
        service.deletePostCategory(id);
        return ResponseEntity.ok(Map.of("message","Đã xóa danh mục bài viết"));
    }

    @GetMapping("/admin/post-comments")
    public List<Map<String,Object>> adminPostComments() { return service.adminPostComments(); }

    @PostMapping("/admin/post-comments")
    public ResponseEntity<?> createPostComment(@RequestBody Map<String,Object> body) {
        service.createPostComment(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Đã thêm bình luận"));
    }

    @PutMapping("/admin/post-comments/{id}")
    public ResponseEntity<?> updatePostComment(@PathVariable int id,@RequestBody Map<String,Object> body) {
        service.updatePostComment(id,body);
        return ResponseEntity.ok(Map.of("message","Cập nhật bình luận thành công"));
    }

    @DeleteMapping("/admin/post-comments/{id}")
    public ResponseEntity<?> deletePostComment(@PathVariable int id) {
        service.deletePostComment(id);
        return ResponseEntity.ok(Map.of("message","Đã xóa bình luận"));
    }
}
