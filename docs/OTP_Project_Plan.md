# OTP Web Demo – Project Plan

> **Chủ đề:** Tìm hiểu về mật khẩu sử dụng một lần OTP  
> **Mục tiêu:** Xây dựng website demo có đăng ký tài khoản, xác thực tài khoản bằng OTP qua Email/SĐT/Zalo, đăng nhập bình thường sau khi đã xác thực và dùng OTP khi quên mật khẩu.
>
> **Scope ưu tiên:** Luồng OTP là trọng tâm. Sau khi đăng nhập thành công chỉ cần một màn hình đơn giản.

---

## 1. Mục tiêu hệ thống

Website cần demo được đầy đủ các đặc tính quan trọng của OTP:

1. Sinh OTP an toàn.
2. Gửi OTP cho người dùng.
3. Kiểm tra thời gian hết hạn.
4. OTP chỉ được sử dụng một lần.
5. Gửi lại OTP có kiểm soát.
6. OTP cũ bị vô hiệu khi resend.
7. Giới hạn số lần nhập OTP sai.
8. Dùng OTP trong hai nghiệp vụ:
   - Xác thực tài khoản sau đăng ký.
   - Quên mật khẩu / đặt lại mật khẩu.

### 1.1. Luồng đăng ký

```text
Đăng ký
   ↓
Tạo User với trạng thái UNVERIFIED
   ↓
Chọn phương thức xác thực
   ├── EMAIL
   ├── PHONE
   └── ZALO
   ↓
Sinh OTP
   ↓
Gửi OTP
   ↓
Nhập OTP
   ↓
Kiểm tra OTP
   ├── Đúng + còn hạn + chưa dùng → VERIFIED
   └── Sai / hết hạn / đã dùng → REJECT
   ↓
User chuyển sang ACTIVE
   ↓
Có thể đăng nhập
```

### 1.2. Luồng đăng nhập

```text
Username + Password
        ↓
Kiểm tra tài khoản
        ↓
status == ACTIVE ?
   ├── YES → Login thành công
   └── NO  → Yêu cầu xác thực tài khoản
```

> Sau khi tài khoản đã được xác thực, các lần đăng nhập sau **không cần OTP**.

### 1.3. Luồng quên mật khẩu

```text
Quên mật khẩu
      ↓
Nhập username/email/SĐT
      ↓
Chọn phương thức nhận OTP
      ↓
Sinh + gửi OTP
      ↓
Verify OTP
      ↓
Backend trả resetToken ngắn hạn
      ↓
Nhập mật khẩu mới
      ↓
Reset password
      ↓
Đăng nhập bình thường
```

---

# 2. Công nghệ đề xuất

## Backend

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Security
- Spring Mail
- MySQL
- Flyway
- Lombok

## Frontend

Có thể chọn một trong hai:

- React + Vite
- Hoặc HTML/CSS/JavaScript thuần nếu muốn làm nhanh

Khuyến nghị: **React + Vite** nếu nhóm đã quen.

## Email

Giai đoạn đầu:

- SMTP
- Gmail App Password hoặc SMTP provider khác

## Phone / Zalo

Thiết kế sẵn interface để mở rộng:

```text
OtpSender
├── EmailOtpSender
├── SmsOtpSender
└── ZaloOtpSender
```

Giai đoạn 1 chỉ bắt buộc `EmailOtpSender` hoạt động thật.

---

# 3. Phạm vi Giai đoạn 1

## Bắt buộc

- [ ] Khởi tạo Spring Boot.
- [ ] Kết nối MySQL.
- [ ] Tạo bảng `users`.
- [ ] Tạo bảng `otp_log`.
- [ ] Đăng ký tài khoản.
- [ ] Hash password bằng BCrypt.
- [ ] Sinh OTP 6 số bằng `SecureRandom`.
- [ ] Lưu OTP dạng hash/HMAC, không lưu plaintext.
- [ ] Gửi OTP qua email.
- [ ] Verify OTP.
- [ ] OTP hết hạn sau 5 phút.
- [ ] OTP chỉ được dùng 1 lần.
- [ ] Resend OTP.
- [ ] Resend cooldown 60 giây.
- [ ] Resend làm OTP cũ mất hiệu lực.
- [ ] Giới hạn tối đa 5 lần nhập sai.
- [ ] Xác thực thành công chuyển User `UNVERIFIED → ACTIVE`.
- [ ] Login username/password.
- [ ] Forgot password bằng OTP.
- [ ] Reset password.
- [ ] Test bằng Postman.
- [ ] Push code chạy được lên `main`.

