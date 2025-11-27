# DuAn1-Nhom3 - Ứng dụng Mua sắm Quần áo

## 🚀 Cài đặt Server

### 1. Cài đặt MongoDB

Đảm bảo MongoDB đang chạy trên máy của bạn:
```bash
# Windows
mongod

# Linux/Mac
sudo systemctl start mongod
```

### 2. Cấu hình môi trường

Tạo file `server/.env` trong thư mục server:

```env
MONGODB_URI=mongodb://127.0.0.1:27017/duan1
JWT_SECRET=thay_bang_chuoi_bao_mat_ngau_nhien
PORT=3000
```

### 3. Cài đặt dependencies

```bash
cd server
npm install
```

### 4. Chạy server

```bash
npm start
```

Server sẽ chạy tại: `http://localhost:3000`

Kiểm tra server đang chạy:
```bash
curl http://localhost:3000
```

---

## 📱 Cài đặt Android App

### 1. Mở project trong Android Studio

- Mở Android Studio
- Chọn `Open` và chọn thư mục `DuAn1Nhom3_app`
- Đợi Gradle sync hoàn tất

### 2. Cấu hình API URL

File `ApiClient.java` đã được cấu hình để kết nối với server:
- **Emulator**: `http://10.0.2.2:3000/api/`
- **Thiết bị thật**: Thay đổi IP trong `ApiClient.java` thành IP máy tính của bạn

### 3. Chạy ứng dụng

- Kết nối thiết bị Android hoặc khởi động emulator
- Click `Run` trong Android Studio
- Chọn thiết bị/emulator và đợi app cài đặt

---

## 📁 Cấu trúc dự án

```
DuAn1-Nhom3-Manh/
├── server/                    # Backend Node.js
│   ├── config/
│   │   └── database.js       # Cấu hình MongoDB
│   ├── middleware/
│   │   └── auth.js           # Authentication middleware
│   ├── models/               # MongoDB models
│   │   ├── User.js
│   │   ├── Product.js
│   │   ├── Category.js
│   │   ├── Cart.js
│   │   ├── Order.js
│   │   ├── Review.js
│   │   └── Voucher.js
│   ├── routes/               # API routes
│   │   ├── users.js
│   │   ├── products.js
│   │   ├── categories.js
│   │   ├── cart.js
│   │   ├── orders.js
│   │   ├── reviews.js
│   │   └── vouchers.js
│   └── server.js             # Entry point
│
└── DuAn1Nhom3_app/           # Android app
    └── app/
        └── src/
            └── main/
                ├── java/
                │   └── fpl/manhph61584/duan1_nhom3_app/
                │       ├── network/         # API client
                │       ├── activities/      # Activities
                │       └── ...
                └── res/                     # Resources
```

---

## 🔌 API Endpoints

### Authentication (`/api/users`)

#### Đăng ký
```http
POST /api/users/register
Content-Type: application/json

{
  "name": "Nguyễn Văn A",
  "email": "user@example.com",
  "password": "password123"
}
```

**Lưu ý**: Khi đăng ký thành công, user sẽ tự động nhận một voucher giảm giá 50% vĩnh viễn!

#### Đăng nhập
```http
POST /api/users/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "message": "Đăng nhập thành công",
  "token": "jwt_token_here",
  "user": {
    "id": "...",
    "name": "Nguyễn Văn A",
    "email": "user@example.com",
    "role": "user"
  }
}
```

---

### Categories (`/api/categories`)

#### Lấy danh sách categories
```http
GET /api/categories
```

#### Tạo category mới
```http
POST /api/categories
Content-Type: application/json

{
  "name": "Áo thun",
  "description": "Các mẫu áo thun"
}
```

---

### Products (`/api/products`)

#### Lấy danh sách sản phẩm
```http
GET /api/products?search=áo&category=category_id
```

Query parameters:
- `search` (optional): Tìm kiếm theo tên
- `category` (optional): Lọc theo category ID

#### Lấy chi tiết sản phẩm
```http
GET /api/products/:id
```

#### Tạo sản phẩm mới (form-data)
```http
POST /api/products
Content-Type: multipart/form-data

Form fields:
- image: File (ảnh sản phẩm)
- name: Text
- description: Text
- price: Text (số)
- stock: Text (số)
- category: Text (_id của category)
- colors: Text (JSON array, ví dụ: ["Đỏ", "Đen", "Xanh"])
- sizes: Text (JSON array, ví dụ: ["S", "M", "L"])
```

