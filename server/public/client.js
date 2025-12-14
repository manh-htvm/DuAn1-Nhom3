/**
 * Admin Client - Quản lý tất cả chức năng admin
 * File này chứa tất cả các hàm để quản lý admin từ phía client (JavaScript)
 */

// Get base URL from current location or use default
const BASE_URL = (typeof window !== 'undefined' && window.location.origin) 
  ? window.location.origin 
  : (typeof process !== 'undefined' && process.env && process.env.API_URL) 
    ? process.env.API_URL 
    : 'http://localhost:3000';

class AdminClient {
  constructor(token) {
    this.token = token;
    this.baseURL = BASE_URL;
  }

  /**
   * Tạo headers với token
   */
  getHeaders() {
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${this.token}`
    };
  }

  /**
   * Xử lý response
   */
  async handleResponse(response) {
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || data.error || 'Lỗi không xác định');
    }
    return data;
  }

  // ==================== QUẢN LÝ SẢN PHẨM ====================

  /**
   * Lấy danh sách tất cả sản phẩm
   */
  async getAllProducts(search = null, category = null) {
    try {
      let url = `${this.baseURL}/api/products?`;
      if (search) url += `search=${encodeURIComponent(search)}&`;
      if (category) url += `category=${encodeURIComponent(category)}&`;
      url = url.replace(/&$/, '');

      const response = await fetch(url, {
        method: 'GET',
        headers: this.getHeaders() // Gửi token để backend nhận diện admin
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi lấy danh sách sản phẩm: ${error.message}`);
    }
  }

  /**
   * Lấy danh sách categories
   */
  async getCategories() {
    try {
      const response = await fetch(`${this.baseURL}/api/categories`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi lấy danh sách categories: ${error.message}`);
    }
  }

  /**
   * Tạo category mới
   * @param {Object} categoryData - { name, description }
   */
  async createCategory(categoryData) {
    try {
      const response = await fetch(`${this.baseURL}/api/categories`, {
        method: 'POST',
        headers: this.getHeaders(),
        body: JSON.stringify(categoryData)
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi tạo danh mục: ${error.message}`);
    }
  }

  /**
   * Cập nhật category
   * @param {string} categoryId - ID category
   * @param {Object} categoryData - { name?, description? }
   */
  async updateCategory(categoryId, categoryData) {
    try {
      const response = await fetch(`${this.baseURL}/api/categories/${categoryId}`, {
        method: 'PUT',
        headers: this.getHeaders(),
        body: JSON.stringify(categoryData)
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi cập nhật danh mục: ${error.message}`);
    }
  }

  /**
   * Xóa category
   * @param {string} categoryId - ID category
   */
  async deleteCategory(categoryId) {
    try {
      const response = await fetch(`${this.baseURL}/api/categories/${categoryId}`, {
        method: 'DELETE',
        headers: this.getHeaders()
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi xóa danh mục: ${error.message}`);
    }
  }

  /**
   * Tạo sản phẩm mới
   * @param {FormData} formData - FormData chứa image, name, description, price, stock, category, colors, sizes
   */
  async createProduct(formData) {
    try {
      const headers = {};
      // Không set Content-Type cho FormData, browser sẽ tự động set với boundary
      if (this.token) {
        headers['Authorization'] = `Bearer ${this.token}`;
      }

      const response = await fetch(`${this.baseURL}/api/products`, {
        method: 'POST',
        headers: headers,
        body: formData
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi tạo sản phẩm: ${error.message}`);
    }
  }

  /**
   * Cập nhật sản phẩm
   * @param {string} productId - ID sản phẩm
   * @param {FormData} formData - FormData chứa các thông tin cập nhật
   */
  async updateProduct(productId, formData) {
    try {
      const headers = {};
      if (this.token) {
        headers['Authorization'] = `Bearer ${this.token}`;
      }

      const response = await fetch(`${this.baseURL}/api/products/${productId}`, {
        method: 'PUT',
        headers: headers,
        body: formData
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi cập nhật sản phẩm: ${error.message}`);
    }
  }

  /**
   * Xóa sản phẩm
   * @param {string} productId - ID sản phẩm
   */
  async deleteProduct(productId) {
    try {
      const response = await fetch(`${this.baseURL}/api/products/${productId}`, {
        method: 'DELETE',
        headers: this.getHeaders()
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi xóa sản phẩm: ${error.message}`);
    }
  }

  // ==================== QUẢN LÝ NGƯỜI DÙNG ====================

  /**
   * Lấy danh sách tất cả người dùng
   */
  async getAllUsers() {
    try {
      const response = await fetch(`${this.baseURL}/api/users/admin/all`, {
        method: 'GET',
        headers: this.getHeaders()
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi lấy danh sách người dùng: ${error.message}`);
    }
  }

  /**
   * Cập nhật role của người dùng
   * @param {string} userId - ID người dùng
   * @param {string} role - 'user' hoặc 'admin'
   */
  async updateUserRole(userId, role) {
    try {
      if (!['user', 'admin'].includes(role)) {
        throw new Error('Role không hợp lệ. Phải là "user" hoặc "admin"');
      }

      const response = await fetch(`${this.baseURL}/api/users/admin/${userId}/role`, {
        method: 'PUT',
        headers: this.getHeaders(),
        body: JSON.stringify({ role })
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi cập nhật role: ${error.message}`);
    }
  }

  /**
   * Xóa người dùng
   * @param {string} userId - ID người dùng
   */
  async deleteUser(userId) {
    try {
      const response = await fetch(`${this.baseURL}/api/users/admin/${userId}`, {
        method: 'DELETE',
        headers: this.getHeaders()
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi xóa người dùng: ${error.message}`);
    }
  }

  // ==================== QUẢN LÝ VOUCHER ====================

  /**
   * Lấy danh sách tất cả vouchers (bao gồm inactive)
   */
  async getAllVouchers() {
    try {
      const response = await fetch(`${this.baseURL}/api/vouchers/admin/all`, {
        method: 'GET',
        headers: this.getHeaders()
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi lấy danh sách vouchers: ${error.message}`);
    }
  }

  /**
   * Tạo voucher mới
   * @param {Object} voucherData - Dữ liệu voucher
   */
  async createVoucher(voucherData) {
    try {
      const response = await fetch(`${this.baseURL}/api/vouchers`, {
        method: 'POST',
        headers: this.getHeaders(),
        body: JSON.stringify(voucherData)
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi tạo voucher: ${error.message}`);
    }
  }

  /**
   * Cập nhật voucher
   * @param {string} voucherId - ID voucher
   * @param {Object} voucherData - Dữ liệu voucher cập nhật
   */
  async updateVoucher(voucherId, voucherData) {
    try {
      const response = await fetch(`${this.baseURL}/api/vouchers/${voucherId}`, {
        method: 'PUT',
        headers: this.getHeaders(),
        body: JSON.stringify(voucherData)
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi cập nhật voucher: ${error.message}`);
    }
  }

  /**
   * Xóa voucher
   * @param {string} voucherId - ID voucher
   */
  async deleteVoucher(voucherId) {
    try {
      const response = await fetch(`${this.baseURL}/api/vouchers/${voucherId}`, {
        method: 'DELETE',
        headers: this.getHeaders()
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi xóa voucher: ${error.message}`);
    }
  }

  // ==================== QUẢN LÝ ĐÁNH GIÁ ====================

  /**
   * Lấy danh sách đánh giá (admin)
   * @param {string} productId - ID sản phẩm (optional)
   * @param {string} userId - ID người dùng (optional)
   * @param {number} rating - Điểm đánh giá (optional)
   */
  async getAdminReviews(productId = null, userId = null, rating = null) {
    try {
      let url = `${this.baseURL}/api/reviews/admin/manage?`;
      if (productId) url += `product=${encodeURIComponent(productId)}&`;
      if (userId) url += `user=${encodeURIComponent(userId)}&`;
      if (rating) url += `rating=${rating}&`;
      url = url.replace(/&$/, '');

      const response = await fetch(url, {
        method: 'GET',
        headers: this.getHeaders()
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi lấy danh sách đánh giá: ${error.message}`);
    }
  }

  /**
   * Phản hồi đánh giá
   * @param {string} reviewId - ID đánh giá
   * @param {string} reply - Nội dung phản hồi
   */
  async replyReview(reviewId, reply) {
    try {
      const response = await fetch(`${this.baseURL}/api/reviews/${reviewId}/reply`, {
        method: 'POST',
        headers: this.getHeaders(),
        body: JSON.stringify({ reply })
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi phản hồi đánh giá: ${error.message}`);
    }
  }

  /**
   * Toggle ẩn/hiện đánh giá
   * @param {string} reviewId - ID đánh giá
   */
  async toggleReviewVisibility(reviewId) {
    try {
      const url = `${this.baseURL}/api/reviews/${reviewId}/toggle-visibility`;
      console.log('🔄 Toggling review visibility:', url);
      
      const response = await fetch(url, {
        method: 'PUT',
        headers: this.getHeaders()
      });

      // Kiểm tra nếu response không phải JSON
      const contentType = response.headers.get('content-type');
      if (!contentType || !contentType.includes('application/json')) {
        const text = await response.text();
        console.error('❌ Server returned non-JSON response:', text.substring(0, 200));
        throw new Error(`Server trả về lỗi: ${response.status} ${response.statusText}`);
      }

      return await this.handleResponse(response);
    } catch (error) {
      console.error('❌ Error toggling review visibility:', error);
      throw new Error(`Lỗi ẩn/hiện đánh giá: ${error.message}`);
    }
  }

  /**
   * Xóa đánh giá
   * @param {string} reviewId - ID đánh giá
   */
  async deleteReview(reviewId) {
    try {
      const response = await fetch(`${this.baseURL}/api/reviews/${reviewId}`, {
        method: 'DELETE',
        headers: this.getHeaders()
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi xóa đánh giá: ${error.message}`);
    }
  }

  // ==================== QUẢN LÝ ĐƠN HÀNG ====================

  /**
   * Lấy danh sách tất cả đơn hàng
   * @param {string} status - Trạng thái đơn hàng (optional): 'pending', 'processing', 'shipped', 'delivered', 'cancelled'
   */
  async getAllOrders(status = null) {
    try {
      let url = `${this.baseURL}/api/orders/admin/all`;
      if (status) {
        url += `?status=${encodeURIComponent(status)}`;
      }

      const response = await fetch(url, {
        method: 'GET',
        headers: this.getHeaders()
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi lấy danh sách đơn hàng: ${error.message}`);
    }
  }

  /**
   * Cập nhật trạng thái đơn hàng
   * @param {string} orderId - ID đơn hàng
   * @param {string} status - Trạng thái mới: 'pending', 'shipped', 'delivered' (bỏ processing và cancelled)
   */
  async updateOrderStatus(orderId, status) {
    try {
      // Chỉ cho phép các trạng thái hợp lệ theo backend
      const validStatuses = ['pending', 'shipped', 'delivered'];
      if (!validStatuses.includes(status)) {
        throw new Error(`Trạng thái không hợp lệ. Chỉ cho phép: Chờ xác nhận (pending), Đang giao (shipped), Hoàn thành (delivered)`);
      }

      const response = await fetch(`${this.baseURL}/api/orders/${orderId}/status`, {
        method: 'PUT',
        headers: this.getHeaders(),
        body: JSON.stringify({ status })
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi cập nhật trạng thái đơn hàng: ${error.message}`);
    }
  }

  /**
   * Cập nhật toàn bộ thông tin đơn hàng
   * @param {string} orderId - ID đơn hàng
   * @param {Object} orderData - Dữ liệu đơn hàng cập nhật
   */
  async updateOrder(orderId, orderData) {
    try {
      const response = await fetch(`${this.baseURL}/api/orders/${orderId}`, {
        method: 'PUT',
        headers: this.getHeaders(),
        body: JSON.stringify(orderData)
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi cập nhật đơn hàng: ${error.message}`);
    }
  }

  // ==================== THỐNG KÊ ====================

  /**
   * Lấy thống kê doanh thu
   * @param {string} startDate - Ngày bắt đầu (format: YYYY-M-D, ví dụ: 2025-1-4)
   * @param {string} endDate - Ngày kết thúc (format: YYYY-M-D, ví dụ: 2025-1-31)
   * @returns {Promise<Object>} { totalOrders, totalRevenue, categoryRevenue: [{ categoryId, categoryName, revenue }] }
   */
  async getRevenue(startDate, endDate) {
    try {
      if (!startDate || !endDate) {
        throw new Error('Vui lòng cung cấp đầy đủ ngày bắt đầu và ngày kết thúc');
      }

      const url = `${this.baseURL}/api/orders/revenue?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`;
      
      const response = await fetch(url, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      });

      return await this.handleResponse(response);
    } catch (error) {
      throw new Error(`Lỗi lấy thống kê doanh thu: ${error.message}`);
    }
  }

  // ==================== UTILITY FUNCTIONS ====================

  /**
   * Kiểm tra quyền admin
   * @param {Object} user - Thông tin user
   * @returns {boolean}
   */
  static isAdmin(user) {
    return user && user.role === 'admin';
  }

  /**
   * Format tiền tệ
   * @param {number} amount - Số tiền
   * @returns {string}
   */
  static formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  }

  /**
   * Format số tiền đơn giản (không có ký hiệu VND)
   * @param {number} amount - Số tiền
   * @returns {string}
   */
  static formatMoney(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount);
  }

  /**
   * Format ngày tháng
   * @param {string|Date} date - Ngày tháng
   * @param {string} format - Format (default: 'dd/MM/yyyy')
   * @returns {string}
   */
  static formatDate(date, format = 'dd/MM/yyyy') {
    const d = new Date(date);
    if (isNaN(d.getTime())) return date;

    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');

    return format
      .replace('dd', day)
      .replace('MM', month)
      .replace('yyyy', year)
      .replace('HH', hours)
      .replace('mm', minutes);
  }

  /**
   * Tạo FormData cho sản phẩm
   * @param {Object} productData - Dữ liệu sản phẩm
   * @param {File} imageFile - File ảnh (optional)
   * @returns {FormData}
   */
  static createProductFormData(productData, imageFile = null) {
    const formData = new FormData();
    
    if (imageFile) {
      formData.append('image', imageFile);
    }
    
    formData.append('name', productData.name || '');
    formData.append('description', productData.description || '');
    formData.append('price', String(productData.price || 0));
    formData.append('stock', String(productData.stock || 0));
    formData.append('sold', String(productData.sold || 0));
    formData.append('category', productData.category || '');
    
    // Format colors và sizes thành JSON array
    const colors = Array.isArray(productData.colors) 
      ? productData.colors 
      : (productData.colors ? productData.colors.split(',').map(c => c.trim()).filter(c => c) : []);
    const sizes = Array.isArray(productData.sizes) 
      ? productData.sizes 
      : (productData.sizes ? productData.sizes.split(',').map(s => s.trim()).filter(s => s) : []);
    
    formData.append('colors', JSON.stringify(colors));
    formData.append('sizes', JSON.stringify(sizes));
    formData.append('variants', JSON.stringify(productData.variants || []));

    return formData;
  }

  /**
   * Validate voucher data
   * @param {Object} voucherData - Dữ liệu voucher
   * @returns {Object} { valid: boolean, errors: string[] }
   */
  static validateVoucher(voucherData) {
    const errors = [];

    if (!voucherData.code || voucherData.code.trim() === '') {
      errors.push('Mã voucher không được để trống');
    }

    if (!voucherData.name || voucherData.name.trim() === '') {
      errors.push('Tên voucher không được để trống');
    }

    if (!voucherData.discountType || !['percentage', 'fixed'].includes(voucherData.discountType)) {
      errors.push('Loại giảm giá không hợp lệ');
    }

    if (voucherData.discountValue === undefined || voucherData.discountValue === null) {
      errors.push('Giá trị giảm giá không được để trống');
    }

    if (!voucherData.startDate) {
      errors.push('Ngày bắt đầu không được để trống');
    }

    if (!voucherData.endDate) {
      errors.push('Ngày kết thúc không được để trống');
    }

    if (voucherData.startDate && voucherData.endDate) {
      const start = new Date(voucherData.startDate);
      const end = new Date(voucherData.endDate);
      if (end < start) {
        errors.push('Ngày kết thúc phải sau ngày bắt đầu');
      }
    }

    return {
      valid: errors.length === 0,
      errors
    };
  }
}

// Export cho Node.js
if (typeof module !== 'undefined' && module.exports) {
  module.exports = AdminClient;
}

// Export cho browser
if (typeof window !== 'undefined') {
  window.AdminClient = AdminClient;
}

