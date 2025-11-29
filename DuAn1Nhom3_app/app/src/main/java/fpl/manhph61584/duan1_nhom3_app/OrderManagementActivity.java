package fpl.manhph61584.duan1_nhom3_app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import fpl.manhph61584.duan1_nhom3_app.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderManagementActivity extends AppCompatActivity {

    private RecyclerView rcvOrders;
    private ProgressBar progressBar;
    private OrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private String currentFilter = "all";
    private ImageView btnRefresh;
    private LinearLayout layoutStatusTabs;

    private final String[] statuses = {"all", "pending", "confirmed", "shipping", "delivered", "cancelled"};
    private final String[] statusLabels = {"Tất cả", "Chờ xử lý", "Đã xác nhận", "Đang giao", "Đã giao", "Đã hủy"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_management);

        initViews();
        setupStatusTabs();
        loadOrders();
    }

    private void initViews() {
        rcvOrders = findViewById(R.id.rcvOrders);
        progressBar = findViewById(R.id.progressBar);
        btnRefresh = findViewById(R.id.btnRefresh);
        layoutStatusTabs = findViewById(R.id.layoutStatusTabs);

        adapter = new OrderAdapter(this, new ArrayList<>());
        adapter.setOnOrderClickListener(new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onViewDetail(Order order) {
                showOrderDetail(order);
            }

            @Override
            public void onUpdateStatus(Order order) {
                showStatusUpdateDialog(order);
            }
        });

        rcvOrders.setLayoutManager(new LinearLayoutManager(this));
        rcvOrders.setAdapter(adapter);

        btnRefresh.setOnClickListener(v -> loadOrders());
    }

    private void setupStatusTabs() {
        layoutStatusTabs.removeAllViews();
        for (int i = 0; i < statuses.length; i++) {
            TextView tab = new TextView(this);
            tab.setText(statusLabels[i]);
            tab.setPadding(24, 12, 24, 12);
            tab.setTextSize(14);
            tab.setBackgroundResource(android.R.drawable.btn_default);
            tab.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            final String status = statuses[i];
            final int index = i;
            tab.setOnClickListener(v -> {
                currentFilter = status;
                filterOrders();
                updateTabSelection(index);
            });

            layoutStatusTabs.addView(tab);
        }
        updateTabSelection(0);
    }

    private void updateTabSelection(int selectedIndex) {
        for (int i = 0; i < layoutStatusTabs.getChildCount(); i++) {
            TextView tab = (TextView) layoutStatusTabs.getChildAt(i);
            if (i == selectedIndex) {
                tab.setBackgroundColor(0xFF2196F3);
                tab.setTextColor(0xFFFFFFFF);
            } else {
                tab.setBackgroundResource(android.R.drawable.btn_default);
                tab.setTextColor(0xFF000000);
            }
        }
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        Call<List<Order>> call = apiService.getAllOrders();

        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body();
                    filterOrders();
                } else {
                    Toast.makeText(OrderManagementActivity.this, "Không thể tải danh sách đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderManagementActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterOrders() {
        List<Order> filtered = new ArrayList<>();
        if (currentFilter.equals("all")) {
            filtered.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (order.getStatus() != null && order.getStatus().equalsIgnoreCase(currentFilter)) {
                    filtered.add(order);
                }
            }
        }
        adapter.updateOrders(filtered);
    }

    private void showOrderDetail(Order order) {
        StringBuilder detail = new StringBuilder();
        detail.append("Mã đơn hàng: ").append(order.getId()).append("\n\n");
        detail.append("Trạng thái: ").append(getStatusText(order.getStatus())).append("\n");
        detail.append("Tổng tiền: ").append(String.format("%,.0f₫", order.getTotalAmount())).append("\n");
        detail.append("Địa chỉ giao hàng: ").append(order.getShippingAddress()).append("\n");
        detail.append("Số điện thoại: ").append(order.getPhoneNumber()).append("\n");
        detail.append("Phương thức thanh toán: ").append(order.getPaymentMethod()).append("\n\n");
        detail.append("Sản phẩm:\n");
        
        if (order.getItems() != null) {
            for (Order.OrderItem item : order.getItems()) {
                detail.append("- ").append(item.getProductName())
                        .append(" x").append(item.getQuantity())
                        .append(" (").append(item.getColor()).append(", ").append(item.getSize()).append(")\n");
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết đơn hàng")
                .setMessage(detail.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showStatusUpdateDialog(Order order) {
        String[] statusOptions = {"Chờ xử lý", "Đã xác nhận", "Đang giao", "Đã giao", "Đã hủy"};
        String[] statusValues = {"pending", "confirmed", "shipping", "delivered", "cancelled"};

        new AlertDialog.Builder(this)
                .setTitle("Cập nhật trạng thái")
                .setItems(statusOptions, (dialog, which) -> {
                    updateOrderStatus(order, statusValues[which]);
                })
                .show();
    }

    private void updateOrderStatus(Order order, String newStatus) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        ApiService.OrderStatusUpdateRequest request = new ApiService.OrderStatusUpdateRequest(newStatus);
        Call<Order> call = apiService.updateOrderStatus(order.getId(), request);

        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(OrderManagementActivity.this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                    loadOrders();
                } else {
                    Toast.makeText(OrderManagementActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderManagementActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStatusText(String status) {
        if (status == null) return "Chờ xử lý";
        switch (status.toLowerCase()) {
            case "pending": return "Chờ xử lý";
            case "confirmed": return "Đã xác nhận";
            case "shipping": return "Đang giao";
            case "delivered": return "Đã giao";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }
}

