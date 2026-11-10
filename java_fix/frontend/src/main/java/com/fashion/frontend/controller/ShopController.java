package com.fashion.frontend.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;

@Controller
public class ShopController {
    private final RestTemplate rest; private final String backend;
    public ShopController(RestTemplate rest,@Value("${backend.url}")String backend){this.rest=rest;this.backend=backend;}

    @GetMapping("/")
    public String home(Model m,@RequestParam(required=false)String search,@RequestParam(required=false)Integer categoryId){
        String url=backend+"/api/products?size=8"+(search==null?"":"&search="+search)+(categoryId==null?"":"&categoryId="+categoryId);
        m.addAttribute("products",rest.getForObject(url,List.class));
        m.addAttribute("categories",rest.getForObject(backend+"/api/categories",List.class));
        m.addAttribute("posts",rest.getForObject(backend+"/api/posts?size=3",List.class));
        return "home";
    }
    @GetMapping("/products")
    public String products(Model m,@RequestParam(required=false)String search,@RequestParam(required=false)Integer categoryId,
                           @RequestParam(defaultValue="1")int page){
        String url=backend+"/api/products?page="+page+"&size=12"+(search==null?"":"&search="+search)+(categoryId==null?"":"&categoryId="+categoryId);
        m.addAttribute("products",rest.getForObject(url,List.class));
        m.addAttribute("categories",rest.getForObject(backend+"/api/categories",List.class));
        return "products";
    }
    @GetMapping("/products/{id}")
    public String product(@PathVariable int id,Model m){m.addAttribute("product",rest.getForObject(backend+"/api/products/"+id,Map.class));return "product-detail";}
    @GetMapping("/blog")
    public String blog(Model m){m.addAttribute("posts",rest.getForObject(backend+"/api/posts?size=20",List.class));return "blog";}
    @GetMapping("/blog/{slug}")
    public String post(@PathVariable String slug,Model m){m.addAttribute("post",rest.getForObject(backend+"/api/posts/"+slug,Map.class));return "blog-detail";}
    @GetMapping("/cart")
    public String cart(Model m,HttpSession s){
        String sessionId=(String)s.getAttribute("cartSession"); if(sessionId==null){sessionId="web-"+UUID.randomUUID();s.setAttribute("cartSession",sessionId);}
        m.addAttribute("items",rest.getForObject(backend+"/api/cart/"+sessionId,List.class));m.addAttribute("sessionId",sessionId);return "cart";
    }
    @PostMapping("/cart/add")
    public String addCart(@RequestParam int variantId,@RequestParam(defaultValue="1")int quantity,HttpSession s){
        String sessionId=(String)s.getAttribute("cartSession");if(sessionId==null){sessionId="web-"+UUID.randomUUID();s.setAttribute("cartSession",sessionId);}
        rest.postForObject(backend+"/api/cart/items",Map.of("sessionId",sessionId,"variantId",variantId,"quantity",quantity),Map.class);
        return "redirect:/cart";
    }
    @PostMapping("/cart/remove/{id}")
    public String removeCart(@PathVariable int id){rest.delete(backend+"/api/cart/items/"+id);return "redirect:/cart";}
    @GetMapping("/checkout")
    public String checkout(Model m,HttpSession s){
        String sessionId=(String)s.getAttribute("cartSession");m.addAttribute("items",sessionId==null?List.of():rest.getForObject(backend+"/api/cart/"+sessionId,List.class));return "checkout";
    }
    @PostMapping("/checkout")
    public String checkoutSubmit(@RequestParam Map<String,String>b,HttpSession s,Model m){
        List<Map<String,Object>> cart=sessionId(s)==null?List.of():rest.getForObject(backend+"/api/cart/"+sessionId(s),List.class);
        List<Map<String,Object>> items=new ArrayList<>();
        for(Map<String,Object>row:cart)items.add(Map.of("variantId",row.get("variant_id"),"quantity",row.get("quantity")));
        Map<String,Object>payload=new HashMap<>(b);payload.put("items",items);
        try{Map result=rest.postForObject(backend+"/api/orders/checkout",payload,Map.class);m.addAttribute("result",result);return "order-success";}
        catch(Exception e){m.addAttribute("error","Thanh toán không thành công. Vui lòng kiểm tra giỏ hàng và dữ liệu.");return checkout(m,s);}
    }
    private String sessionId(HttpSession s){return(String)s.getAttribute("cartSession");}
    @GetMapping("/login") public String login(){return "login";}
    @PostMapping("/login")
    public String loginSubmit(@RequestParam String email,@RequestParam String password,HttpSession s,Model m){
        try{Map result=rest.postForObject(backend+"/api/auth/login",Map.of("email",email.trim().toLowerCase(),"password",password),Map.class);
            s.setAttribute("token",result.get("token"));s.setAttribute("user",result.get("user"));
            Object role=((Map<?,?>)result.get("user")).get("role");
            return "admin".equalsIgnoreCase(String.valueOf(role))?"redirect:/admin":"redirect:/";}
        catch(RestClientResponseException e){
            String body=e.getResponseBodyAsString();
            m.addAttribute("error",body.contains("khóa")?"Tài khoản đã bị khóa.":"Email/tài khoản hoặc mật khẩu không đúng.");
            return "login";
        }
        catch(Exception e){m.addAttribute("error","Không thể kết nối máy chủ đăng nhập.");return "login";}
    }
    @GetMapping("/register") public String register(){return "register";}
    @PostMapping("/register")
    public String registerSubmit(@RequestParam Map<String,String>b,Model m){
        b.put("email",b.getOrDefault("email","").trim().toLowerCase());
        if(b.get("fullname")==null||b.get("fullname").trim().isEmpty()){
            b.put("fullname",b.getOrDefault("email","Người dùng mới"));
        }
        String password = b.getOrDefault("password","");
        if(password.length()<6){
            m.addAttribute("error","Mật khẩu phải có ít nhất 6 ký tự.");
            return "register";
        }
        try{
            rest.postForObject(backend+"/api/auth/register",b,Map.class);
            m.addAttribute("success","Đăng ký thành công. Bạn có thể đăng nhập.");
            return "login";
        }catch(HttpClientErrorException e){
            m.addAttribute("error","Email đã tồn tại hoặc dữ liệu không hợp lệ.");
            return "register";
        }catch(Exception e){
            m.addAttribute("error","Không thể kết nối máy chủ đăng ký.");
            return "register";
        }
    }
    @GetMapping("/logout") public String logout(HttpSession s){s.invalidate();return "redirect:/";}
    @GetMapping("/track") public String track(){return "track";}
    @PostMapping("/track") public String track(@RequestParam String code,Model m){
        try{m.addAttribute("order",rest.getForObject(backend+"/api/orders/track/"+code,Map.class));}catch(Exception e){m.addAttribute("error","Không tìm thấy đơn hàng");}return "track";
    }

