package fpl.manhph61584.duan1_nhom3_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import fpl.manhph61584.duan1_nhom3_app.network.dto.UserDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    RecyclerView rc;
    OrderHistoryAdapter adapter;
    ProgressBar loading;
    TextView tvEmpty;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rc = findViewById(R.id.rcOrderHistory);
        loading = findViewById(R.id.progressLoading);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);

        rc.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());

        UserDto user = UserManager.getCurrentUser();
        if (user == null) {
            tvEmpty.setText("Vui lòng đăng nhập lại.");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        String userId = user.getId();
        if (userId == null || userId.trim().isEmpty()) {
            tvEmpty.setText("Lỗi lấy thông tin tài khoản. Vui lòng đăng nhập lại.");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        loadOrders(userId);
    }

    private void loadOrders(String userId) {
        loading.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rc.setVisibility(View.GONE);

        ApiClient.getApiService().getOrders(userId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> res) {
                loading.setVisibility(View.GONE);

                if (!res.isSuccessful() || res.body() == null) {
                    tvEmpty.setText("Có lỗi xảy ra. Vui lòng thử lại sau.");
                    tvEmpty.setVisibility(View.VISIBLE);
                    return;
                }

                List<Order> list = res.body();

                if (list.isEmpty()) {
                    tvEmpty.setText("Bạn chưa có đơn hàng nào.");
                    tvEmpty.setVisibility(View.VISIBLE);
                    return;
                }

                rc.setVisibility(View.VISIBLE);

                // Khởi tạo adapter với userId thật
                adapter = new OrderHistoryAdapter(OrderHistoryActivity.this, list, userId);

                // Thiết lập callback reload list khi mua lại
                adapter.setOnOrderUpdatedListener(() -> loadOrders(userId));

                rc.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                loading.setVisibility(View.GONE);
                tvEmpty.setText("Không thể kết nối. Kiểm tra mạng của bạn.");
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

}