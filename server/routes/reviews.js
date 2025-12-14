const express = require('express');
const mongoose = require('mongoose');
const Review = require('../models/Review');
const { verifyToken, requireAdmin } = require('../middleware/auth');

const router = express.Router();

/**
 * Tạo đánh giá mới
 */
router.post('/', verifyToken, async (req, res) => {
  try {
    console.log('📥 Received review request:', {
      body: req.body,
      userId: req.user?.id
    });

    const productId = req.body.productId || req.body.product;
    const rating = req.body.rating;
    const comment = req.body.comment;

    console.log('📋 Parsed data:', { productId, rating, comment, ratingType: typeof rating });

    if (!productId || productId.trim() === '') {
      console.error('❌ Missing or empty productId');
      return res.status(400).json({ 
        message: 'Vui lòng nhập đủ thông tin: productId và rating',
        error: 'productId is required'
      });
    }

    if (rating === undefined || rating === null) {
      console.error('❌ Missing rating');
      return res.status(400).json({ 
        message: 'Vui lòng nhập đủ thông tin: productId và rating',
        error: 'rating is required'
      });
    }

    const userId = req.user.id;
    const ratingNum = Number(rating);

    if (isNaN(ratingNum) || ratingNum < 1 || ratingNum > 5) {
      console.error('❌ Invalid rating:', ratingNum);
      return res.status(400).json({ 
        message: 'Rating phải từ 1 đến 5',
        error: 'rating must be between 1 and 5'
      });
    }

    const Order = require('../models/Order');
    const hasPurchased = await Order.findOne({
      user: userId,
      'items.product': productId,
      paymentStatus: 'paid'
    });

    if (!hasPurchased) {
      console.log('❌ User chưa mua sản phẩm này');
      return res.status(403).json({ 
        message: 'Bạn chỉ có thể đánh giá sản phẩm sau khi đã mua',
        error: 'Product not purchased'
      });
    }

    const existingReview = await Review.findOne({ user: userId, product: productId });
    if (existingReview) {
      return res.status(409).json({ message: 'Bạn đã đánh giá sản phẩm này rồi' });
    }

    const review = await Review.create({
      user: userId,
      product: productId,
      rating: ratingNum,
      comment: comment || '',
      createdAt: new Date(),
      updatedAt: new Date()
    });

    const populatedReview = await Review.findById(review._id)
      .populate('user', 'name email')
      .populate('product', 'name image');

    console.log('✅ Review đã được lưu vào MongoDB:', {
      reviewId: review._id,
      userId: userId,
      productId: productId,
      rating: review.rating,
      comment: review.comment
    });

    res.status(201).json({
      message: 'Đánh giá thành công',
      review: populatedReview
    });
  } catch (error) {
    console.error('❌ Error creating review:', error);
    console.error('Error stack:', error.stack);
    res.status(400).json({ 
      message: error.message || 'Có lỗi xảy ra khi tạo đánh giá',
      error: error.message 
    });
  }
});

/**
 * Lấy danh sách đánh giá
 */
