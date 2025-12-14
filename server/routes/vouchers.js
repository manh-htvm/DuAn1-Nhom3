const express = require('express');
const Voucher = require('../models/Voucher');
const { verifyToken, requireAdmin } = require('../middleware/auth');
const router = express.Router();

/**
 * Lấy danh sách vouchers active
 */
router.get('/', async (req, res) => {
  try {
    const authHeader = req.headers.authorization;
    let userId = null;

    if (authHeader && authHeader.startsWith('Bearer ')) {
      try {
        const jwt = require('jsonwebtoken');
        const JWT_SECRET = process.env.JWT_SECRET || 'secret_key';
        const token = authHeader.split(' ')[1];
        const decoded = jwt.verify(token, JWT_SECRET);
        userId = decoded.id;
      } catch (err) {

      }
    }

    const now = new Date();
    const query = {
      isActive: true,
      startDate: { $lte: now },
      endDate: { $gte: now }
    };

    if (userId) {
      query.$or = [
        { user: userId },
        { user: null }
      ];
    } else {
      query.user = null;
    }

    const vouchers = await Voucher.find(query).sort({ createdAt: -1 });

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
 * Lấy danh sách vouchers public
 */
router.get('/public', async (req, res) => {
  try {
    const now = new Date();
    const vouchers = await Voucher.find({
      user: null,
      isActive: true,
      startDate: { $lte: now },
      endDate: { $gte: now }
    }).sort({ createdAt: -1 });

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
      userId
    } = req.body;

    if (!code || !name || !discountType || discountValue === undefined) {
      return res.status(400).json({ message: 'Thiếu thông tin bắt buộc' });
    }

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
      user: userId || null
    });

    res.status(201).json(voucher);
  } catch (error) {
    res.status(500).json({ message: 'Không thể tạo voucher', error: error.message });
  }
});

/**
 * Admin lấy tất cả vouchers
 */
router.get('/admin/all', verifyToken, requireAdmin, async (req, res) => {
  try {
    const vouchers = await Voucher.find().sort({ createdAt: -1 });
    res.json(vouchers);
  } catch (error) {
    res.status(500).json({ message: 'Không thể lấy danh sách vouchers', error: error.message });
  }
});

/**
 * Admin cập nhật voucher
 */
router.put('/:id', verifyToken, requireAdmin, async (req, res) => {
  try {
    const voucherId = req.params.id;
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
      isActive
    } = req.body;

    const voucher = await Voucher.findById(voucherId);
    if (!voucher) {
      return res.status(404).json({ message: 'Không tìm thấy voucher' });
    }

    if (code && code.toUpperCase() !== voucher.code) {
      const existingVoucher = await Voucher.findOne({ code: code.toUpperCase() });
      if (existingVoucher) {
        return res.status(409).json({ message: 'Mã voucher đã tồn tại' });
      }
      voucher.code = code.toUpperCase();
    }

    if (name) voucher.name = name;
    if (description !== undefined) voucher.description = description;
    if (discountType) voucher.discountType = discountType;
    if (discountValue !== undefined) voucher.discountValue = Number(discountValue);
    if (minPurchaseAmount !== undefined) voucher.minPurchaseAmount = Number(minPurchaseAmount);
    if (maxDiscountAmount !== undefined) voucher.maxDiscountAmount = maxDiscountAmount ? Number(maxDiscountAmount) : null;
    if (startDate) voucher.startDate = new Date(startDate);
    if (endDate) voucher.endDate = new Date(endDate);
    if (usageLimit !== undefined) voucher.usageLimit = usageLimit ? Number(usageLimit) : null;
    if (isActive !== undefined) voucher.isActive = isActive;
    voucher.updatedAt = new Date();

    await voucher.save();
    res.json(voucher);
  } catch (error) {
    res.status(500).json({ message: 'Không thể cập nhật voucher', error: error.message });
  }
});

/**
 * Admin xóa voucher
 */
router.delete('/:id', verifyToken, requireAdmin, async (req, res) => {
  try {
    const voucherId = req.params.id;
    const voucher = await Voucher.findByIdAndDelete(voucherId);
    
    if (!voucher) {
      return res.status(404).json({ message: 'Không tìm thấy voucher' });
    }

    res.json({ message: 'Đã xóa voucher thành công' });
  } catch (error) {
    res.status(500).json({ message: 'Không thể xóa voucher', error: error.message });
  }
});

module.exports = router;