## Không cần làm ở Giai đoạn 1

- Role/Permission phức tạp.
- Admin dashboard.
- OAuth2.
- Keycloak.
- Google Login.
- Authenticator App/TOTP.
- Redis.
- Microservice.
- Kubernetes.

---

# 4. Thiết kế màn hình

## 4.1. Trang đăng ký

Route:

```text
/register
```

Wireframe:

```text
┌──────────────────────────────────────┐
│              ĐĂNG KÝ                │
│                                      │
│ Họ tên                               │
│ [ Nguyễn Văn A____________________ ] │
│                                      │
│ Username                             │
│ [ nguyenvana______________________ ] │
│                                      │
│ Email                                │
│ [ example@gmail.com_______________ ] │
│                                      │
│ Số điện thoại                        │
│ [ 0912345678______________________ ] │
│                                      │
│ Mật khẩu                             │
│ [ •••••••••••••__________________ ] │
│                                      │
│ Nhập lại mật khẩu                    │
│ [ •••••••••••••__________________ ] │
│                                      │
│              [ Đăng ký ]             │
│                                      │
│ Đã có tài khoản? Đăng nhập           │
└──────────────────────────────────────┘
```

### Validation FE

- Username: 4–50 ký tự.
- Password: tối thiểu 8 ký tự.
- Email đúng format.
- Phone đúng format cơ bản.
- Confirm password phải giống password.

### Sau khi đăng ký

Chuyển sang:

```text
/verify-method
```

---

## 4.2. Chọn phương thức xác thực

Route:

```text
/verify-method
```

```text
┌────────────────────────────────────┐
│        XÁC THỰC TÀI KHOẢN          │
│                                    │
│ Chọn phương thức nhận OTP          │
│                                    │
│ ┌────────────────────────────────┐ │
│ │ 📧 Email                      │ │
│ │ nguy***@gmail.com             │ │
│ └────────────────────────────────┘ │
│                                    │
│ ┌────────────────────────────────┐ │
│ │ 📱 Số điện thoại / Zalo       │ │
│ │ ******5678                    │ │
│ └────────────────────────────────┘ │
└────────────────────────────────────┘
```

Giai đoạn đầu:

- Email hoạt động thật.
- Phone/Zalo có thể để `Coming soon` nếu chưa tích hợp provider.

---

## 4.3. Nhập OTP

Route:

```text
/verify-otp
```

```text
┌──────────────────────────────────────┐
│           XÁC THỰC OTP              │
│                                      │
│ Mã OTP đã được gửi tới               │
│ nguy***@gmail.com                    │
│                                      │
│       [ 5 ][ 8 ][ 3 ][ 9 ][ 2 ][ 1 ]│
│                                      │
│ OTP hết hạn sau: 04:32               │
│                                      │
│          [ Xác nhận OTP ]            │
│                                      │
│ Gửi lại mã sau 52 giây               │
│                                      │
│ Đã nhập sai 1/5 lần                  │
└──────────────────────────────────────┘
```

### FE cần hiển thị

- Countdown 5 phút.
- Countdown resend 60 giây.
- Số lần nhập sai nếu backend trả về.
- Button resend bị disable trong cooldown.

> Countdown frontend chỉ để hiển thị. Backend vẫn là nơi quyết định OTP có hết hạn hay không.

---

## 4.4. Trang đăng nhập

Route:

```text
/login
```

```text
┌────────────────────────────────────┐
│             ĐĂNG NHẬP             │
│                                    │
│ Username                           │
│ [ ______________________________ ] │
│                                    │
│ Password                           │
│ [ ______________________________ ] │
│                                    │
│          [ Đăng nhập ]             │
│                                    │
│           Quên mật khẩu?           │
└────────────────────────────────────┘
```

Nếu account chưa verify:

```text
Tài khoản chưa được xác thực.

[Xác thực ngay]
```

---

## 4.5. Quên mật khẩu

Route:

```text
/forgot-password
```

```text
┌────────────────────────────────────┐
│          QUÊN MẬT KHẨU            │
│                                    │
│ Username / Email / SĐT             │
│ [ ______________________________ ] │
│                                    │
│            [ Tiếp tục ]            │
└────────────────────────────────────┘
```

Sau đó chọn channel giống màn `/verify-method`.

---

## 4.6. Đặt mật khẩu mới

Route:

```text
/reset-password
```

```text
┌────────────────────────────────────┐
│        ĐẶT MẬT KHẨU MỚI           │
│                                    │
│ Mật khẩu mới                       │
│ [ ______________________________ ] │
│                                    │
│ Nhập lại mật khẩu                  │
│ [ ______________________________ ] │
│                                    │
│        [ Đổi mật khẩu ]            │
└────────────────────────────────────┘
```