router.get('/', async (req, res) => {
  try {
    const { product, user } = req.query;
    const query = {};

    if (product) {
      query.product = product;
    }
    if (user) {
      query.user = user;
    }

    query.isHidden = { $ne: true };

    const reviews = await Review.find(query)
      .populate('user', 'name email')
      .populate('product', 'name image')
      .sort({ createdAt: -1 });

    res.json(reviews);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

/**
 * Admin quản lý đánh giá
 */
router.get('/admin/manage', verifyToken, requireAdmin, async (req, res) => {
  try {
    const { product, user, rating } = req.query;
    const query = {};

    if (product) query.product = product;
    if (user) query.user = user;
    if (rating) query.rating = Number(rating);

    const reviews = await Review.find(query)
      .populate('user', 'name email')
      .populate('product', 'name image')
      .sort({ createdAt: -1 });

    res.json(reviews);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

/**
 * Lấy rating trung bình và tổng số đánh giá theo productId
 */
router.get('/product/:productId/rating', async (req, res) => {
  try {
    const { productId } = req.params;
    console.log('========================================');
    console.log('📊 GET /reviews/product/:productId/rating');
    console.log('📊 Received productId:', productId);
    console.log('📊 ProductId type:', typeof productId);
    
    let query;
    try {
      const objectId = new mongoose.Types.ObjectId(productId);
      query = { product: objectId };
      console.log('✅ Using ObjectId query for rating:', objectId.toString());
    } catch (e) {
      query = { product: productId };
      console.log('⚠️ Using string query for rating:', productId);
      console.log('⚠️ Error converting to ObjectId:', e.message);
    }
    
    console.log('🔍 Rating query:', JSON.stringify(query));

    const reviewQuery = { ...query, isHidden: { $ne: true } };
    const reviews = await Review.find(reviewQuery);
    const totalReviews = reviews.length;
    
    console.log('📊 Found', totalReviews, 'reviews in MongoDB for productId:', productId);
    
    if (totalReviews === 0) {
      console.log('📊 No reviews found, returning 0.0 (0 đánh giá)');
      console.log('========================================');
      return res.json({
        averageRating: 0,
        totalReviews: 0
      });
    }

    const sumRating = reviews.reduce((sum, review) => {
      const rating = review.rating || 0;
      console.log('📊 Review rating:', rating);
      return sum + rating;
    }, 0);

    const averageRating = sumRating / totalReviews;
    
    console.log('📊 Rating calculated from MongoDB:');
    console.log('  - Total reviews:', totalReviews);
    console.log('  - Sum of ratings:', sumRating);
    console.log('  - Average rating:', averageRating.toFixed(2));
    
    const result = {
      averageRating: parseFloat(averageRating.toFixed(1)),
      totalReviews: totalReviews
    };
    
    console.log('📤 Sending rating response:', result);
    console.log('========================================');
    res.json(result);
  } catch (error) {
    console.error('❌❌❌ ERROR getting rating ❌❌❌');
    console.error('Error:', error.message);
    console.error('Stack:', error.stack);
    console.log('========================================');
    res.status(500).json({ error: error.message });
  }
});

/**
 * Lấy đánh giá theo productId
 */
router.get('/product/:productId', async (req, res) => {
  try {
    const { productId } = req.params;
    console.log('========================================');
    console.log('📥 GET /reviews/product/:productId');
    console.log('📥 Received productId:', productId);
    console.log('📥 ProductId type:', typeof productId);
    
    let query;
    try {
      const objectId = new mongoose.Types.ObjectId(productId);
      query = { product: objectId };
      console.log('✅ Using ObjectId query:', objectId.toString());
    } catch (e) {
      query = { product: productId };
      console.log('⚠️ Using string query:', productId);
      console.log('⚠️ Error converting to ObjectId:', e.message);
    }
    
    console.log('🔍 Query:', JSON.stringify(query));

    const reviewQuery = { ...query, isHidden: { $ne: true } };
    
    const reviews = await Review.find(reviewQuery)
      .populate('user', 'name email')
      .populate('product', 'name image')
      .sort({ createdAt: -1 });

    console.log('📋 Found', reviews.length, 'reviews for productId:', productId);
    
    if (reviews.length > 0) {
      console.log('📋 First review:', {
        id: reviews[0]._id,
        rating: reviews[0].rating,
        comment: reviews[0].comment,
        user: reviews[0].user ? reviews[0].user.name : 'null',
        product: reviews[0].product ? reviews[0].product.name : 'null'
      });
    }
    
    console.log('📤 Sending response with', reviews.length, 'reviews');
    res.json(reviews);
    console.log('========================================');
  } catch (error) {
    console.error('❌❌❌ ERROR getting reviews ❌❌❌');
    console.error('Error:', error.message);
    console.error('Stack:', error.stack);
    console.log('========================================');
    res.status(500).json({ error: error.message });
  }
});

/**
 * Admin toggle ẩn/hiện đánh giá
 */
router.put('/:id/toggle-visibility', verifyToken, requireAdmin, async (req, res) => {
  try {
    console.log('🔄 Toggle review visibility:', req.params.id);
    
    const review = await Review.findById(req.params.id);

    if (!review) {
      console.log('❌ Review not found:', req.params.id);
      return res.status(404).json({ message: 'Không tìm thấy đánh giá' });
    }

    const oldStatus = review.isHidden;
    review.isHidden = !review.isHidden;
    review.updatedAt = Date.now();
    await review.save();

    
    const populatedReview = await Review.findById(review._id)
      .populate('user', 'name email')
      .populate('product', 'name image');

    res.json(populatedReview);
  } catch (error) {
    console.error('❌ Error toggling review visibility:', error);
    res.status(400).json({ error: error.message });
  }
});

/**
 * Admin trả lời đánh giá
 */
router.post('/:id/reply', verifyToken, requireAdmin, async (req, res) => {
  try {
    const { reply } = req.body;

    if (!reply || reply.trim() === '') {
      return res.status(400).json({ message: 'Vui lòng nhập nội dung trả lời' });
    }

    const review = await Review.findByIdAndUpdate(
      req.params.id,
      { adminReply: reply.trim(), updatedAt: Date.now() },
      { new: true, runValidators: true }
    )
      .populate('user', 'name email')
      .populate('product', 'name image');

    if (!review) {
      return res.status(404).json({ message: 'Không tìm thấy đánh giá' });
    }

    res.json(review);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

/**
 * Lấy đánh giá theo ID
 */
router.get('/:id', async (req, res) => {
  try {
    const review = await Review.findById(req.params.id)
      .populate('user', 'name email')
      .populate('product', 'name image');

    if (!review) {
      return res.status(404).json({ message: 'Không tìm thấy đánh giá' });
    }

    res.json(review);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

/**
 * Cập nhật đánh giá
 */
router.put('/:id', verifyToken, requireAdmin, async (req, res) => {
  try {
    const { rating, comment } = req.body;
    const updateData = { updatedAt: Date.now() };

    if (rating !== undefined) {
      if (rating < 1 || rating > 5) {
        return res.status(400).json({ message: 'Rating phải từ 1 đến 5' });
      }
      updateData.rating = Number(rating);
    }

    if (comment !== undefined) {
      updateData.comment = comment;
    }

    const review = await Review.findByIdAndUpdate(
      req.params.id,
      updateData,
      { new: true, runValidators: true }
    )
      .populate('user', 'name email')
      .populate('product', 'name image');

    if (!review) {
      return res.status(404).json({ message: 'Không tìm thấy đánh giá' });
    }

    res.json(review);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

/**
 * Xóa đánh giá
 */
router.delete('/:id', verifyToken, requireAdmin, async (req, res) => {
  try {
    const review = await Review.findByIdAndDelete(req.params.id);

    if (!review) {
      return res.status(404).json({ message: 'Không tìm thấy đánh giá' });
    }

    res.json({ message: 'Xóa đánh giá thành công', review });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;