    private <T>T adminGet(HttpSession s,String path,Class<T> type){HttpEntity<Void>e=adminHeaders(s);try{return rest.exchange(backend+path,HttpMethod.GET,e,type).getBody();}catch(Exception ex){return null;}}
    private HttpEntity<Void> adminHeaders(HttpSession s){HttpHeaders h=new HttpHeaders();h.setBearerAuth(String.valueOf(s.getAttribute("token")));return new HttpEntity<>(h);}
    @GetMapping("/admin")
    public String admin(Model m,HttpSession s){Object u=s.getAttribute("user");if(!isAdmin(u))return "redirect:/login";
        m.addAttribute("dashboard",adminGet(s,"/api/admin/dashboard",Map.class));return "admin";}
    @GetMapping("/admin/{section}")
    public String adminSection(@PathVariable String section,Model m,HttpSession s){Object u=s.getAttribute("user");if(!isAdmin(u))return "redirect:/login";
        Map<String,String> paths=Map.ofEntries(
            Map.entry("products","/api/admin/products"),
            Map.entry("users","/api/admin/users"),
            Map.entry("orders","/api/admin/orders"),
            Map.entry("coupons","/api/admin/coupons"),
            Map.entry("categories","/api/categories"),
            Map.entry("posts","/api/admin/posts"),
            Map.entry("reviews","/api/admin/reviews"),
            Map.entry("variants","/api/admin/variants"),
            Map.entry("post-categories","/api/admin/post-categories"),
            Map.entry("post-comments","/api/admin/post-comments"));
        String path=paths.getOrDefault(section,"/api/admin/dashboard");m.addAttribute("section",section);m.addAttribute("rows",adminGet(s,path,List.class));return "admin-section";}
    private boolean isAdmin(Object user){return user instanceof Map && "admin".equalsIgnoreCase(String.valueOf(((Map<?,?>)user).get("role")));}

    @PostMapping("/admin/{section}/create")
    public String adminCreate(@PathVariable String section, @RequestParam Map<String,String> body, HttpSession s) {
        if (!isAdmin(s.getAttribute("user"))) return "redirect:/login";
        body.remove("_csrf");
        removeEmptyValues(body);
        try { rest.postForObject(backend + adminPath(section, null, false), body, Map.class); }
        catch (Exception ex) { return "redirect:/admin/" + section + "?error=Thao+tac+that+bai"; }
        return "redirect:/admin/" + section + "?success=Da+luu+vao+MySQL";
    }

    @PostMapping("/admin/{section}/update/{id}")
    public String adminUpdate(@PathVariable String section, @PathVariable int id,
                              @RequestParam Map<String,String> body, HttpSession s) {
        if (!isAdmin(s.getAttribute("user"))) return "redirect:/login";
        body.remove("_csrf");
        removeEmptyValues(body);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpMethod method = "orders".equals(section) ? HttpMethod.PATCH : HttpMethod.PUT;
            String path = backend + adminPath(section, id, false);
            if ("orders".equals(section)) path += "/status";
            rest.exchange(path, method,
                new HttpEntity<>(body, headers), Map.class);
        } catch (Exception ex) { return "redirect:/admin/" + section + "?error=Thao+tac+that+bai"; }
        return "redirect:/admin/" + section + "?success=Da+cap+nhat+MySQL";
    }

    @PostMapping("/admin/{section}/delete/{id}")
    public String adminDelete(@PathVariable String section, @PathVariable int id, HttpSession s) {
        if (!isAdmin(s.getAttribute("user"))) return "redirect:/login";
        try { rest.delete(backend + adminPath(section, id, true)); }
        catch (Exception ex) { return "redirect:/admin/" + section + "?error=Khong+the+xoa"; }
        return "redirect:/admin/" + section + "?success=Da+xoa+MySQL";
    }

    private void removeEmptyValues(Map<String,String> body) {
        body.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().trim().isEmpty());
    }

    private String adminPath(String section, Integer id, boolean delete) {
        String base = switch (section) {
            case "products" -> "/api/admin/products";
            case "variants" -> "/api/admin/variants";
            case "categories" -> "/api/admin/categories";
            case "users" -> "/api/admin/users";
            case "orders" -> "/api/admin/orders";
            case "coupons" -> "/api/admin/coupons";
            case "posts" -> "/api/admin/posts";
            case "reviews" -> "/api/admin/reviews";
            case "post-categories" -> "/api/admin/post-categories";
            case "post-comments" -> "/api/admin/post-comments";
            default -> "/api/admin/dashboard";
        };
        if (id == null) return base;
        return base + "/" + id;
    }
}
