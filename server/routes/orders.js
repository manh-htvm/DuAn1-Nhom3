const express = require("express");
const mongoose = require("mongoose");
const Order = require("../models/Order");
const router = express.Router();

// 1️⃣ TẠO ĐƠN HÀNG (dùng ID product thật từ client)
router.post("/", async (req, res) => {
  try {
    const { userId, items, totalAmount, shippingAddress, paymentMethod } = req.body;

    if (!userId || !items || items.length === 0) {
      return res.status(400).json({ message: "Thiếu thông tin đơn hàng" });
    }

    if (!mongoose.Types.ObjectId.isValid(userId)) {
      return res.status(400).json({ message: "UserId không hợp lệ" });
    }

    // Chuyển items.product sang ObjectId, bắt buộc phải đúng product collection
    const formattedItems = [];
    for (const item of items) {
      if (!item.product || !mongoose.Types.ObjectId.isValid(item.product)) {
        return res.status(400).json({ message: `ProductId không hợp lệ: ${item.product}` });
      }
      formattedItems.push({
        ...item,
        product: new mongoose.Types.ObjectId(item.product),
      });
    }

    const newOrder = await Order.create({
      user: new mongoose.Types.ObjectId(userId),
      items: formattedItems,
      totalAmount,
      shippingAddress: shippingAddress || {},
      paymentMethod: paymentMethod || "COD",
      status: "pending",
    });

    res.status(201).json({
      message: "Đặt hàng thành công",
      order: newOrder,
    });
  } catch (err) {
    res.status(500).json({ message: "Lỗi tạo đơn hàng", error: err.message });
  }
});

// 2️⃣ Lấy lịch sử đơn hàng theo user
router.get("/:userId", async (req, res) => {
  try {
    const { userId } = req.params;

    if (!mongoose.Types.ObjectId.isValid(userId)) {
      return res.status(400).json({ message: "UserId không hợp lệ" });
    }

    const orders = await Order.find({ user: userId })
      .sort({ createdAt: -1 })
      .populate("user", "name email")
      .populate("items.product", "name price image");

    res.json(orders);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