---

## 4.7. Màn hình sau đăng nhập

Route:

```text
/
```

Không cần làm đẹp.

```text
┌────────────────────────────────────┐
│                                    │
│      ĐĂNG NHẬP THÀNH CÔNG         │
│                                    │
│      Xin chào Nguyễn Văn A        │
│                                    │
│             [ Logout ]             │
└────────────────────────────────────┘
```

---

# 5. Thiết kế Database

## 5.1. Bảng `users`

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    full_name VARCHAR(100),

    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20) UNIQUE,

    status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);
```

### `status`

```text
UNVERIFIED
ACTIVE
LOCKED
```

Ý nghĩa:

| Status | Ý nghĩa |
|---|---|
| `UNVERIFIED` | Đã đăng ký nhưng chưa verify OTP |
| `ACTIVE` | Đã xác thực, được phép login |
| `LOCKED` | Tài khoản bị khóa |

---

# 5.2. Bảng `otp_log`

```sql
CREATE TABLE otp_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,

    otp_hash VARCHAR(255) NOT NULL,

    purpose VARCHAR(30) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    attempt_count INT NOT NULL DEFAULT 0,

    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    invalidated_at TIMESTAMP NULL,

    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## `purpose`

```text
VERIFY_ACCOUNT
RESET_PASSWORD
```

## `channel`

```text
EMAIL
PHONE
ZALO
```

## `status`

```text
PENDING
VERIFIED
EXPIRED
INVALIDATED
BLOCKED
```

Ý nghĩa:

| Status | Ý nghĩa |
|---|---|
| `PENDING` | OTP đang chờ xác thực |
| `VERIFIED` | OTP đã xác thực thành công |
| `EXPIRED` | OTP đã hết hạn |
| `INVALIDATED` | OTP bị vô hiệu do resend hoặc logic khác |
| `BLOCKED` | OTP bị khóa vì nhập sai quá nhiều |

---

# 5.3. Index đề xuất

```sql
CREATE INDEX idx_otp_user_purpose_status
ON otp_log(user_id, purpose, status);

CREATE INDEX idx_otp_created_at
ON otp_log(created_at);

CREATE INDEX idx_otp_expires_at
ON otp_log(expires_at);
```

---

# 5.4. Quan hệ

```text
users
  1
  │
  │
  N
otp_log
```

Một user có thể có nhiều OTP theo thời gian.

Ví dụ:

```text
User #10
├── OTP #100 VERIFY_ACCOUNT EMAIL VERIFIED
├── OTP #101 RESET_PASSWORD EMAIL EXPIRED
└── OTP #102 RESET_PASSWORD EMAIL VERIFIED
```

---

# 6. Quy tắc OTP

## 6.1. Sinh OTP

Không dùng:

```java
Random
Math.random()
```

Dùng:

```java
SecureRandom
```

Ví dụ:

```java
private static final SecureRandom SECURE_RANDOM = new SecureRandom();

public String generateOtp() {
    int value = SECURE_RANDOM.nextInt(1_000_000);
    return String.format("%06d", value);
}
```

OTP có thể có dạng:

```text
000042
583921
927104
```

---

# 6.2. Không lưu OTP plaintext

Không lưu:

```text
otp = 583921
```

Khuyến nghị:

```text
HMAC-SHA256(serverSecret, otp)
```

DB lưu:

```text
otp_hash = <HMAC value>
```

`serverSecret` phải đặt trong biến môi trường, không commit Git.

Lý do dùng HMAC thay vì SHA-256 thuần:

OTP chỉ có 1.000.000 khả năng nên nếu DB bị lộ, SHA-256 thuần có thể bị brute-force rất nhanh. HMAC kèm secret phía server giảm rủi ro khi chỉ DB bị lộ.

---

# 6.3. Thời gian hết hạn

```text
OTP_TTL = 5 phút
```

Backend verify:

```text
now >= expiresAt
→ OTP_EXPIRED
```

Frontend countdown không quyết định tính hợp lệ.

---

# 6.4. Single-use

OTP sau khi verify thành công:

```text
PENDING → VERIFIED
```

Nếu gửi lại OTP đó:

```text
OTP_ALREADY_USED
```

---

# 6.5. Giới hạn attempt

```text
MAX_ATTEMPTS = 5
```

OTP sai:

```text
attempt_count += 1
```

Nếu:

```text
attempt_count >= 5
```

