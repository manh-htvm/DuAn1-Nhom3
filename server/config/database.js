const mongoose = require('mongoose');

const connectDB = async () => {
  try {
    const conn = await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/duan1');

    console.log(`Kết nối thành công`);
    console.log(`Database: ${conn.connection.name}`);
  } catch (error) {
    console.error(`Kết nối thất bại`);
    console.error(`Error: ${error.message}`);
    process.exit(1);
  }
};

module.exports = connectDB;

