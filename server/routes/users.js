const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const User = require('../models/User');
const Voucher = require('../models/Voucher');
const { verifyToken, requireAdmin } = require('../middleware/auth');

const router = express.Router();
const JWT_SECRET = process.env.JWT_SECRET || 'secret_key';

function isValidEmail(email) {
  const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
  return emailPattern.test(email);
}

/**
 * Đăng ký người dùng mới
 */
router.post('/register', async (req, res) => {
  try {
    const { name, email, password } = req.body;

    if (!name || !email || !password) {
      return res.status(400).json({ message: 'Vui lòng nhập đủ tên, email và mật khẩu' });
    }

    if (!isValidEmail(email)) {
      return res.status(400).json({ message: 'Email không đúng định dạng' });
    }

    const existingUser = await User.findOne({ email });
    if (existingUser) {
      return res.status(409).json({ message: 'Email đã được sử dụng' });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const newUser = await User.create({ name, email, password: hashedPassword });

    try {
      const voucherCode = `WELCOME${newUser._id.toString().substring(0, 8).toUpperCase()}`;
      const now = new Date();
      const foreverDate = new Date('2099-12-31T23:59:59.999Z');

      await Voucher.create({
        code: voucherCode,
        name: 'Voucher chào mừng',
        description: 'Giảm 50% cho khách hàng mới',
        discountType: 'percentage',
        discountValue: 50,
        minPurchaseAmount: 0,
        maxDiscountAmount: null,
        startDate: now,
        endDate: foreverDate,
        usageLimit: null,
        usedCount: 0,
        isActive: true,
        user: newUser._id
      });
    } catch (voucherError) {
    }

    res.status(201).json({
      message: 'Đăng ký thành công',
      user: {
        id: newUser._id,
        name: newUser.name,
        email: newUser.email,
        role: newUser.role,
        avatar: newUser.avatar || null
      }
    });
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Đăng nhập
 */
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ message: 'Vui lòng nhập email và mật khẩu' });
    }

    if (!isValidEmail(email)) {
      return res.status(400).json({ message: 'Email không đúng định dạng' });
    }

    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ message: 'Không tìm thấy người dùng' });
    }

    if (user.isLocked) {
      return res.status(403).json({ message: 'Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.' });
    }

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res.status(401).json({ message: 'Sai mật khẩu' });
    }

    const tokenPayload = {
      id: user._id,
      role: user.role || 'user'
    };
    const token = jwt.sign(tokenPayload, JWT_SECRET, { expiresIn: '24h' });

    res.json({
      message: 'Đăng nhập thành công',
      token,
      user: {
        id: user._id,
        name: user.name,
        email: user.email,
        role: user.role,
        avatar: user.avatar || null
      }
    });
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Lấy thông tin profile của user hiện tại
 */
router.get('/profile', verifyToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const user = await User.findById(userId).select('-password');
    
    if (!user) {
      return res.status(404).json({ message: 'Không tìm thấy người dùng' });
    }
    
    res.json({
      user: {
        id: user._id,
        name: user.name,
        email: user.email,
        role: user.role,
        avatar: user.avatar || null
      }
    });
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Cập nhật thông tin profile
 */
router.put('/profile', verifyToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { name, avatar } = req.body;

    const updateData = {};
    if (name !== undefined && name !== null) {
      updateData.name = name;
    }
    if (avatar !== undefined && avatar !== null) {
      updateData.avatar = avatar;
    }
    updateData.updatedAt = new Date();

    if (Object.keys(updateData).length === 0) {
      return res.status(400).json({ message: 'Không có dữ liệu để cập nhật' });
    }

    const updatedUser = await User.findByIdAndUpdate(
      userId,
      { $set: updateData },
      { new: true, runValidators: true }
    );

    if (!updatedUser) {
      return res.status(404).json({ message: 'Không tìm thấy người dùng' });
    }

    res.json({
      message: 'Cập nhật profile thành công',
      user: {
        id: updatedUser._id,
        name: updatedUser.name,
        email: updatedUser.email,
        role: updatedUser.role,
        avatar: updatedUser.avatar || null
      }
    });
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Admin lấy tất cả users
 */
router.get('/admin/all', verifyToken, requireAdmin, async (req, res) => {
  try {
    const users = await User.find().select('-password').sort({ createdAt: -1 });
    res.json(users.map(user => ({
      id: user._id,
      name: user.name,
      email: user.email,
      role: user.role,
      avatar: user.avatar || null,
      isLocked: user.isLocked || false,
      createdAt: user.createdAt
    })));
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Admin cập nhật role của user
 */
router.put('/admin/:id/role', verifyToken, requireAdmin, async (req, res) => {
  try {
    const userId = req.params.id;
    const { role } = req.body;

    if (!role || !['user', 'admin'].includes(role)) {
      return res.status(400).json({ message: 'Role không hợp lệ' });
    }

    const user = await User.findById(userId);

    if (!user) {
      return res.status(404).json({ message: 'Không tìm thấy người dùng' });
    }

    if (user.role === 'admin' && role === 'user') {
      return res.status(400).json({ message: 'Không thể đổi tài khoản admin thành người dùng' });
    }

    const updatedUser = await User.findByIdAndUpdate(
      userId,
      { $set: { role: role, updatedAt: new Date() } },
      { new: true }
    );

    res.json({
      message: 'Đã cập nhật role thành công',
      user: {
        id: updatedUser._id,
        name: updatedUser.name,
        email: updatedUser.email,
        role: updatedUser.role,
        avatar: updatedUser.avatar || null
      }
    });
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Admin khóa/mở khóa tài khoản user
 */
router.delete('/admin/:id', verifyToken, requireAdmin, async (req, res) => {
  try {
    const userId = req.params.id;
    const user = await User.findById(userId);

    if (!user) {
      return res.status(404).json({ message: 'Không tìm thấy người dùng' });
    }

    if (user.role === 'admin') {
      return res.status(400).json({ message: 'Không thể khóa tài khoản admin' });
    }

    if (user._id.toString() === req.user.id) {
      return res.status(400).json({ message: 'Bạn không thể khóa chính tài khoản của mình' });
    }

    user.isLocked = !user.isLocked;
    user.updatedAt = new Date();
    await user.save();

    const status = user.isLocked ? "khóa" : "mở khóa";
    res.json({ 
      message: `Đã ${status} tài khoản thành công`,
      user: {
        id: user._id,
        name: user.name,
        email: user.email,
        role: user.role,
        isLocked: user.isLocked
      }
    });
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

module.exports = router;