thì:

```text
status = BLOCKED
```

---

# 6.6. Resend OTP

Quy tắc:

```text
RESEND_COOLDOWN = 60 giây
```

Khi resend:

1. Kiểm tra cooldown.
2. Tìm OTP `PENDING` cũ cùng `user + purpose`.
3. Chuyển OTP cũ thành `INVALIDATED`.
4. Sinh OTP mới.
5. Tạo bản ghi `otp_log` mới.
6. Gửi OTP mới.

OTP cũ không được phép verify nữa.

---

# 6.7. Giới hạn spam gửi OTP

Đề xuất:

```text
MAX_SEND_PER_10_MINUTES = 5
```

Nếu vượt quá:

```text
OTP_TOO_MANY_REQUESTS
```

HTTP:

```text
429 Too Many Requests
```

---

# 7. Thiết kế Endpoint

Base URL:

```text
/api/v1
```

---

## 7.1. Register

```http
POST /api/v1/auth/register
```

Request:

```json
{
  "username": "luc123",
  "password": "Password@123",
  "fullName": "Nguyen Van Luc",
  "email": "luc@gmail.com",
  "phone": "0912345678"
}
```

Response:

```json
{
  "timestamp": "2026-09-20T17:00:00+07:00",
  "code": "SUCCESS",
  "message": "Đăng ký thành công",
  "data": {
    "userId": 15,
    "status": "UNVERIFIED",
    "email": "lu***@gmail.com",
    "phone": "******5678"
  },
  "traceId": "..."
}
```

HTTP:

```text
201 Created
```

---

## 7.2. Send OTP

```http
POST /api/v1/otp/send
```

Request:

```json
{
  "userId": 15,
  "purpose": "VERIFY_ACCOUNT",
  "channel": "EMAIL"
}
```

Response:

```json
{
  "code": "SUCCESS",
  "message": "OTP đã được gửi",
  "data": {
    "otpId": 120,
    "channel": "EMAIL",
    "destination": "lu***@gmail.com",
    "expiresIn": 300,
    "resendAfter": 60
  }
}
```

---

## 7.3. Verify OTP

```http
POST /api/v1/otp/verify
```

Request:

```json
{
  "otpId": 120,
  "otp": "583921"
}
```

### VERIFY_ACCOUNT thành công

```json
{
  "code": "SUCCESS",
  "message": "Xác thực tài khoản thành công",
  "data": {
    "verified": true,
    "purpose": "VERIFY_ACCOUNT"
  }
}
```

### RESET_PASSWORD thành công

```json
{
  "code": "SUCCESS",
  "message": "Xác thực OTP thành công",
  "data": {
    "verified": true,
    "purpose": "RESET_PASSWORD",
    "resetToken": "<short-lived-token>"
  }
}
```

---

## 7.4. Resend OTP

```http
POST /api/v1/otp/resend
```

Request:

```json
{
  "otpId": 120
}
```

Response:

```json
{
  "code": "SUCCESS",
  "message": "OTP mới đã được gửi",
  "data": {
    "otpId": 121,
    "expiresIn": 300,
    "resendAfter": 60
  }
}
```

---

## 7.5. Login

```http
POST /api/v1/auth/login
```

Request:

```json
{
  "username": "luc123",
  "password": "Password@123"
}
```

Response:

```json
{
  "code": "SUCCESS",
  "message": "Đăng nhập thành công",
  "data": {
    "userId": 15,
    "username": "luc123",
    "fullName": "Nguyen Van Luc"
  }
}
```

> Demo đơn giản có thể chưa cần JWT nếu chỉ cần chuyển sang màn success. Nếu dùng session/JWT thì bổ sung sau.

---

## 7.6. Forgot Password

```http
POST /api/v1/auth/forgot-password
```

Request:

```json
{
  "identifier": "luc123"
}
```

Có thể cho phép:

```text
username
email
phone
```

Response:

```json
{
  "code": "SUCCESS",
  "message": "Thông tin khôi phục đã được xử lý",
  "data": {
    "userId": 15,
    "availableChannels": [
      {
        "channel": "EMAIL",
        "destination": "lu***@gmail.com"
      },
      {
        "channel": "PHONE",
        "destination": "******5678"
      }
    ]
  }
}
```

> Khi triển khai production cần hạn chế account enumeration. Với demo môn học, có thể giữ flow này cho dễ trình diễn nhưng nên nêu rõ đây là điểm cần hardening nếu triển khai thật.

Sau đó frontend gọi:

```text
POST /otp/send
purpose = RESET_PASSWORD
```

