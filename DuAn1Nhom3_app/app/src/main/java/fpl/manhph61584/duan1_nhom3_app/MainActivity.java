package fpl.manhph61584.duan1_nhom3_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
<<<<<<< HEAD
import android.content.Intent;
import android.view.View;
import android.graphics.Color;
=======
import android.widget.Toast;
>>>>>>> 7946ba63acc271ad11d2fe70f26f6ff1f8538723

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

<<<<<<< HEAD
import fpl.manhph61584.duan1_nhom3_app.Category;
import fpl.manhph61584.duan1_nhom3_app.ProductAdapter;
=======
>>>>>>> 7946ba63acc271ad11d2fe70f26f6ff1f8538723
import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import fpl.manhph61584.duan1_nhom3_app.ProductAdapter;
import fpl.manhph61584.duan1_nhom3_app.Product;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rcvProduct;
    private ProductAdapter adapter;
    private EditText edtSearch;
    private ImageView btnCart;
    private LinearLayout layoutTabs;
<<<<<<< HEAD
    private LinearLayout btnBottomCart, btnProfile;
=======
>>>>>>> 7946ba63acc271ad11d2fe70f26f6ff1f8538723
    private List<Product> productList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private String selectedCategoryId = null;
    private TextView selectedTab = null;

    String[] categories = {
            "Tất cả",
            "Quần áo mùa đông",
            "Quần áo mùa hè",
            "Áo thun",
            "Áo khoác",
            "Quần jean",
            "Đồ bộ",
            "Thể thao"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);
            Log.d("MainActivity", "✅ Layout loaded");
            
            // Ánh xạ view
            rcvProduct = findViewById(R.id.rcvProduct);
            edtSearch = findViewById(R.id.edtSearch);
            btnCart = findViewById(R.id.btnCart);
            layoutTabs = findViewById(R.id.layoutTabs);
<<<<<<< HEAD
            btnBottomCart = findViewById(R.id.btnBottomCart);
            btnProfile = findViewById(R.id.btnProfile);

            if (rcvProduct == null) {
                Log.e("MainActivity", "❌ rcvProduct is null!");
                Toast.makeText(this, "Lỗi: không tìm thấy RecyclerView", Toast.LENGTH_SHORT).show();
                return;
            }

            if (edtSearch == null) {
                Log.e("MainActivity", "❌ edtSearch is null!");
            }
=======
>>>>>>> 7946ba63acc271ad11d2fe70f26f6ff1f8538723

            // Setup RecyclerView
            rcvProduct.setLayoutManager(new GridLayoutManager(this, 2));
            adapter = new ProductAdapter(this, productList);
            rcvProduct.setAdapter(adapter);

            // Setup Cart button
            if (btnCart != null) {
                btnCart.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, OrderStatusActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
            }

