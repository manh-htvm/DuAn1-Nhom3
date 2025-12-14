require('dotenv').config();
const express = require('express');
const connectDB = require('./config/database');

const app = express();

connectDB();

app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, PATCH, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
  
  if (req.method === 'OPTIONS') {
    return res.sendStatus(200);
  }
  next();
});

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use('/uploads', express.static('uploads'));

app.use(express.static('public'));
const usersRoutes = require('./routes/users');
const productsRoutes = require('./routes/products');
const categoriesRoutes = require('./routes/categories');
const ordersRoutes = require('./routes/orders');
const reviewsRoutes = require('./routes/reviews');
const cartRoutes = require('./routes/cart');
const vouchersRoutes = require('./routes/vouchers');
const uploadRoutes = require('./routes/upload');

app.use('/api/users', usersRoutes);
app.use('/api/products', productsRoutes);
app.use('/api/categories', categoriesRoutes);
app.use('/api/orders', ordersRoutes);
app.use('/api/reviews', reviewsRoutes);
app.use('/api/cart', cartRoutes);
app.use('/api/vouchers', vouchersRoutes);
app.use('/api/upload', uploadRoutes);

app.get('/', (req, res) => {
  res.json({ 
    message: 'Server đang chạy!',
    database: 'duan1',
    collections: ['users', 'products', 'categories', 'orders', 'reviews', 'vouchers', 'cart'],
    apiEndpoints: {
      users: '/api/users',
      products: '/api/products',
      categories: '/api/categories',
      orders: '/api/orders',
      reviews: '/api/reviews',
      vouchers: '/api/vouchers',
      upload: '/api/upload',
      cart: '/api/cart'
    }
  });
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`Server đang chạy trên port ${PORT}`);
});