---

## 7.7. Reset Password

```http
POST /api/v1/auth/reset-password
```

Request:

```json
{
  "resetToken": "<token>",
  "newPassword": "NewPassword@123",
  "confirmPassword": "NewPassword@123"
}
```

Response:

```json
{
  "code": "SUCCESS",
  "message": "Đổi mật khẩu thành công",
  "data": null
}
```

`resetToken`:

- Sinh sau khi verify OTP `RESET_PASSWORD`.
- Có hiệu lực khoảng 5 phút.
- Phải chứa hoặc liên kết với:
  - userId
  - otpLogId
  - purpose
  - expiration
- Không cho gọi `/reset-password` nếu chưa verify OTP.

---

# 8. DTO Design

## 8.1. `RegisterRequest`

```java
public class RegisterRequest {
    String username;
    String password;
    String fullName;
    String email;
    String phone;
}
```

Validation:

```text
username: @NotBlank, 4–50
password: @NotBlank, >= 8
email: @Email
phone: regex cơ bản
```

---

## 8.2. `RegisterResponse`

```java
public class RegisterResponse {
    Long userId;
    String status;
    String email;
    String phone;
}
```

Email/phone phải trả dạng masked.

---

## 8.3. `SendOtpRequest`

```java
public class SendOtpRequest {
    Long userId;
    OtpPurpose purpose;
    OtpChannel channel;
}
```

---

## 8.4. `SendOtpResponse`

```java
public class SendOtpResponse {
    Long otpId;
    OtpChannel channel;
    String destination;
    Long expiresIn;
    Long resendAfter;
}
```

---

## 8.5. `VerifyOtpRequest`

```java
public class VerifyOtpRequest {
    Long otpId;
    String otp;
}
```

Validation:

```text
otp: đúng 6 chữ số
```

Regex:

```text
^\d{6}$
```

---

## 8.6. `VerifyOtpResponse`

```java
public class VerifyOtpResponse {
    Boolean verified;
    OtpPurpose purpose;
    String resetToken;
    Integer remainingAttempts;
}
```

`resetToken` chỉ trả khi:

```text
purpose = RESET_PASSWORD
```

---

## 8.7. `LoginRequest`

```java
public class LoginRequest {
    String username;
    String password;
}
```

---

## 8.8. `LoginResponse`

```java
public class LoginResponse {
    Long userId;
    String username;
    String fullName;
}
```

---

## 8.9. `ForgotPasswordRequest`

```java
public class ForgotPasswordRequest {
    String identifier;
}
```

---

## 8.10. `ForgotPasswordResponse`

```java
public class ForgotPasswordResponse {
    Long userId;
    List<RecoveryChannelDto> availableChannels;
}
```

---

## 8.11. `RecoveryChannelDto`

```java
public class RecoveryChannelDto {
    OtpChannel channel;
    String destination;
}
```

---

## 8.12. `ResetPasswordRequest`

```java
public class ResetPasswordRequest {
    String resetToken;
    String newPassword;
    String confirmPassword;
}
```

---

# 9. Enum

## `UserStatus`

```java
UNVERIFIED,
ACTIVE,
LOCKED
```

## `OtpPurpose`

```java
VERIFY_ACCOUNT,
RESET_PASSWORD
```

## `OtpChannel`

```java
EMAIL,
PHONE,
ZALO
```

## `OtpStatus`

```java
PENDING,
VERIFIED,
EXPIRED,
INVALIDATED,
BLOCKED
```

---

# 10. Response Format chung

Tất cả API dùng format:

```json
{
  "timestamp": "2026-09-20T17:00:00+07:00",
  "code": "SUCCESS",
  "message": "Thành công",
  "data": {},
  "traceId": "..."
}
```

## Error response

```json
{
  "timestamp": "2026-09-20T17:00:00+07:00",
  "code": "OTP-4002",
  "message": "Mã OTP đã hết hạn",
  "data": null,
  "traceId": "..."
}
```

Không trả stack trace cho client.

---

# 11. Quy ước HTTP Status

| HTTP | Khi nào dùng |
|---|---|
| `200 OK` | Thành công |
| `201 Created` | Register thành công |
| `400 Bad Request` | Request sai, OTP sai/hết hạn |
| `401 Unauthorized` | Sai username/password |
| `403 Forbidden` | Account chưa verify / bị khóa |
| `404 Not Found` | Resource không tồn tại |
| `409 Conflict` | Username/email/phone đã tồn tại |
| `429 Too Many Requests` | Spam resend/send OTP |
| `500 Internal Server Error` | Lỗi không xác định |
| `503 Service Unavailable` | Email/SMS provider lỗi |

