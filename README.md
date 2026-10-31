# 💰 Finance Tracker

Ứng dụng web **quản lý tài chính cá nhân** xây dựng bằng **Spring Boot 4**, **Thymeleaf**, **Bootstrap 5**, **Chart.js** và **H2 Database (lưu file)**.
Theo dõi các khoản thu/chi, phân loại theo danh mục, lọc theo tháng/loại và xem biểu đồ thống kê trực quan.

---

## ✨ Tính năng

| Nhóm | Chức năng |
|------|-----------|
| **Tổng quan (Dashboard)** | Thẻ thống kê: *Tổng số dư* (toàn thời gian), *Thu / Chi trong tháng*, *Chênh lệch* và *Tỷ lệ tiết kiệm*; chuyển tháng nhanh (◀ ▶, chọn tháng) |
| **Biểu đồ** | 🥧 Biểu đồ tròn (doughnut) cơ cấu **chi tiêu / thu nhập theo danh mục**; 📈 Biểu đồ **dòng tiền** (cột Thu – Chi + đường *Số dư lũy kế*) theo **12 tháng** hoặc **theo ngày** trong tháng |
| **Giao dịch** | Thêm, sửa, xóa, xem danh sách; **lọc theo tháng, loại (thu/chi), danh mục, từ khóa**; tự tính tổng thu, tổng chi, số dư theo bộ lọc |
| **Danh mục** | Thêm, sửa, xóa danh mục thu/chi với **icon** (Bootstrap Icons) và **màu** riêng (dùng cho biểu đồ); chặn xóa danh mục đang có giao dịch |
| **Dữ liệu** | H2 lưu file trên ổ cứng → **không mất dữ liệu khi tắt ứng dụng**; tự khởi tạo **14 danh mục mặc định** ở lần chạy đầu |
| **Kiểm tra dữ liệu** | Bean Validation (số tiền > 0, ngày không ở tương lai, mô tả bắt buộc...), danh mục phải đúng loại giao dịch, không trùng tên danh mục |

---

## 🧱 Công nghệ

- Java **21**, Spring Boot **4.1** (Web MVC, Data JPA, Validation, Thymeleaf)
- H2 Database **file mode** + H2 Console
- Lombok
- Bootstrap **5.3**, Bootstrap Icons, Chart.js **4** (nạp qua CDN)
- JUnit 5, MockMvc, AssertJ

---

## 📁 Cấu trúc dự án

```
finance-tracker/
├── pom.xml
├── data/                                   # (tự sinh) file CSDL H2: financedb.mv.db
└── src/
    ├── main/
    │   ├── java/com/huanpham/financetracker/
    │   │   ├── FinanceTrackerApplication.java
    │   │   ├── config/
    │   │   │   ├── DataInitializer.java        # Khởi tạo danh mục mặc định / giao dịch mẫu
    │   │   │   └── WebConfig.java              # Cấu hình MVC bổ sung
    │   │   ├── controller/
    │   │   │   ├── DashboardController.java    # Trang tổng quan + dữ liệu biểu đồ
    │   │   │   ├── TransactionController.java  # CRUD + lọc giao dịch
    │   │   │   ├── CategoryController.java     # CRUD danh mục
    │   │   │   └── GlobalExceptionHandler.java # Trang lỗi 404/400
    │   │   ├── model/
    │   │   │   ├── Category.java               # Entity danh mục
    │   │   │   ├── Transaction.java            # Entity giao dịch
    │   │   │   ├── TransactionType.java        # INCOME / EXPENSE
    │   │   │   └── dto/                        # TransactionForm, TransactionFilter, FinancialSummary, ChartData
    │   │   ├── repository/
    │   │   │   ├── CategoryRepository.java
    │   │   │   ├── TransactionRepository.java
    │   │   │   └── TransactionSpecifications.java # Điều kiện lọc động (JPA Criteria)
    │   │   └── service/
    │   │       ├── CategoryService.java
    │   │       ├── TransactionService.java     # Nghiệp vụ + tính tổng thu/chi/số dư
    │   │       ├── StatisticsService.java      # Dữ liệu biểu đồ
    │   │       ├── BusinessException.java
    │   │       └── ResourceNotFoundException.java
    │   └── resources/
    │       ├── application.properties
    │       ├── static/css/app.css, static/js/app.js
    │       └── templates/
    │           ├── fragments/layout.html       # head, navbar, alerts, footer, scripts
    │           ├── dashboard.html
    │           ├── transactions/list.html, form.html
    │           ├── categories/list.html, form.html
    │           └── error.html, error/custom.html
    └── test/
        ├── java/.../FinanceTrackerApplicationTests.java
        ├── java/.../service/TransactionServiceTests.java
        ├── java/.../controller/WebPagesTests.java
        └── resources/application.properties   # H2 in-memory cho test
```

---

## 🚀 Cài đặt & chạy

### Yêu cầu
- **JDK 21** trở lên (`java -version`)
- Không cần cài Maven — dự án có sẵn Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Kết nối Internet ở lần build đầu (tải thư viện) và khi mở giao diện (Bootstrap/Chart.js qua CDN)

### Chạy ứng dụng

```bash
cd finance-tracker

# Windows (PowerShell / CMD)
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

Mở trình duyệt: **http://localhost:8080**

### Build file JAR

```bash
.\mvnw.cmd clean package          # Windows
./mvnw clean package              # macOS / Linux

