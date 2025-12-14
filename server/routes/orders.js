const express = require('express');
const Order = require('../models/Order');
const Cart = require('../models/Cart');
const Product = require('../models/Product');
const Voucher = require('../models/Voucher');
const Category = require('../models/Category');
const { verifyToken, requireAdmin } = require('../middleware/auth');
const router = express.Router();

/**
 * Tạo đơn hàng mới (thanh toán)
 */
router.post('/', verifyToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { receiverName, phone, address, note, voucherId, items } = req.body;

    console.log('Order request:', { receiverName, phone, address, note, voucherId, itemsCount: items ? items.length : 0 });

    if (!receiverName || !phone || !address) {
      return res.status(400).json({ message: 'Vui lòng nhập đầy đủ thông tin: tên người nhận, số điện thoại và địa chỉ' });
    }

    let orderItems = [];
    let totalAmount = 0;

    if (!items || !Array.isArray(items) || items.length === 0) {
      console.log('Error: No items in request');
      return res.status(400).json({ message: 'Vui lòng gửi danh sách sản phẩm' });
    }

    for (const item of items) {
      console.log('Processing item:', item);
      if (!item.productId || item.quantity === undefined || item.quantity === null || !item.price) {
        console.log('Error: Invalid item data:', item);
        return res.status(400).json({ message: 'Thông tin sản phẩm không hợp lệ: thiếu productId, quantity hoặc price' });
      }

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
        size: item.size || 'Free size',
        productName: product.name || '',
        productImage: product.image || '',
        productDescription: product.description || ''
      });
    }

    let discountAmount = 0;
    if (voucherId) {
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

    const order = await Order.create({
      user: userId,
      items: orderItems,
      totalAmount,
      discountAmount,
      finalAmount,
      receiverName,
      phone,
      shippingAddress: address,
      note: note || '',
      voucher: voucherId || null,
      paymentStatus: 'paid',
      status: 'pending'
    });

    for (const item of orderItems) {
      try {
        const quantity = Math.abs(item.quantity);

        const updatedVariant = await Product.findOneAndUpdate(
          {
            _id: item.product,
            "variants.color": item.color, 
            "variants.size": item.size    
          },
          {
            $inc: {
              "variants.$.stock": -quantity,
              "variants.$.sold": quantity,
              "stock": -quantity,
              "sold": quantity
            }
          },
          { new: true }
        );

        if (!updatedVariant) {
           await Product.findByIdAndUpdate(item.product, {
              $inc: {
                stock: -quantity,
                sold: quantity
              }
           });
           console.log(`✅ Đã cập nhật sản phẩm thường (không biến thể): ${item.product}`);
        } else {
           console.log(`✅ Đã cập nhật biến thể ${item.color}/${item.size} của sp ${item.product}`);
        }

      } catch (updateError) {
        console.error(`⚠️ Lỗi cập nhật kho hàng cho sp ${item.product}:`, updateError.message);
      }
    }

    try {
      await Cart.findOneAndDelete({ user: userId });
      console.log('✅ Cart đã được xóa sau khi tạo order');
    } catch (cartError) {
      console.log('⚠️ Lỗi khi xóa cart (không ảnh hưởng đến order):', cartError.message);
    }

    const populatedOrder = await Order.findById(order._id)
      .populate({
        path: 'items.product',
        select: 'name price image stock colors sizes description',
        populate: false
      })
      .populate('user', 'name email')
      .populate('voucher');

    const orderObj = populatedOrder.toObject({ getters: true, virtuals: false });

    if (orderObj.user) {
      if (typeof orderObj.user === 'object' && orderObj.user._id) {

        orderObj.user._id = orderObj.user._id.toString();
        orderObj.user.name = orderObj.user.name || null;
        orderObj.user.email = orderObj.user.email || null;
      } else {

        const userId = orderObj.user.toString ? orderObj.user.toString() : String(orderObj.user);
        orderObj.user = {
          _id: userId,
          name: null,
          email: null
        };
      }
    }

    if (orderObj.createdAt) {
      orderObj.createdAt = (orderObj.createdAt instanceof Date 
        ? orderObj.createdAt 
        : new Date(orderObj.createdAt)).toISOString();
    }
    if (orderObj.updatedAt) {
      orderObj.updatedAt = (orderObj.updatedAt instanceof Date 
        ? orderObj.updatedAt 
        : new Date(orderObj.updatedAt)).toISOString();
    }

    if (orderObj.items && Array.isArray(orderObj.items)) {
      orderObj.items = orderObj.items.map(item => {
        const itemObj = item.toObject ? item.toObject() : item;
        if (itemObj.product && typeof itemObj.product === 'object' && itemObj.product._id) {
          itemObj.product._id = itemObj.product._id.toString();
        }
        return itemObj;
      });
    }

    res.status(201).json({
      message: 'Đặt hàng thành công',
      order: orderObj
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
        select: 'name price image stock colors sizes description isActive',
        populate: false,

      })
      .populate('user', 'name email')
      .populate('voucher')
      .sort({ createdAt: -1 });

    const formattedOrders = orders.map(order => {
      const orderObj = order.toObject({ getters: true, virtuals: false });

      orderObj._id = orderObj._id.toString();

      if (orderObj.user) {
        if (typeof orderObj.user === 'object' && orderObj.user._id) {

          orderObj.user._id = orderObj.user._id.toString();
          orderObj.user.name = orderObj.user.name || null;
          orderObj.user.email = orderObj.user.email || null;
        } else {

          const userId = orderObj.user.toString ? orderObj.user.toString() : String(orderObj.user);
          orderObj.user = {
            _id: userId,
            name: null,
            email: null
          };
        }
      }

      if (orderObj.createdAt) {
        orderObj.createdAt = (orderObj.createdAt instanceof Date 
          ? orderObj.createdAt 
          : new Date(orderObj.createdAt)).toISOString();
      }
      if (orderObj.updatedAt) {
        orderObj.updatedAt = (orderObj.updatedAt instanceof Date 
          ? orderObj.updatedAt 
          : new Date(orderObj.updatedAt)).toISOString();
      }

      if (orderObj.items && Array.isArray(orderObj.items)) {
        orderObj.items = orderObj.items.map(item => {
          const itemObj = item.toObject ? item.toObject() : item;

          if (!itemObj.product || (itemObj.product && !itemObj.product.name)) {

            if (itemObj.productName) {
              itemObj.product = {
                _id: itemObj.product ? (typeof itemObj.product === 'object' ? itemObj.product._id?.toString() : itemObj.product.toString()) : null,
                name: itemObj.productName || 'Sản phẩm đã bị xóa',
                price: itemObj.price || 0,
                image: itemObj.productImage || '',
                description: itemObj.productDescription || '',
                stock: 0,
                colors: [],
                sizes: []
              };
            } else {

              itemObj.product = {
                _id: itemObj.product ? (typeof itemObj.product === 'object' ? itemObj.product._id?.toString() : itemObj.product.toString()) : null,
                name: 'Sản phẩm đã bị xóa',
                price: itemObj.price || 0,
                image: '',
                description: '',
                stock: 0,
                colors: [],
                sizes: []
              };
            }
          } else {

            if (itemObj.product && typeof itemObj.product === 'object' && itemObj.product._id) {
              itemObj.product._id = itemObj.product._id.toString();
            }

            itemObj.product.name = itemObj.product.name || itemObj.productName || 'Sản phẩm';
            itemObj.product.image = itemObj.product.image || itemObj.productImage || '';
            itemObj.product.description = itemObj.product.description || itemObj.productDescription || '';
          }
          
          return itemObj;
        });
      }
      
      return orderObj;
    });

    res.json(formattedOrders);
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Thống kê doanh thu
 */
router.get('/revenue', async (req, res) => {
    console.log('📊 Revenue API called:', { startDate: req.query.startDate, endDate: req.query.endDate });
    
    try {
        const { startDate, endDate } = req.query;

        let query = { 
            paymentStatus: 'paid',
            status: { $ne: 'cancelled' }
        };

        if (startDate && endDate) {
            try {

                const parseDate = (dateStr) => {
                    const parts = dateStr.split('-');
                    if (parts.length === 3) {
                        const year = parseInt(parts[0], 10);
                        const month = parseInt(parts[1], 10) - 1;
                        const day = parseInt(parts[2], 10);
                        const date = new Date(year, month, day);
                        if (isNaN(date.getTime())) {
                            throw new Error('Invalid date');
                        }
                        return date;
                    }
                    throw new Error('Invalid date format');
                };

                const startOfDay = parseDate(startDate);
                startOfDay.setHours(0, 0, 0, 0);

                const end = parseDate(endDate);
                const nextDay = new Date(end);
                nextDay.setDate(end.getDate() + 1);
                nextDay.setHours(0, 0, 0, 0);

                query.createdAt = {
                    $gte: startOfDay,
                    $lt: nextDay
                };

                console.log(`📅 Filtering orders from ${startOfDay.toISOString()} to before ${nextDay.toISOString()}`);
            } catch (dateError) {
                console.error('❌ Lỗi parse ngày:', dateError);
                                return res.status(400).json({ 
                    message: "Định dạng ngày không hợp lệ. Vui lòng sử dụng format: YYYY-M-D (ví dụ: 2025-1-4)",
                    error: dateError.message 
                });
            }
        } else {
            console.log('📊 Fetching all paid and non-cancelled orders (no date filter)');
        }

        const allCategories = await Category.find().select('_id name');
        console.log(`📋 Found ${allCategories.length} categories in system`);

        const orders = await Order.find(query)
            .populate({
                path: 'items.product',
                select: 'name category',
                populate: {
                    path: 'category',
                    select: 'name _id'
                }
            });
        
        console.log(`📦 Found ${orders.length} orders matching criteria`);

        const categoryRevenueMap = new Map();
        allCategories.forEach(cat => {
            categoryRevenueMap.set(cat._id.toString(), {
                categoryId: cat._id.toString(),
                categoryName: cat.name,
                revenue: 0
            });
        });

        let itemsWithoutCategory = 0;

        orders.forEach(order => {
            if (order.paymentStatus === 'paid' && order.status !== 'cancelled') {

                const orderTotalAmount = order.totalAmount || 0;

                const orderFinalAmount = order.finalAmount || 0;

                const discountRatio = orderTotalAmount > 0 ? (orderFinalAmount / orderTotalAmount) : 1;

                order.items.forEach(item => {
                    if (item.product) {

                        let categoryId = null;
                        
                        if (item.product.category) {

                            if (typeof item.product.category === 'object' && item.product.category._id) {
                                categoryId = item.product.category._id.toString();
                            } 

                            else if (item.product.category.toString) {
                                categoryId = item.product.category.toString();
                            }
                        } else {
                            itemsWithoutCategory++;
                        }

                        if (categoryId && categoryRevenueMap.has(categoryId)) {

                            const itemOriginalValue = (item.price || 0) * (item.quantity || 0);

                            const itemRevenue = itemOriginalValue * discountRatio;
                            
                            const existing = categoryRevenueMap.get(categoryId);
                            existing.revenue += itemRevenue;
                        }
                    }
                });
            }
        });

        if (itemsWithoutCategory > 0) {
            console.log(`⚠️ Warning: ${itemsWithoutCategory} items without category`);
        }

        const categoryRevenue = Array.from(categoryRevenueMap.values())
            .sort((a, b) => b.revenue - a.revenue);

        const totalRevenue = categoryRevenue.reduce((sum, cat) => sum + cat.revenue, 0);
        const totalOrders = orders.length;

        console.log(`✅ Revenue stats: ${totalOrders} orders, ${totalRevenue} total revenue, ${categoryRevenue.length} categories`);
        
        if (categoryRevenue.length > 0) {
            console.log('📊 Category Revenue Details:');
            categoryRevenue.forEach(cat => {
                console.log(`  - ${cat.categoryName} (${cat.categoryId}): ${cat.revenue}`);
            });
        } else {
            console.log('⚠️ No category revenue found!');
        }

        const Product = require('../models/Product');

        let productQuery = {};

        const allProducts = await Product.find({})
            .select('_id name sold price')
            .sort({ sold: -1 })
            .limit(10);

        const topProducts = await Promise.all(allProducts.map(async (product) => {

            let productRevenue = 0;
            let productQuantity = 0;
            
            orders.forEach(order => {
                if (order.paymentStatus === 'paid' && order.status !== 'cancelled') {
                    const orderTotalAmount = order.totalAmount || 0;
                    const orderFinalAmount = order.finalAmount || 0;
                    const discountRatio = orderTotalAmount > 0 ? (orderFinalAmount / orderTotalAmount) : 1;
                    
                    order.items.forEach(item => {
                        let itemProductId = null;
                        if (item.product) {
                            if (typeof item.product === 'object' && item.product._id) {
                                itemProductId = item.product._id.toString();
                            } else if (item.product.toString && typeof item.product.toString === 'function') {
                                itemProductId = item.product.toString();
                            } else {
                                itemProductId = String(item.product);
                            }
                        }
                        
                        const productIdStr = product._id.toString();
                        if (itemProductId === productIdStr) {
                            const quantity = item.quantity || 0;
                            const itemPrice = item.price || 0;
                            productQuantity += quantity;
                            productRevenue += (itemPrice * quantity) * discountRatio;
                        }
                    });
                }
            });
            
            return {
                productId: product._id.toString(),
                productName: product.name,
                quantity: product.sold || 0,
                revenue: productRevenue
            };
        }));

        topProducts.sort((a, b) => b.quantity - a.quantity);
        
        console.log(`📊 Top Products: Found ${topProducts.length} products (based on 'sold' field)`);
        if (topProducts.length > 0) {
            console.log('Top Products Details:');
            topProducts.forEach((p, index) => {
                console.log(`  ${index + 1}. ${p.productName} - Sold: ${p.quantity}, Revenue: ${p.revenue}`);
            });
        } else {
            console.log('⚠️ No top products found!');
        }

        res.status(200).json({
            totalOrders: totalOrders,
            totalRevenue: totalRevenue,
            categoryRevenue: categoryRevenue,
            topProducts: topProducts
        });

    } catch (err) {
        console.error('❌ Lỗi thống kê doanh thu:', err);
        res.status(500).json({ 
            message: "Lỗi server", 
            error: err.message 
        });
    }
});

/**
 * Lấy chi tiết đơn hàng
 */
router.get('/:id', verifyToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const orderId = req.params.id;

    const order = await Order.findOne({ _id: orderId, user: userId })
      .populate({
        path: 'items.product',
        select: 'name price image stock colors sizes description isActive',
        populate: false,

      })
      .populate('user', 'name email')
      .populate('voucher');

    if (!order) {
      return res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
    }

    const orderObj = order.toObject({ getters: true, virtuals: false });

    if (orderObj.user) {
      if (typeof orderObj.user === 'object' && orderObj.user._id) {

        orderObj.user._id = orderObj.user._id.toString();
        orderObj.user.name = orderObj.user.name || null;
        orderObj.user.email = orderObj.user.email || null;
      } else {

        const userId = orderObj.user.toString ? orderObj.user.toString() : String(orderObj.user);
        orderObj.user = {
          _id: userId,
          name: null,
          email: null
        };
      }
    }

    if (orderObj.createdAt) {
      orderObj.createdAt = (orderObj.createdAt instanceof Date 
        ? orderObj.createdAt 
        : new Date(orderObj.createdAt)).toISOString();
    }
    if (orderObj.updatedAt) {
      orderObj.updatedAt = (orderObj.updatedAt instanceof Date 
        ? orderObj.updatedAt 
        : new Date(orderObj.updatedAt)).toISOString();
    }

    if (orderObj.items && Array.isArray(orderObj.items)) {
      orderObj.items = orderObj.items.map(item => {
        const itemObj = item.toObject ? item.toObject() : item;

        if (!itemObj.product || (itemObj.product && !itemObj.product.name)) {

          if (itemObj.productName) {
            itemObj.product = {
              _id: itemObj.product ? (typeof itemObj.product === 'object' ? itemObj.product._id?.toString() : itemObj.product.toString()) : null,
              name: itemObj.productName || 'Sản phẩm đã bị xóa',
              price: itemObj.price || 0,
              image: itemObj.productImage || '',
              description: itemObj.productDescription || '',
              stock: 0,
              colors: [],
              sizes: []
            };
          } else {

            itemObj.product = {
              _id: itemObj.product ? (typeof itemObj.product === 'object' ? itemObj.product._id?.toString() : itemObj.product.toString()) : null,
              name: 'Sản phẩm đã bị xóa',
              price: itemObj.price || 0,
              image: '',
              description: '',
              stock: 0,
              colors: [],
              sizes: []
            };
          }
        } else {

          if (itemObj.product && typeof itemObj.product === 'object' && itemObj.product._id) {
            itemObj.product._id = itemObj.product._id.toString();
          }

          itemObj.product.name = itemObj.product.name || itemObj.productName || 'Sản phẩm';
          itemObj.product.image = itemObj.product.image || itemObj.productImage || '';
          itemObj.product.description = itemObj.product.description || itemObj.productDescription || '';
        }
        
        return itemObj;
      });
    }

    res.json(orderObj);
  } catch (err) {
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Admin lấy tất cả đơn hàng
 */
router.get('/admin/all', verifyToken, requireAdmin, async (req, res) => {
  try {
    console.log('📦 GET /api/orders/admin/all');
    console.log('📦 User ID:', req.user.id, ', Role:', req.user.role);
    const { status } = req.query;
    let query = {};
    
    if (status && ['pending', 'processing', 'shipped', 'delivered', 'cancelled'].includes(status)) {
      query.status = status;
      console.log('📦 Filtering by status:', status);
    } else {
      console.log('📦 No status filter - getting all orders');
    }

    const orders = await Order.find(query)
      .populate({
        path: 'items.product',
        select: 'name price image',
        populate: false
      })
      .populate('user', 'name email')
      .sort({ createdAt: -1 });

    console.log('📦 Found', orders.length, 'orders in database');
    
    const formattedOrders = orders.map(order => {
      const orderObj = order.toObject({ getters: true, virtuals: false });

      orderObj._id = orderObj._id.toString();

      if (orderObj.user) {
        if (typeof orderObj.user === 'object' && orderObj.user._id) {

          orderObj.user._id = orderObj.user._id.toString();
          orderObj.user.name = orderObj.user.name || null;
          orderObj.user.email = orderObj.user.email || null;
        } else {

          const userId = orderObj.user.toString ? orderObj.user.toString() : String(orderObj.user);
          orderObj.user = {
            _id: userId,
            name: null,
            email: null
          };
        }
      }

      if (orderObj.createdAt) {
        orderObj.createdAt = (orderObj.createdAt instanceof Date 
          ? orderObj.createdAt 
          : new Date(orderObj.createdAt)).toISOString();
      }
      if (orderObj.updatedAt) {
        orderObj.updatedAt = (orderObj.updatedAt instanceof Date 
          ? orderObj.updatedAt 
          : new Date(orderObj.updatedAt)).toISOString();
      }

      if (orderObj.items && Array.isArray(orderObj.items)) {
        orderObj.items = orderObj.items.map(item => {
          const itemObj = item.toObject ? item.toObject() : item;
          if (itemObj.product && typeof itemObj.product === 'object' && itemObj.product._id) {
            itemObj.product._id = itemObj.product._id.toString();
          }
          return itemObj;
        });
      }
      
      return orderObj;
    });

    console.log('📦 Returning', formattedOrders.length, 'formatted orders');
    if (formattedOrders.length > 0) {
      console.log('📦 Sample order (first order):');
      console.log(JSON.stringify(formattedOrders[0], null, 2));
    }
    res.json(formattedOrders);
  } catch (err) {
    console.error('❌ Error getting all orders:', err);
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Admin cập nhật trạng thái đơn hàng
 */
router.put('/:id/status', verifyToken, requireAdmin, async (req, res) => {
  try {
    const orderId = req.params.id;
    const { status } = req.body;

    if (!status || !['pending', 'shipped', 'delivered'].includes(status)) {
      return res.status(400).json({ message: 'Trạng thái không hợp lệ. Chỉ cho phép: Chờ xác nhận, Đang giao, Hoàn thành' });
    }

    const order = await Order.findById(orderId);
    if (!order) {
      return res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
    }

    if (order.status === 'delivered' || order.status === 'cancelled') {
      return res.status(400).json({ message: 'Không thể cập nhật đơn hàng đã hoàn thành hoặc đã hủy' });
    }

    if (order.status === 'pending' && status !== 'shipped') {
      return res.status(400).json({ message: 'Từ "Chờ xác nhận" chỉ có thể chuyển đến "Đang giao"' });
    }

    if (order.status === 'shipped' && status !== 'delivered') {
      return res.status(400).json({ message: 'Từ "Đang giao" chỉ có thể chuyển đến "Hoàn thành"' });
    }

    if (status === 'cancelled') {
      return res.status(400).json({ message: 'Không thể hủy đơn hàng từ đây. Người dùng sẽ tự hủy.' });
    }

    if (status === 'cancelled' && order.status !== 'cancelled') {
      for (const item of order.items) {
        try {
          const quantity = Math.abs(item.quantity);
          const updatedVariant = await Product.findOneAndUpdate(
            {
              _id: item.product,
              "variants.color": item.color,
              "variants.size": item.size
            },
            {
              $inc: {
                "variants.$.stock": quantity,
                "variants.$.sold": -quantity,
                "stock": quantity,
                "sold": -quantity
              }
            },
            { new: true }
          );

          if (!updatedVariant) {
            await Product.findByIdAndUpdate(item.product, {
              $inc: {
                stock: quantity,
                sold: -quantity
              }
            });
          }
        } catch (updateError) {
          console.error(`⚠️ Lỗi cập nhật kho hàng khi hủy đơn:`, updateError.message);
        }
      }
    }

    order.status = status;
    order.updatedAt = new Date();
    await order.save();

    const populatedOrder = await Order.findById(order._id)
      .populate({
        path: 'items.product',
        select: 'name price image stock colors sizes description',
        populate: false
      })
      .populate('user', 'name email')
      .populate('voucher');

    const orderObj = populatedOrder.toObject({ getters: true, virtuals: false });

    if (orderObj.user) {
      if (typeof orderObj.user === 'object' && orderObj.user._id) {
        orderObj.user._id = orderObj.user._id.toString();
        orderObj.user.name = orderObj.user.name || null;
        orderObj.user.email = orderObj.user.email || null;
      } else {
        const userId = orderObj.user.toString ? orderObj.user.toString() : String(orderObj.user);
        orderObj.user = {
          _id: userId,
          name: null,
          email: null
        };
      }
    }

    if (orderObj.createdAt) {
      orderObj.createdAt = (orderObj.createdAt instanceof Date 
        ? orderObj.createdAt 
        : new Date(orderObj.createdAt)).toISOString();
    }
    if (orderObj.updatedAt) {
      orderObj.updatedAt = (orderObj.updatedAt instanceof Date 
        ? orderObj.updatedAt 
        : new Date(orderObj.updatedAt)).toISOString();
    }

    if (orderObj.items && Array.isArray(orderObj.items)) {
      orderObj.items = orderObj.items.map(item => {
        const itemObj = item.toObject ? item.toObject() : item;
        if (itemObj.product && typeof itemObj.product === 'object' && itemObj.product._id) {
          itemObj.product._id = itemObj.product._id.toString();
        }
        return itemObj;
      });
    }

    res.json(orderObj);
  } catch (err) {
    console.error('❌ Error updating order status:', err);
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Admin cập nhật toàn bộ thông tin đơn hàng
 */
router.put('/:id', verifyToken, requireAdmin, async (req, res) => {
  try {
    const orderId = req.params.id;
    const { receiverName, phone, shippingAddress, note, status, paymentStatus } = req.body;

    const order = await Order.findById(orderId);
    if (!order) {
      return res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
    }

    if (status && !['pending', 'processing', 'shipped', 'delivered', 'cancelled'].includes(status)) {
      return res.status(400).json({ message: 'Trạng thái không hợp lệ' });
    }

    if (paymentStatus && !['paid', 'unpaid'].includes(paymentStatus)) {
      return res.status(400).json({ message: 'Trạng thái thanh toán không hợp lệ' });
    }

    const oldStatus = order.status;

    if (receiverName !== undefined) order.receiverName = receiverName;
    if (phone !== undefined) order.phone = phone;
    if (shippingAddress !== undefined) order.shippingAddress = shippingAddress;
    if (note !== undefined) order.note = note;
    if (status !== undefined) order.status = status;
    if (paymentStatus !== undefined) order.paymentStatus = paymentStatus;

    if (status === 'cancelled' && oldStatus !== 'cancelled') {
      for (const item of order.items) {
        try {
          const quantity = Math.abs(item.quantity);
          const updatedVariant = await Product.findOneAndUpdate(
            {
              _id: item.product,
              "variants.color": item.color,
              "variants.size": item.size
            },
            {
              $inc: {
                "variants.$.stock": quantity,
                "variants.$.sold": -quantity,
                "stock": quantity,
                "sold": -quantity
              }
            },
            { new: true }
          );

          if (!updatedVariant) {
            await Product.findByIdAndUpdate(item.product, {
              $inc: {
                stock: quantity,
                sold: -quantity
              }
            });
          }
        } catch (updateError) {
          console.error(`⚠️ Lỗi cập nhật kho hàng khi hủy đơn:`, updateError.message);
        }
      }
    }

    if (oldStatus === 'cancelled' && status && status !== 'cancelled') {
      for (const item of order.items) {
        try {
          const quantity = Math.abs(item.quantity);
          const updatedVariant = await Product.findOneAndUpdate(
            {
              _id: item.product,
              "variants.color": item.color,
              "variants.size": item.size
            },
            {
              $inc: {
                "variants.$.stock": -quantity,
                "variants.$.sold": quantity,
                "stock": -quantity,
                "sold": quantity
              }
            },
            { new: true }
          );

          if (!updatedVariant) {
            await Product.findByIdAndUpdate(item.product, {
              $inc: {
                stock: -quantity,
                sold: quantity
              }
            });
          }
        } catch (updateError) {
          console.error(`⚠️ Lỗi cập nhật kho hàng khi khôi phục đơn:`, updateError.message);
        }
      }
    }

    order.updatedAt = new Date();
    await order.save();

    const populatedOrder = await Order.findById(order._id)
      .populate({
        path: 'items.product',
        select: 'name price image stock colors sizes description',
        populate: false
      })
      .populate('user', 'name email')
      .populate('voucher');

    const orderObj = populatedOrder.toObject({ getters: true, virtuals: false });

    if (orderObj.user) {
      if (typeof orderObj.user === 'object' && orderObj.user._id) {
        orderObj.user._id = orderObj.user._id.toString();
        orderObj.user.name = orderObj.user.name || null;
        orderObj.user.email = orderObj.user.email || null;
      } else {
        const userId = orderObj.user.toString ? orderObj.user.toString() : String(orderObj.user);
        orderObj.user = {
          _id: userId,
          name: null,
          email: null
        };
      }
    }

    if (orderObj.createdAt) {
      orderObj.createdAt = (orderObj.createdAt instanceof Date 
        ? orderObj.createdAt 
        : new Date(orderObj.createdAt)).toISOString();
    }
    if (orderObj.updatedAt) {
      orderObj.updatedAt = (orderObj.updatedAt instanceof Date 
        ? orderObj.updatedAt 
        : new Date(orderObj.updatedAt)).toISOString();
    }

    if (orderObj.items && Array.isArray(orderObj.items)) {
      orderObj.items = orderObj.items.map(item => {
        const itemObj = item.toObject ? item.toObject() : item;
        if (itemObj.product && typeof itemObj.product === 'object' && itemObj.product._id) {
          itemObj.product._id = itemObj.product._id.toString();
        }
        return itemObj;
      });
    }

    res.json(orderObj);
  } catch (err) {
    console.error('❌ Error updating order:', err);
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

/**
 * Hủy đơn hàng
 */
router.put('/:id/cancel', verifyToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const orderId = req.params.id;
    const { cancelReason } = req.body;

    const order = await Order.findOne({ _id: orderId, user: userId });

    if (!order) {
      return res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
    }

    if (order.status !== 'pending') {
      return res.status(400).json({ message: 'Chỉ có thể hủy đơn hàng đang chờ xác nhận' });
    }

    for (const item of order.items) {
      try {
        const quantity = Math.abs(item.quantity);

        const updatedVariant = await Product.findOneAndUpdate(
          {
            _id: item.product,
            "variants.color": item.color,
            "variants.size": item.size
          },
          {
            $inc: {
              "variants.$.stock": quantity,
              "variants.$.sold": -quantity,
              "stock": quantity,
              "sold": -quantity
            }
          },
          { new: true }
        );

        if (!updatedVariant) {
          await Product.findByIdAndUpdate(item.product, {
            $inc: {
              stock: quantity,
              sold: -quantity
            }
          });
        }
      } catch (updateError) {
        console.error(`⚠️ Lỗi cập nhật kho hàng khi hủy đơn cho sp ${item.product}:`, updateError.message);
      }
    }

    order.status = 'cancelled';
    if (cancelReason && cancelReason.trim()) {
      order.cancelReason = cancelReason.trim();
    }
    order.updatedAt = new Date();
    await order.save();

    res.json({ message: 'Đã hủy đơn hàng thành công', order });
  } catch (err) {
    console.error('❌ Error cancelling order:', err);
    res.status(500).json({ message: 'Lỗi server', error: err.message });
  }
});

module.exports = router;
