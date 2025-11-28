const express = require('express');
const Order = require('../models/Order');
const Cart = require('../models/Cart');
const { verifyToken } = require('../middleware/auth');
const router = express.Router();

/**
 * Tạo đơn hàng mới (thanh toán)
 * Headers: Authorization: Bearer <token>
 * Body: { phone, address, note?, voucherId? }
 */
router.post('/', verifyToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { phone, address, note, voucherId, items } = req.body;

    console.log('Order request:', { phone, address, note, voucherId, itemsCount: items ? items.length : 0 });

    if (!phone || !address) {
      return res.status(400).json({ message: 'Vui lòng nhập số điện thoại và địa chỉ' });
    }

    let orderItems = [];
    let totalAmount = 0;

    // Luôn sử dụng items từ request body (không phụ thuộc vào giỏ hàng)
    if (!items || !Array.isArray(items) || items.length === 0) {
      console.log('Error: No items in request');
      return res.status(400).json({ message: 'Vui lòng gửi danh sách sản phẩm' });
    }

    const Product = require('../models/Product');
    for (const item of items) {
      console.log('Processing item:', item);
      if (!item.productId || item.quantity === undefined || item.quantity === null || !item.price) {
        console.log('Error: Invalid item data:', item);
        return res.status(400).json({ message: 'Thông tin sản phẩm không hợp lệ: thiếu productId, quantity hoặc price' });
      }
      
      // Kiểm tra sản phẩm có tồn tại không
      const product = await Product.findById(item.productId);
      if (!product) {
        console.log('Error: Product not found:', item.productId);
        return res.status(400).json({ message: `Sản phẩm không tồn tại: ${item.productId}` });
      }

      const itemTotal = item.price * item.quantity;
      totalAmount += itemTotal;
      orderItems.push({
        product: item.productId,
        quantity: item.quantity,
        price: item.price,
        color: item.color || 'Mặc định',
        size: item.size || 'Free size'
      });
    }

    // Tính giảm giá nếu có voucher
    let discountAmount = 0;
    if (voucherId) {
      const Voucher = require('../models/Voucher');
      const voucher = await Voucher.findById(voucherId);
      if (voucher && voucher.isActive) {
        const now = new Date();
        if (voucher.startDate <= now && voucher.endDate >= now) {
          if (voucher.user === null || voucher.user.toString() === userId) {
            if (totalAmount >= voucher.minPurchaseAmount) {
              if (voucher.discountType === 'percentage') {
                discountAmount = totalAmount * voucher.discountValue / 100;
                if (voucher.maxDiscountAmount && discountAmount > voucher.maxDiscountAmount) {
                  discountAmount = voucher.maxDiscountAmount;
                }
              } else {
                discountAmount = voucher.discountValue;
                if (discountAmount > totalAmount) {
                  discountAmount = totalAmount;
                }
              }
            }
          }
        }
      }
    }

    const finalAmount = totalAmount - discountAmount;

    console.log('Creating order with data:', {
      user: userId,
      itemsCount: orderItems.length,
      totalAmount,
      discountAmount,
      finalAmount,
      phone,
      address
    });

    // Tạo đơn hàng và lưu vào MongoDB
    const order = await Order.create({
      user: userId,
      items: orderItems,
      totalAmount,
      discountAmount,
      finalAmount,
      phone,
      shippingAddress: address,
      note: note || '',
      voucher: voucherId || null,
      paymentStatus: 'paid', // Mặc định là đã thanh toán
      status: 'pending'
    });

    console.log('✅ Order đã được lưu vào MongoDB:', {
      orderId: order._id,
      userId: userId,
      itemsCount: order.items.length,
      totalAmount: order.totalAmount,
      finalAmount: order.finalAmount,
      paymentStatus: order.paymentStatus
    });

    // Xóa giỏ hàng sau khi tạo đơn hàng thành công
    try {
      await Cart.findOneAndDelete({ user: userId });
      console.log('✅ Cart đã được xóa sau khi tạo order');
    } catch (cartError) {
      console.log('⚠️ Lỗi khi xóa cart (không ảnh hưởng đến order):', cartError.message);
    }

    // Populate order để trả về đầy đủ thông tin (không populate category để tránh lỗi parse)
    const populatedOrder = await Order.findById(order._id)
      .populate({
        path: 'items.product',
        select: 'name price image stock colors sizes description', // Chỉ lấy các field cần thiết, không lấy category
        populate: false
      })
      .populate('voucher');

    console.log('✅ Order populated successfully, sending response');

    res.status(201).json({
      message: 'Đặt hàng thành công',
      order: populatedOrder
    });
  } catch (err) {
    console.error('❌ Error creating order:', err);
    console.error('Error stack:', err.stack);
    res.status(500).json({ 
      message: 'Lỗi server', 
      error: err.message,
      details: process.env.NODE_ENV === 'development' ? err.stack : undefined
    });
  }
});

/**
 * Lấy lịch sử đơn hàng của user
 * Headers: Authorization: Bearer <token>
 * Query: ?status=paid|unpaid (optional)
 */
router.get('/', verifyToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { status } = req.query;

    let query = { user: userId };
    if (status === 'paid' || status === 'unpaid') {
      query.paymentStatus = status;
    }

    const orders = await Order.find(query)
      .populate({
        path: 'items.product',
        select: 'name price image stock colors sizes description', // Chỉ lấy các field cần thiết, không lấy category
        populate: false
      })
      .populate('voucher')
      .sort({ createdAt: -1 });

    res.json(orders);
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Lấy chi tiết đơn hàng
 * Headers: Authorization: Bearer <token>
 */
router.get('/:id', verifyToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const orderId = req.params.id;

    const order = await Order.findOne({ _id: orderId, user: userId })
      .populate({
        path: 'items.product',
        select: 'name price image stock colors sizes description', // Chỉ lấy các field cần thiết, không lấy category
        populate: false
      })
      .populate('voucher');

    if (!order) {
      return res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
    }

    res.json(order);
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

module.exports = router;