java -jar target/finance-tracker-0.0.1-SNAPSHOT.jar
```

### Chạy test

```bash
.\mvnw.cmd test
```

Test dùng H2 **in-memory** (`src/test/resources/application.properties`) nên **không ảnh hưởng** dữ liệu thật trong thư mục `data/`.

---

## 🗄️ Cơ sở dữ liệu (lưu vĩnh viễn)

Cấu hình trong [`application.properties`](src/main/resources/application.properties):

```properties
spring.datasource.url=jdbc:h2:file:./data/financedb;AUTO_SERVER=TRUE
spring.jpa.hibernate.ddl-auto=update
```

- Dữ liệu được ghi vào file `./data/financedb.mv.db` (**tính từ thư mục bạn chạy lệnh**). Hãy luôn chạy ứng dụng từ cùng một thư mục, hoặc đổi sang đường dẫn tuyệt đối, ví dụ `jdbc:h2:file:~/finance-tracker/financedb` (lưu trong thư mục người dùng).
- `ddl-auto=update` chỉ tạo/cập nhật bảng, **không xóa dữ liệu** khi khởi động lại.
- `AUTO_SERVER=TRUE` cho phép mở CSDL bằng công cụ khác (DBeaver, IntelliJ) trong khi ứng dụng đang chạy.
- **Sao lưu**: chỉ cần copy thư mục `data/` (nên tắt ứng dụng trước khi copy).
- **Reset dữ liệu**: tắt ứng dụng và xóa thư mục `data/`. Lần chạy sau sẽ tạo lại danh mục mặc định.

### H2 Console

- URL: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:file:./data/financedb;AUTO_SERVER=TRUE`
- User: `sa` – Password: *(để trống)*

### Dữ liệu mẫu

| Thuộc tính | Mặc định | Ý nghĩa |
|-----------|----------|---------|
| `app.seed.default-categories` | `true` | Tạo 14 danh mục mặc định (9 chi, 5 thu) nếu bảng danh mục trống |
| `app.seed.sample-transactions` | `false` | Tạo giao dịch mẫu cho 6 tháng gần nhất (chỉ khi chưa có giao dịch nào) — bật lên để xem demo biểu đồ |

Danh mục mặc định:
- **Chi tiêu:** Ăn uống, Di chuyển, Nhà ở, Hóa đơn & Tiện ích, Mua sắm, Giải trí, Sức khỏe, Giáo dục, Khác (Chi)
- **Thu nhập:** Lương, Thưởng, Đầu tư, Kinh doanh / Làm thêm, Khác (Thu)

Bật giao dịch mẫu khi chạy (không cần sửa file):

```bash
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--app.seed.sample-transactions=true"
```

---

## 🧭 Hướng dẫn sử dụng

1. **Tổng quan** (`/`): xem số dư, thu/chi tháng hiện tại và biểu đồ. Dùng nút ◀ ▶ hoặc ô chọn tháng để xem tháng khác. Các nút *Chi/Thu* trên biểu đồ tròn và *12 tháng/Theo ngày* trên biểu đồ dòng tiền để chuyển chế độ xem.
2. **Thêm giao dịch**: bấm **Khoản thu** / **Khoản chi** trên thanh menu, hoặc **Thêm giao dịch** tại trang Giao dịch. Chọn loại → danh sách danh mục tự lọc theo loại tương ứng.
3. **Giao dịch** (`/transactions`): lọc theo tháng, loại, danh mục, từ khóa. Ba thẻ phía trên hiển thị tổng thu, tổng chi, số dư **theo bộ lọc hiện tại**. Bấm ✏️ để sửa, 🗑️ để xóa.
4. **Danh mục** (`/categories`): quản lý danh mục thu/chi, chọn icon và màu sắc. Danh mục đang có giao dịch sẽ không thể xóa (và không thể đổi loại).

### Các đường dẫn chính

| Phương thức | URL | Mô tả |
|-------------|-----|-------|
| GET | `/?month=yyyy-MM` | Dashboard theo tháng |
| GET | `/transactions?month=&type=&categoryId=&keyword=` | Danh sách + lọc giao dịch |
| GET | `/transactions/new?type=INCOME\|EXPENSE` | Form thêm giao dịch |
| GET | `/transactions/{id}/edit` | Form sửa giao dịch |
| POST | `/transactions/save` | Lưu (thêm/sửa) giao dịch |
| POST | `/transactions/{id}/delete` | Xóa giao dịch |
| GET | `/categories` | Danh sách danh mục |
| GET | `/categories/new`, `/categories/{id}/edit` | Form thêm/sửa danh mục |
| POST | `/categories/save`, `/categories/{id}/delete` | Lưu / xóa danh mục |
| GET | `/h2-console` | Giao diện quản trị CSDL H2 |

---

## 🛠️ Tùy chỉnh thường gặp

- **Đổi cổng**: `server.port=9090` trong `application.properties`.
- **Chuyển sang MySQL/PostgreSQL**: thêm driver vào `pom.xml` và đổi `spring.datasource.*`; code không cần thay đổi.
- **Tắt H2 Console khi triển khai thật**: `spring.h2.console.enabled=false`.

---

## 📜 Giấy phép

Dự án phục vụ mục đích học tập và sử dụng cá nhân.
