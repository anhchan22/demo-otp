# Demo lỗ hổng OTP Response Code Manipulation

## 1. Mục tiêu

Demo cho thấy kẻ tấn công có thể dùng Burp Suite sửa HTTP response của API xác thực OTP. Frontend tin rằng OTP hợp lệ khi nhận HTTP `200`, dù backend ban đầu trả lỗi vì OTP sai.

Trong chế độ demo hiện tại, backend còn cố tình bỏ qua trạng thái `verified` ở bước đổi mật khẩu để minh họa tác động end-to-end: OTP sai vẫn có thể dẫn đến đổi mật khẩu và đăng nhập bằng mật khẩu mới.

> Đây là cấu hình cố tình tạo lỗ hổng cho môi trường local. Không sử dụng chế độ này trong production.

## 2. Thành phần tham gia

| Thành phần | Địa chỉ | Vai trò |
|---|---|---|
| Frontend | `http://localhost:5174` | Gửi request và xử lý kết quả OTP |
| Backend | `http://localhost:8080` | Xử lý OTP, flow token và đổi mật khẩu |
| Burp Suite | `127.0.0.1:8081` | Chặn và sửa HTTP response |

## 3. Chuẩn bị

Trong Burp Suite:

1. Bật proxy listener `127.0.0.1:8081`.
2. Mở Burp Browser.
3. Đảm bảo Burp Browser truy cập được `http://localhost:5174`.
4. Trong `Proxy settings`, bật response interception rule:

   ```text
   Match type: Status code
   Relationship: Matches
   Condition: 400
   ```

5. Khi tải trang, có thể để `Intercept off`. Chỉ bật `Intercept on` ngay trước khi gửi OTP sai.

## 4. Các bước demo

### Bước 1: Tạo flow quên mật khẩu

1. Mở:

   ```text
   http://localhost:5174/forgot-password
   ```

2. Nhập username/email của tài khoản tồn tại.
3. Chọn phương thức nhận OTP.
4. Chuyển tới màn hình nhập OTP.

### Bước 2: Gửi OTP sai

Nhập mã sai, ví dụ:

```text
000000
```

Request cần tìm trong Burp là:

```http
POST /api/v1/otp/verify
```

Response bình thường:

```http
HTTP/1.1 400
```

Body chứa mã lỗi tương tự:

```json
{
  "code": "OTP-4001",
  "message": "Mã OTP không chính xác"
}
```

### Bước 3: Sửa response bằng Burp

1. Trong `Proxy → Intercept`, bật `Intercept on`.
2. Gửi lại OTP sai.
3. Bỏ qua các message `Type: WS` của Vite; chỉ xử lý request HTTP `/api/v1/otp/verify`.
4. Cho request OTP đi tới backend bằng `Forward`.
5. Khi response bị chặn, sửa dòng đầu:

   ```http
   HTTP/1.1 400
   ```

   thành:

   ```http
   HTTP/1.1 200 OK
   ```

6. Bấm `Forward`.

### Bước 4: Đổi mật khẩu

Frontend hiểu nhầm response `200` là OTP hợp lệ và chuyển tới màn hình đổi mật khẩu.

Nhập mật khẩu mới, ví dụ:

```text
22052005
```

Trong chế độ demo backend, endpoint:

```http
POST /api/v1/auth/reset-password
```

cố tình chấp nhận flow token chưa có `verified=true`.

### Bước 5: Kiểm chứng

Đăng nhập bằng mật khẩu mới. Đăng nhập thành công chứng minh chuỗi khai thác đã đi xuyên suốt:

```text
OTP sai
  → sửa response 400 thành 200
  → frontend cho qua bước OTP
  → backend demo cho đổi mật khẩu
  → đăng nhập bằng mật khẩu mới
```

## 5. Phân tích lỗ hổng

### Lỗi 1: Frontend tin tưởng HTTP status code

Frontend dùng `response.ok` để quyết định request thành công. Khi Burp sửa `400` thành `200`, frontend xử lý body lỗi như một response thành công.

Đây là lỗi **improper client-side trust / response code manipulation**.

Frontend không được tự quyết định OTP hợp lệ chỉ dựa trên status code. Trạng thái xác thực phải do backend chứng minh bằng một flow token đã được ký và đánh dấu verified.

### Lỗi 2: Backend bỏ qua trạng thái OTP đã xác thực

Trong chế độ demo, `/auth/reset-password` chỉ kiểm tra flow token có đúng mục đích `RESET_PASSWORD`, nhưng bỏ qua điều kiện:

```java
claims.verified() == true
```

Đây là lỗi **broken access control / missing server-side authorization check**.

Nếu chỉ có lỗi 1, frontend có thể hiển thị sai hoặc cho đi tiếp trên giao diện nhưng backend vẫn chặn. Lỗi 2 làm cho tác động trở thành đổi mật khẩu thật.

## 6. Khắc phục

### Khắc phục frontend

- Không coi HTTP `200` là bằng chứng OTP hợp lệ.
- Kiểm tra response thành công phải có dữ liệu nghiệp vụ hợp lệ, ví dụ `purpose`, `challengeId` hoặc cờ thành công do backend trả về.
- Nếu response là body lỗi hoặc thiếu trường bắt buộc, phải dừng flow và hiển thị lỗi.
- Không dùng giá trị fallback từ state phía client để tự suy ra OTP đã xác thực.
- Không hiển thị khu vực bảo mật chỉ dựa vào `currentUser` trong React state.

Ví dụ nguyên tắc xử lý:

```javascript
const result = await api('/otp/verify', options)

if (!result.success || result.verified !== true) {
  throw new Error('OTP không hợp lệ')
}
```

### Khắc phục backend

Endpoint reset password luôn phải yêu cầu:

```java
claims.purpose() == OtpPurpose.RESET_PASSWORD
&& claims.verified() == true
```

Không được có chế độ production bỏ qua điều kiện này. Cấu hình demo:

```yaml
app:
  demo-vulnerable-otp: false
```

Tốt nhất là xóa hoàn toàn nhánh vulnerable khỏi bản production thay vì chỉ tắt bằng cấu hình.

### Bảo vệ flow OTP

- OTP phải được kiểm tra hoàn toàn ở backend.
- OTP chỉ dùng một lần và phải hết hạn.
- Giới hạn số lần thử và tốc độ gửi lại.
- Chỉ phát hành flow token `verified=true` sau khi OTP hợp lệ.
- Invalidate token sau khi đổi mật khẩu thành công.
- Dùng cookie `HttpOnly`, `Secure` và `SameSite` phù hợp.
- Thêm test đảm bảo OTP sai luôn bị từ chối ở `/reset-password`, kể cả khi frontend bị can thiệp.

## 7. Kết luận trình bày

Ứng dụng mắc hai lỗi liên quan đến việc tin tưởng phía client và thiếu kiểm tra quyền ở backend. Burp Suite có thể đổi response OTP sai từ `400` thành `200`, làm frontend cho phép đi tiếp. Khi backend đồng thời bỏ qua cờ `verified`, kẻ tấn công có thể đổi mật khẩu và đăng nhập bằng mật khẩu mới.

Biện pháp cốt lõi là: **mọi quyết định xác thực và cho phép đổi mật khẩu phải được thực hiện và kiểm tra lại ở backend; frontend chỉ hiển thị kết quả do backend chứng minh.**