**Ví dụ với Postman:**
| Key | Type | Value |
|-----|------|-------|
| image | File | product.jpg |
| name | Text | Áo thun basic |
| description | Text | Chất cotton 100% |
| price | Text | 199000 |
| stock | Text | 50 |
| category | Text | 6760abc123... |
| colors | Text | ["Đỏ", "Đen", "Xanh"] |
| sizes | Text | ["S", "M", "L", "XL"] |

---

### Cart (`/api/cart`)

**Tất cả endpoints yêu cầu authentication: `Authorization: Bearer <token>`**

#### Lấy giỏ hàng
```http
GET /api/cart
Authorization: Bearer <token>
```

#### Thêm sản phẩm vào giỏ hàng
```http
POST /api/cart
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": "product_id_here",
  "quantity": 2,
  "color": "Đỏ",
  "size": "M"
}
```

#### Cập nhật số lượng
```http
PUT /api/cart/:productId
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantity": 3
}
```

#### Xóa sản phẩm khỏi giỏ hàng
```http
DELETE /api/cart/:productId
Authorization: Bearer <token>
```

---

### Reviews (`/api/reviews`)

#### Lấy reviews của sản phẩm
```http
GET /api/reviews/product/:productId
```

#### Lấy rating trung bình
```http
GET /api/reviews/product/:productId/rating
```

#### Tạo review mới
```http
POST /api/reviews
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": "product_id",
  "rating": 5,
  "comment": "Sản phẩm rất tốt!"
}
```

---

### Vouchers (`/api/vouchers`)

#### Lấy danh sách vouchers
- **Đã đăng nhập**: Lấy cả vouchers của user và vouchers public
- **Chưa đăng nhập**: Chỉ lấy vouchers public

```http
GET /api/vouchers
Authorization: Bearer <token>  # Optional
```

#### Lấy vouchers public
```http
GET /api/vouchers/public
```

#### Admin tạo voucher mới
```http
POST /api/vouchers
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "code": "SALE50",
  "name": "Giảm giá 50%",
  "description": "Giảm 50% cho đơn hàng từ 500k",
  "discountType": "percentage",
  "discountValue": 50,
  "minPurchaseAmount": 500000,
  "maxDiscountAmount": 200000,
  "startDate": "2024-01-01T00:00:00.000Z",
  "endDate": "2024-12-31T23:59:59.999Z",
  "usageLimit": 100,
  "userId": null  // null = public, có giá trị = voucher cho user cụ thể
}
```

**Voucher types:**
- `discountType`: `"percentage"` hoặc `"fixed"`
- `percentage`: Giảm theo phần trăm (ví dụ: 50 = 50%)
- `fixed`: Giảm số tiền cố định (ví dụ: 50000 = 50,000₫)

---

## ✨ Tính năng

### User Features
- ✅ Đăng ký/Đăng nhập
- ✅ Xem danh sách sản phẩm
- ✅ Tìm kiếm sản phẩm
- ✅ Lọc sản phẩm theo category
- ✅ Xem chi tiết sản phẩm (hình ảnh, mô tả, giá, màu sắc, size)
- ✅ Thêm sản phẩm vào giỏ hàng (lưu trên server MongoDB)
- ✅ Xem giỏ hàng
- ✅ Chọn voucher giảm giá
- ✅ Nhập thông tin đặt hàng (số điện thoại, địa chỉ, ghi chú)
- ✅ Đánh giá sản phẩm
- ✅ Xem lịch sử đơn hàng (trang cá nhân)
- ✅ Trang cá nhân với thông tin user

### Voucher System
- ✅ User mới đăng ký tự động nhận voucher 50% vĩnh viễn
- ✅ Admin có thể tạo vouchers public hoặc cho user cụ thể
- ✅ Hỗ trợ 2 loại giảm giá: percentage và fixed amount
- ✅ Tự động tính toán giảm giá trong giỏ hàng

### Product Features
- ✅ Sản phẩm có thể có nhiều màu sắc và size
- ✅ Màu sắc và size được lưu trên server (không hardcode)
- ✅ Tìm kiếm và lọc theo category

---

## 📖 Hướng dẫn sử dụng

### 1. Tạo tài khoản

1. Mở ứng dụng Android
2. Click "Đăng ký"
3. Nhập thông tin: Họ tên, Email, Mật khẩu
4. Click "Đăng ký"

**Lưu ý**: Bạn sẽ tự động nhận voucher giảm giá 50% vĩnh viễn!

### 2. Tạo danh mục sản phẩm

Sử dụng Postman hoặc curl:

```bash
curl -X POST http://localhost:3000/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Áo thun",
    "description": "Các mẫu áo thun"
  }'
```

Lưu lại `_id` của category để dùng khi tạo sản phẩm.

