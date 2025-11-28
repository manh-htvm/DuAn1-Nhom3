const express = require("express");
const multer = require("multer");
const path = require("path");
const Product = require("../models/Product");

const router = express.Router();

const storage = multer.diskStorage({
  destination: function (_req, _file, cb) {
    cb(null, "uploads/");
  },
  filename: function (_req, file, cb) {
    const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1e9);
    cb(null, uniqueSuffix + path.extname(file.originalname));
  },
});

const fileFilter = (_req, file, cb) => {
  const allowedTypes = /jpeg|jpg|png|gif|webp/;
  const extname = allowedTypes.test(path.extname(file.originalname).toLowerCase());
  const mimetype = allowedTypes.test(file.mimetype);

  if (mimetype && extname) {
    cb(null, true);
  } else {
    cb(new Error("Chỉ cho phép upload file ảnh (jpeg, jpg, png, gif, webp)"));
  }
};

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter,
});

router.post("/", upload.single("image"), async (req, res) => {
  try {
    const imagePath = req.file ? `/uploads/${req.file.filename}` : req.body.image;
    const { name, description, price, stock, category, colors, sizes } = req.body;

    // Parse colors và sizes thành mảng
    let colorsArray = [];
    let sizesArray = [];

    if (colors) {
      if (typeof colors === 'string') {
        // Nếu là string JSON, parse nó
        try {
          colorsArray = JSON.parse(colors);
        } catch (e) {
          // Nếu không phải JSON, coi như là một giá trị đơn
          colorsArray = [colors];
        }
      } else if (Array.isArray(colors)) {
        colorsArray = colors;
      }
      // Lọc bỏ giá trị rỗng
      colorsArray = colorsArray.filter(c => c && c.trim() !== '');
    }

    if (sizes) {
      if (typeof sizes === 'string') {
        try {
          sizesArray = JSON.parse(sizes);
        } catch (e) {
          sizesArray = [sizes];
        }
      } else if (Array.isArray(sizes)) {
        sizesArray = sizes;
      }
      // Lọc bỏ giá trị rỗng
      sizesArray = sizesArray.filter(s => s && s.trim() !== '');
    }

    const product = await Product.create({
      name,
      description,
      price: Number(price),
      stock: Number(stock),
      category,
      image: imagePath,
      colors: colorsArray,
      sizes: sizesArray,
    });
    res.status(201).json(product);
  } catch (err) {
    // Handle duplicate key errors (especially for old variant indexes)
    if (err.code === 11000 || err.message.includes('duplicate key')) {
      const errorMessage = err.message.includes('variants') 
        ? 'Lỗi: Index cũ trong database đang gây xung đột. Vui lòng chạy script fix-indexes.js để sửa lỗi này.'
        : 'Lỗi: Dữ liệu trùng lặp. ' + err.message;
      return res.status(400).json({ 
        error: errorMessage,
        details: err.message,
        fix: err.message.includes('variants') ? 'Chạy: node scripts/fix-indexes.js' : null
      });
    }
    res.status(400).json({ error: err.message });
  }
});

// Lấy danh sách sản phẩm
router.get("/", async (req, res) => {
  try {
    const { search, category } = req.query;
    const query = {};
    
    if (search) {
      query.name = { $regex: search, $options: "i" };
    }
    
    if (category) {
      query.category = category;
    }
    
    const products = await Product.find(query).populate("category");
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Xem chi tiết sản phẩm
router.get("/:id", async (req, res) => {
  try {
    const product = await Product.findById(req.params.id).populate("category");
    if (!product) return res.status(404).json({ message: "Không tìm thấy sản phẩm" });
    res.json(product);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
