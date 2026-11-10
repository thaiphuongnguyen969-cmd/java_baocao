# FashionShop - Website bán quần áo Java

Dự án gồm backend REST API và frontend web Java bằng Spring Boot.

## Yêu cầu
- Java 25 (latest LTS; required to build and run both applications)
- Apache Maven
- XAMPP (Apache/MySQL)
- MySQL 8 trở lên

## 1. Tạo cơ sở dữ liệu
1. Mở XAMPP, khởi động MySQL.
2. Mở phpMyAdmin tại `http://localhost/phpmyadmin`.
3. Import `database.sql`, sau đó import `seed.sql`.
4. Nếu MySQL có mật khẩu, sửa `backend/src/main/resources/application.properties`.

## 2. Chạy Backend
```powershell
cd backend
mvnw.cmd spring-boot:run
```
Backend chạy tại `http://localhost:8080`.

## 3. Chạy Frontend
```powershell
cd frontend
mvnw.cmd spring-boot:run
```
Website chạy tại `http://localhost:8090`.

## Cấu trúc Backend

Mã nguồn Java nằm trong `backend/src/main/java/com/fashion/backend`:

```text
config/       Cấu hình Spring Security và CORS
controller/   Khai báo REST endpoints, gọi Service và trả response
dto/request/  Đối tượng request như đăng nhập, sản phẩm, biến thể, checkout
dto/response/ Đối tượng response API
entity/       Kiểu domain ánh xạ dữ liệu người dùng
exception/    Exception nghiệp vụ và REST exception handler tập trung
repository/   Interface truy vấn dữ liệu và phần cài đặt JDBC/MySQL
security/     JWT filter và tạo JWT
service/      Interface nghiệp vụ
service/impl/ Cài đặt nghiệp vụ, kiểm tra dữ liệu, quản lý transaction
util/         Tiện ích dùng chung, ví dụ chuẩn hóa tài khoản
```

Backend hiện dùng `JdbcTemplate` thay vì JPA: SQL chỉ nằm trong các repository `Jdbc*Repository`, không đặt trong controller/service. `created_at` và `updated_at` được ghi nhận theo default/trigger trong MySQL; checkout và thao tác sản phẩm gồm nhiều bước được quản lý bằng `@Transactional`. API quản trị `/api/admin/**` yêu cầu JWT có role `admin`; các API đăng nhập/đăng ký và API cửa hàng công khai giữ nguyên đường dẫn.

Kiểm tra backend sau khi thay đổi:

```powershell
cd backend
.\mvnw.cmd clean test
```

## Tài khoản demo
- Admin: `admin@fashion.vn` / `password`
- Khách hàng: `anh.nguyen@gmail.com` / `password`

## API chính
- `GET /api/products`, `GET /api/products/{id}`
- `GET/POST/PUT/DELETE /api/admin/products`
- `GET/POST/PUT/DELETE /api/admin/categories`
- `GET/POST/PUT/DELETE /api/admin/users`
- `GET/POST/PUT/DELETE /api/admin/coupons`
- `GET /api/admin/orders`, `PATCH /api/admin/orders/{id}/status`
- `POST /api/orders/checkout`, `GET /api/orders/track/{code}`
- `POST /api/auth/login`, `POST /api/auth/register`
- `GET /api/posts`, `GET /api/posts/{slug}`
- `GET/POST /api/reviews`, `POST /api/admin/reviews/{id}/reply`

Dữ liệu mẫu có 10 dòng cho từng nhóm chính, các bảng liên kết như giỏ hàng, chi tiết đơn hàng và ảnh đánh giá cũng có dữ liệu kiểm thử.