### 3. Thêm sản phẩm

Sử dụng Postman với form-data:

1. Method: `POST`
2. URL: `http://localhost:3000/api/products`
3. Body type: `form-data`
4. Thêm các fields:
   - `image`: Chọn file ảnh
   - `name`: Tên sản phẩm
   - `description`: Mô tả
   - `price`: Giá
   - `stock`: Số lượng tồn kho
   - `category`: ID của category
   - `colors`: JSON array `["Đỏ", "Đen", "Xanh"]`
   - `sizes`: JSON array `["S", "M", "L"]`

### 4. Mua sắm

1. Đăng nhập vào ứng dụng
2. Duyệt sản phẩm trên trang chủ
3. Click vào category để lọc sản phẩm
4. Click vào sản phẩm để xem chi tiết
5. Chọn màu, size, số lượng
6. Click "Thêm vào giỏ hàng"
7. Sản phẩm sẽ được lưu vào giỏ hàng trên server

### 5. Thanh toán

1. Vào giỏ hàng
2. Xem danh sách sản phẩm
3. Chọn voucher (nếu có)
4. Nhập số điện thoại và địa chỉ (bắt buộc)
5. Nhập ghi chú (tùy chọn)
6. Xem tổng tiền (đã trừ giảm giá nếu có voucher)
7. Click "Thanh toán"

### 6. Tạo voucher (Admin)

1. Đăng nhập với tài khoản admin
2. Sử dụng API POST `/api/vouchers` với token admin
3. Tạo voucher public hoặc cho user cụ thể

**Ví dụ voucher giảm 50%, tối đa 200k, cho đơn hàng từ 500k:**
```json
{
  "code": "SALE50",
  "name": "Giảm giá 50%",
  "description": "Giảm 50% cho đơn hàng từ 500k, tối đa 200k",
  "discountType": "percentage",
  "discountValue": 50,
  "minPurchaseAmount": 500000,
  "maxDiscountAmount": 200000,
  "startDate": "2024-01-01T00:00:00.000Z",
  "endDate": "2024-12-31T23:59:59.999Z",
  "usageLimit": null,
  "userId": null
}
```

---

## 🔧 Troubleshooting

### Server không kết nối được MongoDB

**Lỗi**: `Không thể kết nối MongoDB`

**Giải pháp**:
1. Kiểm tra MongoDB có đang chạy không
2. Kiểm tra `MONGODB_URI` trong file `.env`
3. Thử kết nối bằng MongoDB Compass

### Android app không kết nối được server

**Lỗi**: `Lỗi kết nối` trong app

**Giải pháp**:
1. **Emulator**: Đảm bảo dùng `http://10.0.2.2:3000`
2. **Thiết bị thật**: 
   - Đảm bảo điện thoại và máy tính cùng mạng WiFi
   - Tìm IP máy tính: `ipconfig` (Windows) hoặc `ifconfig` (Linux/Mac)
   - Cập nhật IP trong `ApiClient.java`
   - Đảm bảo firewall không chặn port 3000

### Voucher không hiển thị

**Giải pháp**:
1. Kiểm tra user đã đăng nhập chưa
2. Kiểm tra voucher có `isActive: true`
3. Kiểm tra ngày hiện tại trong khoảng `startDate` và `endDate`
4. Kiểm tra voucher có còn lượt sử dụng không (nếu có `usageLimit`)

### Ảnh sản phẩm không hiển thị

**Giải pháp**:
1. Kiểm tra server có chạy không
2. Kiểm tra file ảnh có tồn tại trong `server/uploads/`
3. Kiểm tra URL ảnh trong response API
4. Emulator: Đảm bảo dùng `http://10.0.2.2:3000/uploads/...`

---

## 📝 Ghi chú quan trọng

### Voucher tự động khi đăng ký

- Mỗi user mới đăng ký sẽ tự động nhận một voucher:
  - Code: `WELCOME` + 8 ký tự đầu của user ID
  - Giảm giá: 50%
  - Thời hạn: Vĩnh viễn (đến 31/12/2099)
  - Không giới hạn lượt sử dụng
  - Chỉ dành cho user đó

### Colors và Sizes

- Màu sắc và size của sản phẩm được lưu trong database
- Khi tạo sản phẩm, cần gửi `colors` và `sizes` dưới dạng JSON array string
- Android app sẽ tự động hiển thị từ API

### Authentication

- Hầu hết các API cần authentication (trừ đăng ký, đăng nhập, xem sản phẩm)
- Sử dụng JWT token trong header: `Authorization: Bearer <token>`
- Token có thời hạn 1 giờ
