# Lockly

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Python](https://img.shields.io/badge/Python-AI-3776AB.svg)](https://www.python.org/)
[![Docker](https://img.shields.io/badge/Docker-Container-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/license-GPL%20v3-blue.svg)](LICENSE)

**Nền tảng mạng xã hội / chat thời gian thực tích hợp AI để hỗ trợ đăng bài, kết bạn, nhắn tin và kiểm duyệt nội dung**

[Giới Thiệu](#-giới-thiệu) • [Tính Năng](#-tính-năng) • [Tổng Quan Hệ Thống](#️-tổng-quan-hệ-thống) • [Cài Đặt](#-cài-đặt) • [Đóng Góp](#-đóng-góp)

</div>

---

## 📖 Giới Thiệu

Lockly là một nền tảng mạng xã hội hiện đại, tập trung vào trải nghiệm tương tác thời gian thực giữa người dùng. Hệ thống cung cấp các chức năng đăng ký, đăng nhập, kết bạn, nhắn tin, đăng bài, chia sẻ ảnh và vị trí, đồng thời tích hợp trí tuệ nhân tạo để phát hiện ảnh nhạy cảm và nhận diện khuôn mặt.

### 🎯 Mục tiêu chính

- Xây dựng nền tảng giao tiếp trực tuyến nhanh và ổn định.
- Cho phép người dùng tạo kết nối xã hội thông qua bạn bè, hội thoại và bài viết.
- Cung cấp trải nghiệm nội dung đa phương tiện với upload ảnh và vị trí.
- Tích hợp AI để tăng tính bảo mật và kiểm duyệt nội dung.
- Hỗ trợ theo dõi hoạt động realtime qua WebSocket, SSE và push notification.

---

## ✨ Tính Năng

### 🔐 Xác thực và tài khoản
- Đăng ký tài khoản bằng OTP qua email.
- Xác thực OTP, đăng nhập, refresh token và đăng xuất.
- Quên mật khẩu và reset mật khẩu.

### 👥 Mạng xã hội và bạn bè
- Gửi lời mời kết bạn.
- Chấp nhận, từ chối và hủy kết bạn.
- Xem danh sách bạn bè và tìm kiếm người dùng.
- Xem bài viết của người bạn.

### 💬 Hội thoại và tin nhắn
- Tạo conversation giữa 2 người dùng.
- Lấy lịch sử tin nhắn.
- Gửi tin nhắn ảnh và cập nhật realtime.

### 📝 Bài viết và media
- Tạo bài viết với ảnh, caption và vị trí.
- Lấy chi tiết bài viết, ảnh bài viết và feed của bạn bè.
- Thả emoji vào bài viết.
- Thay đổi chế độ hiển thị vị trí và xóa bài viết.

### 🧠 AI và nhận diện
- Phát hiện ảnh NSFW bằng mô hình AI.
- Nhận diện khuôn mặt và lưu embedding vào vector database.
- Hỗ trợ kiểm duyệt nội dung và các luồng nhận diện liên quan hồ sơ người dùng.

### 🏅 Thành tựu và hệ thống cup
- Hỗ trợ tính năng thành tựu người dùng như Explorer, Legacy Inheritor, Popular Leader và các cup định kỳ.
- Dùng job định kỳ để refresh điểm/cup cho người dùng theo tháng.

---

## 🏗️ Tổng Quan Hệ Thống

### 🖥️ Back-end

- Spring Boot 3.5: xây dựng REST API và xử lý nghiệp vụ.
- Spring Security + JWT: xác thực và phân quyền.
- Spring Data JPA / Hibernate: truy cập dữ liệu và ORM.
- PostgreSQL: cơ sở dữ liệu chính.
- Redis: cache và lưu token/session.
- RabbitMQ: xử lý tác vụ nền bất đồng bộ.
- MinIO: lưu trữ file ảnh và media.
- Qdrant: lưu trữ embedding khuôn mặt.
- WebSocket / SSE: realtime và cập nhật nhanh.
- Swagger / OpenAPI: tài liệu API.
- Docker Compose: triển khai và vận hành môi trường.

### 🧠 AI Service

- FastAPI: service riêng phục vụ xử lý ảnh.
- InsightFace: phát hiện và trích xuất embedding khuôn mặt.
- Transformers + PyTorch: phân loại ảnh NSFW.
- Model weights được load từ local folder hoặc Hugging Face khi cần.

### 🛡️ Reliability và vận hành

- Resilience4j Circuit Breaker: bảo vệ các lời gọi đến AI service.
- Nginx rate limiting: giới hạn request để tránh overload.
- Health checks: kiểm tra tình trạng database, Redis, MinIO và các dependency.
- GitHub Actions: pipeline CI/CD tự động build, test và deploy.

---

## ⚙️ Cài Đặt

### Yêu cầu

- Java 17 trở lên
- Maven 3.8+
- Docker và Docker Compose
- Python 3.10+ (cho AI service)

### Bước cơ bản

```bash
git clone https://github.com/HIT-Product-2026/Backend.git
cd lockly
cp .env.example .env
mvn clean package
docker compose up -d
```

> Cần cấu hình các biến môi trường như DB, Redis, RabbitMQ, MinIO, JWT và AI service trước khi chạy.

---

## 🤝 Đóng Góp

Chúng tôi hoan nghênh mọi đóng góp từ cộng đồng.

- Báo lỗi và vấn đề gặp phải
- Đề xuất tính năng mới
- Gửi pull request cho các cải tiến

---

## 📫 Liên Hệ

- Email: [dinhkhanh19042006@gmail.com](mailto:dinhkhanh19042006@gmail.com)

---

## 🗺️ Lộ Trình Phát Triển

### ✅ Đã Hoàn Thành

- Xây dựng hệ thống xác thực và phân quyền
- Quản lý bạn bè, hội thoại và tin nhắn
- Quản lý bài viết, ảnh và vị trí
- Tích hợp realtime, notification và monitoring
- Tích hợp AI kiểm duyệt ảnh NSFW và face embedding cơ bản

### 🚧 Đang Phát Triển

- Tính năng thành tựu người dùng và hệ thống cup định kỳ
- Tính năng nhận diện khuôn mặt cho hồ sơ và xác thực
- Tối ưu hóa gợi ý và trải nghiệm cá nhân hóa
- Mở rộng tích hợp AI và các luồng phân tích hành vi người dùng

---

## 📄 License

Dự án này được cấp phép theo giấy phép GPL v3.