---

# 12. Mã lỗi nghiệp vụ

## 12.1. Validation

| Code | HTTP | Message |
|---|---:|---|
| `VAL-4000` | 400 | Dữ liệu không hợp lệ |
| `VAL-4001` | 400 | Username không hợp lệ |
| `VAL-4002` | 400 | Email không hợp lệ |
| `VAL-4003` | 400 | Số điện thoại không hợp lệ |
| `VAL-4004` | 400 | Mật khẩu không hợp lệ |
| `VAL-4005` | 400 | Mật khẩu xác nhận không khớp |

---

## 12.2. User / Register

| Code | HTTP | Message |
|---|---:|---|
| `USR-4041` | 404 | Không tìm thấy người dùng |
| `USR-4091` | 409 | Username đã tồn tại |
| `USR-4092` | 409 | Email đã tồn tại |
| `USR-4093` | 409 | Số điện thoại đã tồn tại |
| `USR-4031` | 403 | Tài khoản chưa được xác thực |
| `USR-4032` | 403 | Tài khoản đã bị khóa |
| `USR-4001` | 400 | Tài khoản đã được xác thực |

---

## 12.3. Authentication

| Code | HTTP | Message |
|---|---:|---|
| `AUTH-4011` | 401 | Username hoặc mật khẩu không chính xác |
| `AUTH-4012` | 401 | Phiên xác thực không hợp lệ |
| `AUTH-4013` | 401 | Reset token không hợp lệ |
| `AUTH-4014` | 401 | Reset token đã hết hạn |

> Login nên dùng chung message `Username hoặc mật khẩu không chính xác`, không nói riêng username tồn tại hay password sai.

---

## 12.4. OTP

| Code | HTTP | Message |
|---|---:|---|
| `OTP-4001` | 400 | Mã OTP không chính xác |
| `OTP-4002` | 400 | Mã OTP đã hết hạn |
| `OTP-4003` | 400 | Mã OTP đã được sử dụng |
| `OTP-4004` | 400 | Mã OTP đã bị vô hiệu hóa |
| `OTP-4005` | 400 | Mã OTP đã bị khóa do nhập sai quá nhiều lần |
| `OTP-4006` | 400 | Mục đích sử dụng OTP không hợp lệ |
| `OTP-4007` | 400 | Kênh gửi OTP không hợp lệ |
| `OTP-4041` | 404 | Không tìm thấy OTP |
| `OTP-4291` | 429 | Vui lòng chờ trước khi gửi lại OTP |
| `OTP-4292` | 429 | Bạn đã yêu cầu OTP quá nhiều lần |

---

## 12.5. Provider / System

| Code | HTTP | Message |
|---|---:|---|
| `SYS-5031` | 503 | Không thể gửi email OTP |
| `SYS-5032` | 503 | Không thể gửi SMS OTP |
| `SYS-5033` | 503 | Không thể gửi Zalo OTP |
| `SYS-5000` | 500 | Có lỗi xảy ra, vui lòng thử lại |

---

# 13. Quy ước Code Backend

## Package structure

```text
src/main/java/com/example/otpdemo
│
├── config
│   ├── SecurityConfig.java
│   └── MailConfig.java
│
├── controller
│   ├── AuthController.java
│   └── OtpController.java
│
├── dto
│   ├── request
│   └── response
│
├── entity
│   ├── User.java
│   └── OtpLog.java
│
├── enums
│   ├── UserStatus.java
│   ├── OtpPurpose.java
│   ├── OtpChannel.java
│   └── OtpStatus.java
│
├── exception
│   ├── BusinessException.java
│   ├── ErrorCode.java
│   └── GlobalExceptionHandler.java
│
├── repository
│   ├── UserRepository.java
│   └── OtpLogRepository.java
│
├── security
│   └── PasswordEncoderConfig.java
│
├── service
│   ├── AuthService.java
│   ├── OtpService.java
│   ├── EmailService.java
│   └── sender
│       ├── OtpSender.java
│       ├── EmailOtpSender.java
│       ├── SmsOtpSender.java
│       └── ZaloOtpSender.java
│
└── util
    ├── OtpGenerator.java
    ├── OtpHasher.java
    └── MaskingUtils.java
```

---

# 14. Quy ước đặt tên

## REST endpoint

Dùng:

```text
/api/v1/auth/register
/api/v1/auth/login
/api/v1/otp/send
```

Không dùng:

```text
/api/v1/doRegister
/api/v1/sendOTPNow
```

