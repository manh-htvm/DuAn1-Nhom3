const express = require('express');
const Voucher = require('../models/Voucher');
const { verifyToken, requireAdmin } = require('../middleware/auth');
const router = express.Router();

/**
 * Lấy danh sách vouchers active
 * - Nếu đã đăng nhập (có token): lấy cả vouchers của user và vouchers public
 * - Nếu chưa đăng nhập: chỉ lấy vouchers public
 */
router.get('/', async (req, res) => {
  try {
    const authHeader = req.headers.authorization;
    let userId = null;

    // Nếu có token, lấy userId
    if (authHeader && authHeader.startsWith('Bearer ')) {
      try {
        const jwt = require('jsonwebtoken');
        const JWT_SECRET = process.env.JWT_SECRET || 'secret_key';
        const token = authHeader.split(' ')[1];
        const decoded = jwt.verify(token, JWT_SECRET);
        userId = decoded.id;
      } catch (err) {
        // Token không hợp lệ, coi như chưa đăng nhập
      }
    }

    const now = new Date();
    const query = {
      isActive: true,
      startDate: { $lte: now },
      endDate: { $gte: now }
    };

    // Nếu có userId, lấy cả vouchers của user và vouchers public
    // Nếu không có, chỉ lấy vouchers public
    if (userId) {
      query.$or = [
        { user: userId },
        { user: null }
      ];
    } else {
      query.user = null;
    }

    const vouchers = await Voucher.find(query).sort({ createdAt: -1 });

    // Lọc vouchers còn lượt sử dụng
    const availableVouchers = vouchers.filter(voucher => {
      if (voucher.usageLimit && voucher.usedCount >= voucher.usageLimit) {
        return false;
      }
      return true;
    });

    res.json(availableVouchers);
  } catch (error) {
    res.status(500).json({ message: 'Không thể lấy danh sách vouchers', error: error.message });
  }
});

/**
 * Lấy danh sách vouchers (không yêu cầu đăng nhập - chỉ vouchers public)
 */
router.get('/public', async (req, res) => {
  try {
    const now = new Date();
    const vouchers = await Voucher.find({
      user: null, // Chỉ lấy vouchers public
      isActive: true,
      startDate: { $lte: now },
      endDate: { $gte: now }
    }).sort({ createdAt: -1 });

    // Lọc vouchers còn lượt sử dụng
    const availableVouchers = vouchers.filter(voucher => {
      if (voucher.usageLimit && voucher.usedCount >= voucher.usageLimit) {
        return false;
      }
      return true;
    });

    res.json(availableVouchers);
  } catch (error) {
    res.status(500).json({ message: 'Không thể lấy danh sách vouchers', error: error.message });
  }
});

/**
 * Admin tạo voucher mới
 * Body: { code, name, description, discountType, discountValue, minPurchaseAmount, maxDiscountAmount, startDate, endDate, usageLimit, user (optional) }
 */
router.post('/', verifyToken, requireAdmin, async (req, res) => {
  try {
    const {
      code,
      name,
      description,
      discountType,
      discountValue,
      minPurchaseAmount,
      maxDiscountAmount,
      startDate,
      endDate,
      usageLimit,
      userId // Optional: nếu có thì tạo voucher cho user cụ thể, nếu null thì voucher public
    } = req.body;

    if (!code || !name || !discountType || discountValue === undefined) {
      return res.status(400).json({ message: 'Thiếu thông tin bắt buộc' });
    }

    // Kiểm tra code đã tồn tại chưa
    const existingVoucher = await Voucher.findOne({ code: code.toUpperCase() });
    if (existingVoucher) {
      return res.status(409).json({ message: 'Mã voucher đã tồn tại' });
    }

    const voucher = await Voucher.create({
      code: code.toUpperCase(),
      name,
      description,
      discountType,
      discountValue: Number(discountValue),
      minPurchaseAmount: minPurchaseAmount ? Number(minPurchaseAmount) : 0,
      maxDiscountAmount: maxDiscountAmount ? Number(maxDiscountAmount) : null,
      startDate: startDate ? new Date(startDate) : new Date(),
      endDate: endDate ? new Date(endDate) : new Date('2099-12-31'),
      usageLimit: usageLimit ? Number(usageLimit) : null,
      usedCount: 0,
      isActive: true,
      user: userId || null // null = public, có giá trị = voucher của user
    });

    res.status(201).json(voucher);
  } catch (error) {
    res.status(500).json({ message: 'Không thể tạo voucher', error: error.message });
  }
});

module.exports = router;