<<<<<<< HEAD
            // Bottom navigation
            LinearLayout btnHome = findViewById(R.id.btnHome);
            if (btnHome != null) {
                btnHome.setOnClickListener(v -> {
                    // Đã ở trang Home rồi, không cần làm gì
                });
            }

            if (btnBottomCart != null) {
                btnBottomCart.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, OrderStatusActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
            }

            if (btnProfile != null) {
                btnProfile.setOnClickListener(v -> {
                    if (UserManager.getCurrentUser() == null) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });
            }

            // Load categories và sản phẩm
            loadCategories();
            loadProducts(null, null);

            // Tìm kiếm
            if (edtSearch != null) {
                edtSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String keyword = s.toString().trim();
                        loadProducts(keyword.isEmpty() ? null : keyword, selectedCategoryId);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }

        } catch (Exception e) {
            Log.e("MainActivity", "❌ onCreate error: ", e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load cart from server when activity resumes (e.g., after login)
        loadCartFromServer();
    }

    private void loadCartFromServer() {
        String token = UserManager.getAuthToken();
        if (token == null) {
            // Chưa đăng nhập, không cần load
            return;
        }

        String authHeader = "Bearer " + token;
        ApiClient.getApiService().getCart(authHeader).enqueue(new Callback<fpl.manhph61584.duan1_nhom3_app.network.dto.CartResponse>() {
            @Override
            public void onResponse(Call<fpl.manhph61584.duan1_nhom3_app.network.dto.CartResponse> call, Response<fpl.manhph61584.duan1_nhom3_app.network.dto.CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fpl.manhph61584.duan1_nhom3_app.network.dto.CartResponse cartResponse = response.body();
                    List<fpl.manhph61584.duan1_nhom3_app.network.dto.CartItemDto> items = cartResponse.getItems();
                    
                    // Convert CartItemDto to CartItem
                    List<CartItem> cartItems = new ArrayList<>();
                    if (items != null) {
                        for (fpl.manhph61584.duan1_nhom3_app.network.dto.CartItemDto dto : items) {
                            if (dto.getProduct() != null) {
                                CartItem item = new CartItem(
                                    dto.getProduct(),
                                    dto.getQuantity(),
                                    dto.getColor() != null ? dto.getColor() : "Mặc định",
                                    dto.getSize() != null ? dto.getSize() : "Free size"
                                );
                                cartItems.add(item);
                            }
                        }
                    }
                    
                    // Update CartManager
                    CartManager.clear();
                    for (CartItem item : cartItems) {
                        CartManager.addToCart(item.getProduct(), item.getQuantity(), item.getColor(), item.getSize());
                    }
                    
                    Log.d("MainActivity", "✅ Cart loaded from server: " + cartItems.size() + " items");
                } else {
                    Log.d("MainActivity", "⚠️ Failed to load cart from server");
                }
            }

            @Override
            public void onFailure(Call<fpl.manhph61584.duan1_nhom3_app.network.dto.CartResponse> call, Throwable t) {
                Log.e("MainActivity", "❌ Error loading cart: " + t.getMessage());
            }
        });
    }

    private void loadCategories() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body());
                    setupCategoryTabs();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e("MainActivity", "Failed to load categories", t);
            }
        });
    }

    private void setupCategoryTabs() {
        if (layoutTabs == null) return;

        layoutTabs.removeAllViews();

        // Tab "Tất cả"
        TextView tabAll = createTab("Tất cả", null);
        tabAll.setOnClickListener(v -> selectCategory(null, tabAll));
        layoutTabs.addView(tabAll);

        // Các tab category
        for (Category category : categoryList) {
            TextView tab = createTab(category.getName(), category.getId());
            tab.setOnClickListener(v -> selectCategory(category.getId(), tab));
            layoutTabs.addView(tab);
        }

        // Chọn tab "Tất cả" mặc định
        if (selectedTab == null) {
            selectCategory(null, tabAll);
        }
    }

    private TextView createTab(String text, String categoryId) {
        TextView tab = new TextView(this);
        tab.setText(text);
        tab.setTextSize(15);
        tab.setPadding(32, 12, 32, 12);
        tab.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tab.getLayoutParams();
        params.setMarginEnd(20);
        tab.setLayoutParams(params);
        tab.setBackgroundResource(R.drawable.bg_unselect);
        tab.setTextColor(Color.parseColor("#666666"));
        tab.setClickable(true);
        tab.setFocusable(true);
        return tab;
    }

    private void selectCategory(String categoryId, TextView tab) {
        selectedCategoryId = categoryId;

        // Reset tất cả tabs
        for (int i = 0; i < layoutTabs.getChildCount(); i++) {
            View child = layoutTabs.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setBackgroundResource(R.drawable.bg_unselect);
                ((TextView) child).setTextColor(Color.parseColor("#666666"));
            }
        }

        // Highlight tab được chọn
        if (tab != null) {
            tab.setBackgroundResource(R.drawable.bg_selected);
            tab.setTextColor(Color.parseColor("#000000"));
            selectedTab = tab;
        }

        // Reload products với category filter
        String search = edtSearch != null ? edtSearch.getText().toString().trim() : null;
        loadProducts(search != null && !search.isEmpty() ? search : null, categoryId);
    }

    private void loadProducts(String search, String categoryId) {
        try {
            Log.d("MainActivity", "🔄 Loading products...");

            ApiClient.getApiService().getProducts(search, categoryId).enqueue(new Callback<List<Product>>() {
=======
            // Tạo tab động
            generateTabs();

            // Tải tất cả sản phẩm ban đầu
            loadProducts(null);

            // Tìm kiếm sản phẩm theo tên
            edtSearch.addTextChangedListener(new TextWatcher() {
>>>>>>> 7946ba63acc271ad11d2fe70f26f6ff1f8538723
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String keyword = s.toString().trim();
                    loadProducts(keyword.isEmpty() ? null : keyword);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

        } catch (Exception e) {
            Log.e("MainActivity", "❌ onCreate error: ", e);
        }
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCart = findViewById(R.id.navCart);
        LinearLayout navUser = findViewById(R.id.navUser);

        navCart.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderHistoryActivity.class));
        });
    }

    // -----------------------------
    // 🔥 Tạo danh sách TAB
    // -----------------------------
    private void generateTabs() {
        for (int i = 0; i < categories.length; i++) {
            TextView tv = new TextView(this);
            tv.setText(categories[i]);
            tv.setPadding(40, 20, 40, 20);
            tv.setTextSize(15);
            tv.setBackground(getDrawable(R.drawable.tab_default));

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
            params.setMargins(0, 0, 30, 0);
            tv.setLayoutParams(params);

            int index = i;
            tv.setOnClickListener(v -> selectTab(index));

            layoutTabs.addView(tv);
        }

        selectTab(0); // Chọn tab đầu tiên
    }

    // -----------------------------
    // 🔥 Xử lý chọn TAB + gọi API
    // -----------------------------
    private void selectTab(int index) {
        for (int i = 0; i < layoutTabs.getChildCount(); i++) {
            TextView tv = (TextView) layoutTabs.getChildAt(i);

            if (i == index) {
                tv.setBackground(getDrawable(R.drawable.tab_selected));
                tv.setTextColor(getColor(android.R.color.white));
            } else {
                tv.setBackground(getDrawable(R.drawable.tab_default));
                tv.setTextColor(getColor(android.R.color.black));
            }
        }

        String category = categories[index];

        if (category.equals("Tất cả")) {
            loadProducts(null);
        } else {
            loadProducts(category);
        }
    }

    // -----------------------------
    // 🔥 Gọi API load sản phẩm
    // -----------------------------
    private void loadProducts(String search) {
        Log.d("MainActivity", "🔄 Loading products… query = " + search);

        ApiClient.getApiService().getProducts(search).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        productList.clear();
                        productList.addAll(response.body());
                        adapter.notifyDataSetChanged();

                        if (productList.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Không có sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error onResponse: ", e);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối API", Toast.LENGTH_LONG).show();
                Log.e("MainActivity", "API Failed: ", t);
            }
        });
    }
}
