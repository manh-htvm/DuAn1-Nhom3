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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

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
    private List<Product> productList = new ArrayList<>();

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

            // Setup RecyclerView
            rcvProduct.setLayoutManager(new GridLayoutManager(this, 2));
            adapter = new ProductAdapter(this, productList);
            rcvProduct.setAdapter(adapter);

            // Setup Cart button
            if (btnCart != null) {
                btnCart.setOnClickListener(v ->
                        startActivity(new Intent(MainActivity.this, CartActivity.class))
                );
            }

            // Tạo tab động
            generateTabs();

            // Tải tất cả sản phẩm ban đầu
            loadProducts(null);

            // Tìm kiếm sản phẩm theo tên
            edtSearch.addTextChangedListener(new TextWatcher() {
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