## Java

Class:

```text
PascalCase
```

Method / variable:

```text
camelCase
```

Constant:

```text
UPPER_SNAKE_CASE
```

Database:

```text
snake_case
```

---

# 15. Quy ước Git

Branch chính:

```text
main
```

Có thể dùng:

```text
develop
feature/register
feature/email-otp
feature/forgot-password
feature/frontend
```

Commit:

```text
feat: implement user registration
feat: add email otp sender
feat: verify otp with expiration
feat: add otp resend cooldown
fix: invalidate old otp after resend
refactor: extract otp sender interface
test: add otp verification test cases
```

Không commit:

```text
application-secret.yml
.env
password
mail app password
database password
```

---

# 16. Biến môi trường

Ví dụ:

```env
DB_URL=jdbc:mysql://localhost:3306/otp_demo
DB_USERNAME=root
DB_PASSWORD=your_password

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

OTP_HMAC_SECRET=your_long_random_secret
RESET_TOKEN_SECRET=another_long_random_secret
```

`.gitignore`:

```gitignore
.env
application-local.yml
application-secret.yml
```

---

# 17. Logic `OtpService`

Pseudo-flow gửi OTP:

```text
sendOtp(userId, purpose, channel)

1. Get user
2. Validate user + purpose
3. Validate channel
4. Check rate limit
5. Check resend cooldown
6. Invalidate old PENDING OTP
7. Generate SecureRandom OTP
8. HMAC OTP
9. Save otp_log
10. Send OTP via OtpSender
11. Return otpId + expiresIn + resendAfter
```

Pseudo-flow verify:

```text
verifyOtp(otpId, rawOtp)

1. Find otp_log
2. status == PENDING?
3. now < expiresAt?
4. attemptCount < MAX_ATTEMPTS?
5. HMAC(rawOtp)
6. Constant-time compare hashes

Nếu sai:
    attemptCount++
    nếu >= MAX_ATTEMPTS:
        status = BLOCKED
    trả OTP_INVALID

Nếu đúng:
    status = VERIFIED
    verifiedAt = now

    nếu purpose == VERIFY_ACCOUNT:
        user.status = ACTIVE

    nếu purpose == RESET_PASSWORD:
        generate resetToken

7. Save transaction
8. Return result
```

---

# 18. Transaction và Single-use

Verify OTP nên chạy trong transaction.

Mục tiêu:

```text
Không để 2 request đồng thời dùng cùng một OTP thành công.
```

Logic trạng thái:

```text
PENDING
   ↓
VERIFIED
```

chỉ được xảy ra một lần.

Có thể kiểm soát bằng:

- `@Transactional`
- Lock bản ghi khi cần.
- Hoặc update có điều kiện `WHERE status = 'PENDING'`.

---

# 19. Nội dung Email OTP

Subject:

```text
Mã OTP xác thực tài khoản
```

Body:

```text
Xin chào Nguyễn Văn A,

Mã OTP của bạn là:

583921

Mã có hiệu lực trong 5 phút.

Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.

OTP Demo Team
```

Không gửi password trong email.

---

# 20. Test Cases bắt buộc

## Register

| Test | Expected |
|---|---|
| Register hợp lệ | User `UNVERIFIED` |
| Username tồn tại | `USR-4091` |
| Email tồn tại | `USR-4092` |
| Phone tồn tại | `USR-4093` |
| Password yếu | `VAL-4004` |

## OTP Send

| Test | Expected |
|---|---|
| Send email hợp lệ | Email nhận OTP |
| Resend trước 60s | `OTP-4291` |
| Spam quá giới hạn | `OTP-4292` |
| Resend sau cooldown | OTP mới được gửi |
| Sau resend dùng OTP cũ | `OTP-4004` |

## OTP Verify

| Test | Expected |
|---|---|
| OTP đúng | Success |
| OTP sai | `OTP-4001` |
| OTP hết hạn | `OTP-4002` |
| OTP đã dùng | `OTP-4003` |
| Sai 5 lần | `OTP-4005` |
| OTP đúng sau khi resend OTP mới | Success |

## Account Verification

| Test | Expected |
|---|---|
| User UNVERIFIED login | `USR-4031` |
| Verify OTP thành công | User → `ACTIVE` |
| User ACTIVE login | Success |

## Forgot Password

| Test | Expected |
|---|---|
| Request reset | Có thể gửi OTP |
| OTP reset sai | Reject |
| OTP reset đúng | Trả `resetToken` |
| Reset không có token | Reject |
| Reset token hết hạn | `AUTH-4014` |
| Reset thành công | Password cũ login fail, password mới login success |

