const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const User = require('../models/User'); 
const router = express.Router();

// Đăng ký tài khoản
router.post('/register', async (req, res) => {
  const { username, password } = req.body;

  try {
    // Kiểm tra username đã tồn tại
    const existingUser = await User.findOne({ username });
    if (existingUser) return res.status(409).json({ message: 'Username đã tồn tại' });

    // Mã hóa mật khẩu
    const hashedPassword = await bcrypt.hash(password, 10);

    // Tạo người dùng mới
    const newUser = new User({ username, password: hashedPassword });
    await newUser.save();

    res.status(201).json({ message: 'Đăng ký thành công' });
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

// Đăng nhập tài khoản
router.post('/login', async (req, res) => {
  const { username, password } = req.body;

  try {
    // Tìm người dùng
    const user = await User.findOne({ username });
    if (!user) return res.status(404).json({ message: 'Không tìm thấy người dùng' });

    // So sánh mật khẩu
    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) return res.status(401).json({ message: 'Sai mật khẩu' });

    // Tạo token
    const token = jwt.sign({ id: user._id }, 'secret_key', { expiresIn: '1h' });

    res.json({ message: 'Đăng nhập thành công', token });
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

module.exports = router;