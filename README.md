# DuAn1-Nhom3 - API Backend cho Ứng dụng Mua sắm Quần áo

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

## 📖 Hướng dẫn Test API bằng Postman

### 🔐 1. Authentication

#### Đăng ký tài khoản mới

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:3000/api/users/register`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "name": "Nguyễn Văn A",
    "email": "user@example.com",
    "password": "password123"
  }
  ```

**Response:**
```json
{
  "message": "Đăng ký thành công",
  "user": {
    "id": "...",
    "name": "Nguyễn Văn A",
    "email": "user@example.com",
    "role": "user",
    "avatar": null
  }
}
```

**Lưu ý:** User mới đăng ký sẽ tự động nhận voucher giảm giá 50% vĩnh viễn!

#### Đăng nhập

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:3000/api/users/login`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```

**Response:**
```json
{
  "message": "Đăng nhập thành công",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "...",
    "name": "Nguyễn Văn A",
    "email": "user@example.com",
    "role": "user",
    "avatar": null
  }
}
```

**Lưu token này để dùng cho các API yêu cầu authentication!**

#### Cập nhật Profile (Name và Avatar)

**Request:**
- **Method:** `PUT`
- **URL:** `http://localhost:3000/api/users/profile`
- **Headers:**
  ```
  Content-Type: application/json
  Authorization: Bearer <token>
  ```
- **Body (raw JSON):**
  ```json
  {
    "name": "Nguyễn Văn B",
    "avatar": "/uploads/1234567890-123456789.jpg"
  }
  ```

**Response:**
```json
{
  "message": "Cập nhật profile thành công",
  "user": {
    "id": "...",
    "name": "Nguyễn Văn B",
    "email": "user@example.com",
    "role": "user",
    "avatar": "/uploads/1234567890-123456789.jpg"
  }
}
```

---

### 📁 2. Categories

#### Lấy danh sách categories

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:3000/api/categories`

**Response:**
```json
[
  {
    "_id": "...",
    "name": "Áo thun",
    "description": "Các mẫu áo thun",
    "createdAt": "...",
    "updatedAt": "..."
  }
]
```

#### Tạo category mới

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:3000/api/categories`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "name": "Áo thun",
    "description": "Các mẫu áo thun"
  }
  ```

**Lưu lại `_id` của category để dùng khi tạo sản phẩm!**

---

### 🛍️ 3. Products

#### Lấy danh sách sản phẩm

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:3000/api/products`
- **Query Parameters (optional):**
  - `search`: Tìm kiếm theo tên (ví dụ: `?search=áo`)
  - `category`: Lọc theo category ID (ví dụ: `?category=6760abc123...`)

**Response:**
```json
[
  {
    "_id": "...",
    "name": "Áo thun basic",
    "description": "Chất cotton 100%",
    "price": 199000,
    "stock": 50,
    "category": "...",
    "image": "/uploads/1234567890-123456789.jpg",
    "colors": ["Đỏ", "Đen", "Xanh"],
    "sizes": ["S", "M", "L", "XL"],
    "createdAt": "...",
    "updatedAt": "..."
  }
]
```

#### Lấy chi tiết sản phẩm

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:3000/api/products/:id`

**Response:**
```json
{
  "_id": "...",
  "name": "Áo thun basic",
  "description": "Chất cotton 100%",
  "price": 199000,
  "stock": 50,
  "category": "...",
  "image": "/uploads/1234567890-123456789.jpg",
  "colors": ["Đỏ", "Đen", "Xanh"],
  "sizes": ["S", "M", "L", "XL"],
  "createdAt": "...",
  "updatedAt": "..."
}
```

#### Tạo sản phẩm mới

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:3000/api/products`
- **Headers:**
  ```
  (Không cần Content-Type, Postman sẽ tự động set)
  ```
- **Body (form-data):**

| Key | Type | Value |
|-----|------|-------|
| image | File | Chọn file ảnh (jpg, png, gif, webp) |
| name | Text | Áo thun basic |
| description | Text | Chất cotton 100% |
| price | Text | 199000 |
| stock | Text | 50 |
| category | Text | 6760abc123... (ID của category) |
| colors | Text | `["Đỏ", "Đen", "Xanh"]` (JSON array string) |
| sizes | Text | `["S", "M", "L", "XL"]` (JSON array string) |

**Lưu ý:** 
- `colors` và `sizes` phải là JSON array string, ví dụ: `["Đỏ", "Đen"]`
- File ảnh tối đa 5MB
- Chỉ chấp nhận: jpeg, jpg, png, gif, webp

