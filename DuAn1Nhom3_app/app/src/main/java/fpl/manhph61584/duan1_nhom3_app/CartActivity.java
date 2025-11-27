package fpl.manhph61584.duan1_nhom3_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import fpl.manhph61584.duan1_nhom3_app.network.ApiService;
import fpl.manhph61584.duan1_nhom3_app.Voucher;
import fpl.manhph61584.duan1_nhom3_app.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements VoucherAdapter.OnVoucherSelectListener {

    private RecyclerView recyclerView;
    private RecyclerView recyclerViewVouchers;
    private TextView txtSubtotal, txtTotal, txtDiscount, txtSelectedVoucher;
    private LinearLayout layoutDiscount;
    private EditText edtPhone, edtAddress, edtNote;
    private Button btnCheckout;
    private CartAdapter adapter;
    private VoucherAdapter voucherAdapter;
    private Voucher selectedVoucher;
    private List<Voucher> vouchers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cartRecyclerView);
        recyclerViewVouchers = findViewById(R.id.recyclerViewVouchers);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtTotal = findViewById(R.id.txtTotal);
        txtDiscount = findViewById(R.id.txtDiscount);
        txtSelectedVoucher = findViewById(R.id.txtSelectedVoucher);
        layoutDiscount = findViewById(R.id.layoutDiscount);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtNote = findViewById(R.id.edtNote);
        btnCheckout = findViewById(R.id.cartCheckout);

        adapter = new CartAdapter(this, new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        
        loadCartFromServer();

        voucherAdapter = new VoucherAdapter(vouchers, this);
        recyclerViewVouchers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewVouchers.setAdapter(voucherAdapter);

        loadVouchers();
        updateTotal();
        setupBottomNavigation();

        btnCheckout.setOnClickListener(v -> handleCheckout());
    }


    private void setupBottomNavigation() {
        LinearLayout btnHome = findViewById(R.id.btnHome);
        LinearLayout btnBottomCart = findViewById(R.id.btnBottomCart);
        LinearLayout btnProfile = findViewById(R.id.btnProfile);

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            });
        }

        if (btnBottomCart != null) {
            btnBottomCart.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, OrderStatusActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                if (UserManager.getCurrentUser() == null) {
                    Intent intent = new Intent(CartActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    Intent intent = new Intent(CartActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartFromServer();
    }

    private void loadCartFromServer() {
        String token = UserManager.getAuthToken();
        if (token == null) {
            // Chưa đăng nhập, load từ local
            adapter = new CartAdapter(this, new ArrayList<>(CartManager.getCartItems()));
            recyclerView.setAdapter(adapter);
            updateTotal();
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
                    
                    // Update adapter
                    adapter = new CartAdapter(CartActivity.this, new ArrayList<>(cartItems));
                    recyclerView.setAdapter(adapter);
                    updateTotal();
                } else {
                    // Load từ local nếu server lỗi
                    adapter = new CartAdapter(CartActivity.this, new ArrayList<>(CartManager.getCartItems()));
                    recyclerView.setAdapter(adapter);
                    updateTotal();
                }
            }

            @Override
            public void onFailure(Call<fpl.manhph61584.duan1_nhom3_app.network.dto.CartResponse> call, Throwable t) {
                // Load từ local nếu server lỗi
                adapter = new CartAdapter(CartActivity.this, new ArrayList<>(CartManager.getCartItems()));
                recyclerView.setAdapter(adapter);
                updateTotal();
            }
        });
    }

    private void loadVouchers() {
        String token = UserManager.getAuthToken();
        
        if (token != null) {
            // Đã đăng nhập: lấy cả vouchers của user và vouchers public
            String authHeader = "Bearer " + token;
            ApiClient.getApiService().getVouchers(authHeader).enqueue(new Callback<List<Voucher>>() {
                @Override
                public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        vouchers.clear();
                        vouchers.addAll(response.body());
                        voucherAdapter.notifyDataSetChanged();
                    } else {
                        // Nếu lỗi, thử load vouchers public
                        loadPublicVouchers();
                    }
                }

                @Override
                public void onFailure(Call<List<Voucher>> call, Throwable t) {
                    // Nếu lỗi, thử load vouchers public
                    loadPublicVouchers();
                }
            });
        } else {
            // Chưa đăng nhập: chỉ lấy vouchers public
            loadPublicVouchers();
        }
    }

    private void loadPublicVouchers() {
        ApiClient.getApiService().getPublicVouchers().enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    vouchers.clear();
                    vouchers.addAll(response.body());
                    voucherAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                // Không hiển thị lỗi nếu không load được vouchers
            }
        });
    }

    @Override
    public void onVoucherSelected(Voucher voucher) {
        selectedVoucher = voucher;
        voucherAdapter.setSelectedVoucher(voucher);
        if (voucher != null) {
            txtSelectedVoucher.setText("Đã chọn: " + voucher.getCode());
            txtSelectedVoucher.setVisibility(TextView.VISIBLE);
        } else {
            txtSelectedVoucher.setVisibility(TextView.GONE);
        }
        updateTotal();
    }

    private void updateTotal() {
        double subtotal = CartManager.getTotal();
        txtSubtotal.setText(String.format("%,.0f₫", subtotal));

        double discount = 0;
        if (selectedVoucher != null) {
            discount = selectedVoucher.calculateDiscount(subtotal);
            txtDiscount.setText(String.format("-%,.0f₫", discount));
            layoutDiscount.setVisibility(LinearLayout.VISIBLE);
        } else {
            layoutDiscount.setVisibility(LinearLayout.GONE);
        }

        double total = subtotal - discount;
        if (total < 0) total = 0;
        txtTotal.setText(String.format("%,.0f₫", total));
    }

    private void handleCheckout() {
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String note = edtNote.getText().toString().trim();

        if (CartManager.getCartItems().isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            edtPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            edtAddress.requestFocus();
            return;
        }

        // TODO: Tạo đơn hàng và gửi lên server
        Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
        
        // Xóa tất cả sản phẩm khỏi giỏ hàng sau khi thanh toán
        String token = UserManager.getAuthToken();
        if (token != null) {
            String authHeader = "Bearer " + token;
            List<CartItem> items = new ArrayList<>(CartManager.getCartItems());
            for (CartItem item : items) {
                if (item.getProduct() != null && item.getProduct().getId() != null) {
                    // Xóa từ server
                    ApiClient.getApiService().removeFromCart(authHeader, item.getProduct().getId()).enqueue(new Callback<fpl.manhph61584.duan1_nhom3_app.network.dto.CartResponse>() {
                        @Override
                        public void onResponse(Call<fpl.manhph61584.duan1_nhom3_app.network.dto.CartResponse> call, Response<fpl.manhph61584.duan1_nhom3_app.network.dto.CartResponse> response) {
                            // Reload cart
                            loadCartFromServer();
                        }

                        @Override
                        public void onFailure(Call<fpl.manhph61584.duan1_nhom3_app.network.dto.CartResponse> call, Throwable t) {
                            // Reload cart anyway
                            loadCartFromServer();
                        }
                    });
                }
            }
        } else {
            // Chưa đăng nhập, chỉ xóa local
            CartManager.clear();
            loadCartFromServer();
        }
        
        // Clear form
        edtPhone.setText("");
        edtAddress.setText("");
        edtNote.setText("");
    }
}







