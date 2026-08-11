# 📱 Pando

<div align="center">

<img src="app/src/main/ic_launcher-playstore.png" alt="Pando logo" width="120" />

[![Social App](https://img.shields.io/badge/Product-Social%20App-blue.svg)](https://example.com)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

**Pando là nền tảng kết nối bạn bè bằng khoảnh khắc đời thường, tin nhắn realtime và AI hỗ trợ kiểm duyệt nội dung.**

[Giới Thiệu](#-giới-thiệu) • [Tính Năng Chính](#-tính-năng-chính) • [Kiến Trúc](#-kiến-trúc) • [Công Nghệ](#-công-nghệ-sử-dụng) • [Cài Đặt Local](#-cài-đặt-local)

</div>

---

## 📖 Giới Thiệu

Đời sống vật chất được cải thiện nhưng áp lực xã hội, học tập, công việc và sự thiếu kết nối giữa con người vẫn có thể ảnh hưởng đến sức khỏe tinh thần. Pando được xây dựng nhằm khuyến khích người dùng duy trì sự kết nối với bạn bè và người thân thông qua việc chia sẻ những khoảnh khắc đời thường.

### 🧩 Tổng quan sản phẩm

Pando là ứng dụng kết nối bạn bè thân thiết qua việc chia sẻ hình ảnh, khoảnh khắc và tin nhắn trong một không gian riêng tư, đơn giản và dễ dùng. Ứng dụng tập trung vào kết nối hàng ngày, tạo ra chủ đề trò chuyện và tăng sự gắn kết giữa người dùng.

### 🎯 Thông tin sản phẩm

- Tên sản phẩm: Pando
- Loại sản phẩm: Social / Social Connection App
- Mục đích: Tăng cường kết nối giữa người dùng thông qua việc chia sẻ những khoảnh khắc đời thường
- Đối tượng: Bạn bè, gia đình và các mối quan hệ thân thiết

### 🧠 Bài toán sản phẩm giải quyết

Người dùng ngày càng có nhiều phương tiện để giao tiếp nhưng không phải lúc nào cũng duy trì được sự kết nối trong đời sống hàng ngày. Pando tạo ra một không gian chia sẻ đơn giản và riêng tư, nơi người dùng có thể chia sẻ những khoảnh khắc thú vị trong cuộc sống, từ đó tạo ra các chủ đề trò chuyện và tăng sự gắn kết giữa mọi người.

### 👥 Đối tượng sử dụng

- Người dùng muốn chia sẻ hình ảnh với bạn bè thân thiết.
- Nhóm bạn bè, gia đình hoặc các mối quan hệ có nhu cầu cập nhật hoạt động hàng ngày.
- Người dùng yêu thích hình thức chia sẻ hình ảnh nhanh chóng và mang tính riêng tư.

### 📦 Phạm vi sản phẩm

- Đăng ký, đăng nhập và quản lý tài khoản
- Kết nối và quản lý bạn bè
- Chia sẻ hình ảnh và khoảnh khắc
- Nhắn tin
- Gửi thông báo đến người dùng
- Hiển thị nội dung thông qua widget
- Hỗ trợ AI cho một số chức năng liên quan đến hình ảnh/nội dung
- Hệ thống backend triển khai trên môi trường cloud

### ⭐ Điểm nổi bật

- Private connection: tập trung vào bạn bè và người thân thay vì mạng xã hội đại chúng
- Daily moments: khuyến khích chia sẻ những khoảnh khắc đời thường
- Low-friction sharing: chia sẻ nhanh, ít thao tác
- Social interaction: biến hình ảnh thành chủ đề trò chuyện và tương tác

---

## ✨ Tính Năng Chính

### 🔐 Đăng ký và đăng nhập

- Đăng ký tài khoản
- Đăng nhập / đăng xuất
- Xác thực người dùng
- Quản lý thông tin cá nhân

### 👥 Quản lý bạn bè

- Tìm kiếm người dùng
- Gửi lời mời kết bạn
- Chấp nhận hoặc từ chối lời mời kết bạn
- Xem danh sách bạn bè
- Xóa bạn bè

### 📸 Chia sẻ hình ảnh

- Chụp ảnh trực tiếp từ ứng dụng
- Chọn và chỉnh sửa hình ảnh
- Chia sẻ hình ảnh với bạn bè
- Xem lịch sử hình ảnh đã chia sẻ
- Xem lại các khoảnh khắc đã chia sẻ
- Gắn vị trí địa lý vào bài đăng hình ảnh

### 📰 Widget

- Hiển thị hình ảnh và khoảnh khắc mới nhất
- Cập nhật nội dung chia sẻ bởi bạn bè
- Truy cập nhanh nội dung hình ảnh
- Hiển thị các khoảnh khắc nổi bật của người dùng và bạn bè

### 💬 Tương tác với hình ảnh

- Bình luận trên hình ảnh

### 💬 Nhắn tin

- Gửi và nhận tin nhắn giữa bạn bè
- Nhận tin nhắn theo thời gian thực
- Xem lịch sử trò chuyện
- Thông báo khi có tin nhắn mới

### 🗺️ Bản đồ và vị trí

- Hiển thị vị trí của người dùng trên bản đồ
- Gắn vị trí địa lý vào bài đăng
- Xem thông tin địa điểm được gắn trong bài đăng
- Dẫn đường đến vị trí của bài đăng bằng thao tác nhanh

### 🧠 Nhận diện và xử lý hình ảnh bằng AI

- Phát hiện tự động hình ảnh có nội dung không phù hợp (NSFW)
- Kiểm tra và đánh giá mức độ NSFW trước khi chia sẻ
- Phát hiện khuôn mặt xuất hiện trong hình ảnh
- Trích xuất đặc trưng khuôn mặt để nhận diện hoặc đối sánh
- Hỗ trợ xác định bạn bè xuất hiện trong hình ảnh dựa trên dữ liệu khuôn mặt đã đăng ký

### 🔔 Thông báo

- Thông báo khi bạn bè chia sẻ hình ảnh
- Thông báo khi có tin nhắn mới

### 🔐 Quản lý tài khoản

- Cập nhật thông tin cá nhân
- Thay đổi ảnh đại diện
- Thay đổi mật khẩu
- Quản lý quyền riêng tư
- Đăng xuất khỏi tài khoản

---

## 🏗️ Kiến Trúc

Pando được xây dựng theo mô hình backend monolith trên Spring Boot và có thể triển khai với kiến trúc dịch vụ phụ trợ. Hệ thống kết nối Android client, Nginx reverse proxy, backend Spring Boot và các dịch vụ bên ngoài để xử lý dữ liệu, realtime và AI.

### Thành phần chính

- Android Client
- Nginx / Reverse Proxy
- Spring Boot Backend
- PostgreSQL + PostGIS
- Redis
- RabbitMQ
- MinIO
- Qdrant
- Firebase
- AI Services

---

## 💻 Công Nghệ Sử Dụng

| Nhóm | Công nghệ | Mục đích |
| --- | --- | --- |
| Backend | Java, Spring Boot | Xây dựng backend |
| Frontend | Android | Ứng dụng mobile |
| Database | PostgreSQL, PostGIS | Lưu trữ dữ liệu và dữ liệu địa lý |
| ORM | JPA, Hibernate | Mapping object-relational |
| DB Migration | Flyway | Quản lý database migration |
| Cache | Redis | Caching và lưu trữ tạm thời |
| Message Broker | RabbitMQ | Giao tiếp bất đồng bộ và xử lý message |
| File Storage | MinIO | Lưu trữ hình ảnh và file |
| Vector Database | Qdrant | Lưu trữ và tìm kiếm vector embedding |
| API | RESTful API | Giao tiếp giữa ứng dụng và backend |
| Realtime | WebSocket, STOMP | Giao tiếp hai chiều thời gian thực |
| Server Push | SSE | Gửi sự kiện realtime đến client |
| Security | Spring Security, JWT | Authentication và Authorization |
| Resilience | Resilience4j | Circuit breaker |
| Notification | Firebase Admin SDK / FCM | Gửi push notification |
| Email | Spring Mail | Gửi email |
| Validation | Spring Validation | Validate dữ liệu request |
| AI | Hugging Face Transformers, InsightFace | Phát hiện NSFW, nhận diện khuôn mặt |
| Testing | JUnit, Mockito, Spring Test, H2 | Unit test |
| Monitoring | Spring Boot Actuator, Micrometer, Prometheus, Grafana | Thu thập metrics và trực quan hóa |
| API Documentation | SpringDoc OpenAPI / Swagger | Tạo tài liệu API |
| DevOps | Git, GitHub, Docker, CI/CD, AWS | Version control, containerization và triển khai |

---

## 🚀 DevOps & Deployment

- CI/CD: GitHub Actions
- Containerization: Docker
- Cloud: AWS
- Reverse Proxy: Nginx
- Monitoring: Prometheus, Grafana
- SSL/TLS: Let's Encrypt
- Source Control: GitHub
- Container Orchestration: Docker Compose

---

## 🔒 Bảo mật

### Authentication

- JWT Authentication
- Login / Register
- Token validation

### Data Security

- Password hashing
- HTTPS / SSL / TLS
- Validate input

---

## 💻 Cài Đặt Local

### Yêu cầu

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Python 3.10+ (AI service)

### Chạy nhanh

```bash
git clone <repository-url>
cd lockly
cp .env.example .env
./mvnw clean package
docker compose up -d
```

> Cập nhật các biến môi trường DB, Redis, RabbitMQ, MinIO, JWT và AI service trước khi chạy.

---

## 🧪 Kiểm Thử

- Unit test: `./mvnw test`
- Chạy service bằng Docker Compose và kiểm tra endpoints

---

## 🌱 Đang phát triển

- Chức năng thành tựu và Cup dạng social
- Liên kết bảo mật với khuôn mặt đã đăng ký
- Chức năng Pando Gold
- Thêm các tương tác trên bản đồ (ví dụ: thông báo khi 2 người ở gần nhau)
- Mở rộng các tương tác xã hội và cá nhân hóa

> ERD: https://www.drawdb.app/editor?shareId=c1ed60330e3394edb87d7b9d186c2bcb