**Response:**
```json
{
  "_id": "...",
  "name": "Áo thun basic",
  "description": "Chất cotton 100%",
  "price": 199000,
  "stock": 50,
  "category": "...",
  "image": "/uploads/1234567890-123456789.jpg",
  "colors": ["Đỏ", "Đen", "Xanh"],
  "sizes": ["S", "M", "L", "XL"],
  "createdAt": "...",
  "updatedAt": "..."
}
```

---

### 🛒 4. Cart (Giỏ hàng)

**Tất cả endpoints yêu cầu authentication: `Authorization: Bearer <token>`**

#### Lấy giỏ hàng

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:3000/api/cart`
- **Headers:**
  ```
  Authorization: Bearer <token>
  ```

**Response:**
```json
{
  "items": [
    {
      "product": {
        "_id": "...",
        "name": "Áo thun basic",
        "price": 199000,
        "image": "/uploads/..."
      },
      "quantity": 2,
      "color": "Đỏ",
      "size": "M"
    }
  ],
  "total": 398000
}
```

#### Thêm sản phẩm vào giỏ hàng

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:3000/api/cart`
- **Headers:**
  ```
  Content-Type: application/json
  Authorization: Bearer <token>
  ```
- **Body (raw JSON):**
  ```json
  {
    "productId": "product_id_here",
    "quantity": 2,
    "color": "Đỏ",
    "size": "M"
  }
  ```

**Response:**
```json
{
  "message": "Đã thêm vào giỏ hàng",
  "cart": {
    "items": [...],
    "total": 398000
  }
}
```

#### Cập nhật số lượng sản phẩm

**Request:**
- **Method:** `PUT`
- **URL:** `http://localhost:3000/api/cart/:productId`
- **Headers:**
  ```
  Content-Type: application/json
  Authorization: Bearer <token>
  ```
- **Body (raw JSON):**
  ```json
  {
    "quantity": 3
  }
  ```

#### Xóa sản phẩm khỏi giỏ hàng

**Request:**
- **Method:** `DELETE`
- **URL:** `http://localhost:3000/api/cart/:productId`
- **Headers:**
  ```
  Authorization: Bearer <token>
  ```

---

### ⭐ 5. Reviews (Đánh giá)

#### Lấy reviews của sản phẩm

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:3000/api/reviews/product/:productId`

**Response:**
```json
[
  {
    "_id": "...",
    "product": "...",
    "user": {
      "id": "...",
      "name": "Nguyễn Văn A",
      "email": "user@example.com"
    },
    "rating": 5,
    "comment": "Sản phẩm rất tốt!",
    "adminReply": null,
    "createdAt": "..."
  }
]
```

#### Lấy rating trung bình

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:3000/api/reviews/product/:productId/rating`

**Response:**
```json
{
  "averageRating": 4.5,
  "totalReviews": 10
}
```

#### Tạo review mới

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:3000/api/reviews`
- **Headers:**
  ```
  Content-Type: application/json
  Authorization: Bearer <token>
  ```
- **Body (raw JSON):**
  ```json
  {
    "productId": "product_id",
    "rating": 5,
    "comment": "Sản phẩm rất tốt!"
  }
  ```

**Lưu ý:** `comment` có thể để trống (optional)

---

### 🎫 6. Vouchers

#### Lấy danh sách vouchers

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:3000/api/vouchers`
- **Headers (optional):**
  ```
  Authorization: Bearer <token>
  ```

**Lưu ý:**
- Nếu có token: Lấy cả vouchers của user và vouchers public
- Nếu không có token: Chỉ lấy vouchers public

**Response:**
```json
[
  {
    "_id": "...",
    "code": "WELCOME1234",
    "name": "Voucher chào mừng",
    "description": "Giảm 50% cho khách hàng mới",
    "discountType": "percentage",
    "discountValue": 50,
    "minPurchaseAmount": 0,
    "maxDiscountAmount": null,
    "startDate": "...",
    "endDate": "...",
    "usageLimit": null,
    "usedCount": 0,
    "isActive": true,
    "user": "..." // null = public, có giá trị = voucher cho user cụ thể
  }
]
```

#### Lấy vouchers public

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:3000/api/vouchers/public`

#### Tạo voucher mới (Admin only)

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:3000/api/vouchers`
- **Headers:**
  ```
  Content-Type: application/json
  Authorization: Bearer <admin_token>
  ```
