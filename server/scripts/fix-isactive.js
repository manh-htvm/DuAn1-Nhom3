// Script để fix các sản phẩm có isActive = null hoặc undefined thành true
const mongoose = require('mongoose');
const Product = require('../models/Product');
require('dotenv').config();

async function fixIsActive() {
  try {
    // Kết nối database
    const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/toco';
    await mongoose.connect(mongoUri);
    console.log('✅ Đã kết nối database');

    // Tìm tất cả sản phẩm có isActive = null, undefined, hoặc không có field
    const products = await Product.find({
      $or: [
        { isActive: null },
        { isActive: { $exists: false } }
      ]
    });

    console.log(`📦 Tìm thấy ${products.length} sản phẩm cần cập nhật`);

    if (products.length === 0) {
      console.log('✅ Không có sản phẩm nào cần cập nhật');
      await mongoose.disconnect();
      return;
    }

    // Cập nhật tất cả thành isActive = true
    const result = await Product.updateMany(
      {
        $or: [
          { isActive: null },
          { isActive: { $exists: false } }
        ]
      },
      {
        $set: { isActive: true }
      }
    );

    console.log(`✅ Đã cập nhật ${result.modifiedCount} sản phẩm thành isActive = true`);
    console.log('✅ Hoàn thành!');

    await mongoose.disconnect();
  } catch (error) {
    console.error('❌ Lỗi:', error);
    process.exit(1);
  }
}

// Chạy script
fixIsActive();





