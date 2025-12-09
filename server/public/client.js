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
        headers: { 'Content-Type': 'application/json' }
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

// ==================== QUẢN LÝ VOUCHER ====================

/**
 * Lấy danh sách tất cả vouchers (bao gồm inactive)
 */
async getAllVouchers() {
  try {
    const response = await fetch(`${this.baseURL}/api/vouchers/admin/all`, {
      method: 'GET',
      headers: this.getHeaders() // Yêu cầu token
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
      headers: this.getHeaders(), // Yêu cầu token
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
      headers: this.getHeaders(), // Yêu cầu token
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
      headers: this.getHeaders() // Yêu cầu token
    });

    return await this.handleResponse(response);
  } catch (error) {
    throw new Error(`Lỗi xóa voucher: ${error.message}`);
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
      headers: this.getHeaders() // Yêu cầu token
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
      headers: this.getHeaders(), // Yêu cầu token
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
      headers: this.getHeaders(), // Yêu cầu token
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
      headers: this.getHeaders() // Yêu cầu token
    });

    return await this.handleResponse(response);
  } catch (error) {
    throw new Error(`Lỗi xóa voucher: ${error.message}`);
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
}