- **Body (raw JSON):**
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
    "usageLimit": 100,
    "userId": null
  }
  ```

**Voucher types:**
- `discountType`: `"percentage"` hoặc `"fixed"`
- `percentage`: Giảm theo phần trăm (ví dụ: 50 = 50%)
- `fixed`: Giảm số tiền cố định (ví dụ: 50000 = 50,000₫)
- `userId`: `null` = voucher public, có giá trị = voucher cho user cụ thể

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

### 📤 7. Upload Ảnh

#### Upload một ảnh

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:3000/api/upload`
- **Body (form-data):**

| Key | Type | Value |
|-----|------|-------|
| image | File | Chọn file ảnh (jpg, png, gif, webp) |

**Lưu ý:**
- File tối đa 5MB
- Chỉ chấp nhận: jpeg, jpg, png, gif, webp

**Response:**
```json
{
  "message": "Upload ảnh thành công",
  "filename": "1234567890-123456789.jpg",
  "path": "/uploads/1234567890-123456789.jpg",
  "size": 123456
}
```

**Sử dụng `path` để lưu vào profile hoặc sản phẩm!**

#### Upload nhiều ảnh

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:3000/api/upload/multiple`
- **Body (form-data):**

| Key | Type | Value |
|-----|------|-------|
| images | File | Chọn nhiều file ảnh (tối đa 10) |

**Response:**
```json
{
  "message": "Upload 2 ảnh thành công",
  "files": [
    {
      "filename": "1234567890-123456789.jpg",
      "path": "/uploads/1234567890-123456789.jpg",
      "size": 123456
    },
    {
      "filename": "1234567891-123456790.png",
      "path": "/uploads/1234567891-123456790.png",
      "size": 234567
    }
  ]
}
```

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
- Ví dụ: `["Đỏ", "Đen", "Xanh"]` hoặc `["S", "M", "L"]`

### Authentication

- Hầu hết các API cần authentication (trừ đăng ký, đăng nhập, xem sản phẩm)
- Sử dụng JWT token trong header: `Authorization: Bearer <token>`
- Token có thời hạn 1 giờ
- Lấy token từ response khi đăng nhập

### Cách sử dụng Token trong Postman

1. Sau khi đăng nhập, copy token từ response
2. Vào tab **Authorization** trong Postman
3. Chọn type: **Bearer Token**
4. Paste token vào ô **Token**
5. Hoặc thêm header thủ công: `Authorization: Bearer <token>`

---

## 🔧 Troubleshooting

### Server không kết nối được MongoDB

**Lỗi:** `Không thể kết nối MongoDB`

**Giải pháp:**
1. Kiểm tra MongoDB có đang chạy không
2. Kiểm tra `MONGODB_URI` trong file `.env`
3. Thử kết nối bằng MongoDB Compass

### Token không hợp lệ

**Lỗi:** `Token không hợp lệ` hoặc `401 Unauthorized`

**Giải pháp:**
1. Kiểm tra token có đúng format: `Bearer <token>`
2. Token có thể đã hết hạn (1 giờ), đăng nhập lại để lấy token mới
3. Kiểm tra header `Authorization` có đúng không

### Upload ảnh bị lỗi

**Lỗi:** `Chỉ cho phép upload file ảnh`

**Giải pháp:**
1. Kiểm tra file có đúng định dạng: jpeg, jpg, png, gif, webp
2. Kiểm tra file size < 5MB
3. Trong Postman, chọn **form-data** và chọn type **File** cho field `image`

### Ảnh không hiển thị

**Giải pháp:**
1. Kiểm tra server có chạy không
2. Kiểm tra file ảnh có tồn tại trong `server/uploads/`
3. Truy cập trực tiếp: `http://localhost:3000/uploads/filename.jpg`

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
│   │   ├── vouchers.js
│   │   └── upload.js
│   ├── uploads/              # Thư mục lưu ảnh
│   └── server.js             # Entry point
```

---

## 🎯 Quy trình test cơ bản

1. **Đăng ký tài khoản mới** → Lưu user ID
2. **Đăng nhập** → Lưu token
3. **Tạo category** → Lưu category ID
4. **Upload ảnh** → Lưu path
5. **Tạo sản phẩm** (dùng category ID và image path) → Lưu product ID
6. **Thêm vào giỏ hàng** (dùng product ID và token)
7. **Xem giỏ hàng** (dùng token)
8. **Tạo review** (dùng product ID và token)
9. **Xem reviews** (dùng product ID)
10. **Tạo voucher** (nếu là admin)