---

# 21. Thứ tự triển khai

## Phase 1 – Skeleton

```text
1. Spring Boot
2. MySQL
3. Flyway
4. User Entity
5. OtpLog Entity
6. Repository
7. Exception Handler
8. Response format
```

## Phase 2 – Register

```text
1. Register DTO
2. Register API
3. BCrypt password
4. Validation
5. Duplicate checking
```

## Phase 3 – OTP Core

```text
1. SecureRandom generator
2. HMAC OTP
3. OtpService
4. Expiration
5. Single-use
6. Attempt limit
7. Resend
8. Rate limit
```

## Phase 4 – Email

```text
1. SMTP config
2. EmailOtpSender
3. HTML/Text email
4. Test Gmail thật
```

## Phase 5 – Authentication

```text
1. Verify account
2. ACTIVE status
3. Login
```

## Phase 6 – Forgot Password

```text
1. Forgot password
2. OTP purpose RESET_PASSWORD
3. Verify OTP
4. Reset token
5. Update BCrypt password
```

## Phase 7 – Frontend

```text
1. Register
2. Verify method
3. OTP input
4. Login
5. Forgot password
6. Reset password
7. Success page
```

---

# 22. Definition of Done – Giai đoạn 1

Giai đoạn 1 được xem là hoàn thành khi demo được:

```text
1. Đăng ký tài khoản mới.
2. User ở trạng thái UNVERIFIED.
3. Chọn Email.
4. Backend sinh OTP.
5. Gmail nhận OTP thật.
6. Nhập OTP đúng.
7. User chuyển ACTIVE.
8. OTP đã dùng không dùng lại được.
9. OTP quá 5 phút bị từ chối.
10. Resend trước 60 giây bị từ chối.
11. Resend hợp lệ tạo OTP mới.
12. OTP cũ sau resend không còn hợp lệ.
13. Nhập sai 5 lần bị khóa OTP.
14. Login user ACTIVE thành công.
15. Forgot password gửi OTP.
16. Verify OTP reset thành công.
17. Đặt mật khẩu mới.
18. Login bằng mật khẩu mới thành công.
```

---

# 23. Kịch bản demo trước giảng viên

```text
Bước 1:
Đăng ký user mới.

Bước 2:
Cho xem DB:
status = UNVERIFIED.

Bước 3:
Chọn xác thực qua Email.

Bước 4:
Mở Gmail và lấy OTP.

Bước 5:
Nhập sai OTP 1 lần để cho thấy attempt_count tăng.

Bước 6:
Nhập OTP đúng.

Bước 7:
Cho xem DB:
OTP = VERIFIED
User = ACTIVE.

Bước 8:
Thử nhập lại OTP cũ.
→ Bị từ chối do single-use.

Bước 9:
Login bình thường.
→ Không yêu cầu OTP.

Bước 10:
Logout → Forgot Password.

Bước 11:
Gửi OTP reset password.

Bước 12:
Verify OTP → đặt mật khẩu mới.

Bước 13:
Login bằng mật khẩu mới.
→ Thành công.
```

Nếu còn thời gian có thể demo thêm:

```text
Resend OTP → OTP cũ INVALIDATED.
Expiration → OTP_EXPIRED.
5 lần sai → OTP_BLOCKED.
```

---

# 24. Các điểm cần nhấn mạnh khi báo cáo

1. OTP được sinh bằng `SecureRandom`.
2. OTP không lưu plaintext trong DB.
3. OTP có thời gian sống ngắn.
4. OTP chỉ dùng được một lần.
5. OTP cũ bị vô hiệu khi resend.
6. Có cooldown và rate limit chống spam OTP.
7. Có giới hạn số lần nhập sai.
8. Password được BCrypt.
9. OTP có `purpose`, không dùng nhầm OTP đăng ký để reset password.
10. Reset password chỉ được thực hiện sau khi OTP được xác thực.
11. Secret/email password không commit Git.
12. Frontend countdown chỉ hỗ trợ UX; backend mới là nguồn quyết định cuối cùng.

---

# 25. Scope mở rộng sau Giai đoạn 1

Có thể phát triển thêm:

```text
Email OTP
   ↓
SMS OTP
   ↓
Zalo OTP
   ↓
TOTP / Google Authenticator
   ↓
2FA Login
   ↓
Redis OTP Store
   ↓
Device/IP Rate Limiting
   ↓
Audit Log
```

Nhưng không nên triển khai các phần này trước khi luồng OTP Email cơ bản chạy ổn.
