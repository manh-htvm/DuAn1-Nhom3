package fpl.manhph61584.duan1_nhom3_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private LinearLayout cardProducts, cardUsers, cardVouchers, cardReviews, cardStatistics;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Kiểm tra quyền admin
        if (!isAdmin()) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        cardProducts = findViewById(R.id.cardProducts);
        cardUsers = findViewById(R.id.cardUsers);
        cardVouchers = findViewById(R.id.cardVouchers);
        cardReviews = findViewById(R.id.cardReviews);
        cardStatistics = findViewById(R.id.cardStatistics);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        cardProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminProductManagementActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        cardUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminUserManagementActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        cardVouchers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminVoucherManagementActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        cardReviews.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminReviewManagementActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        cardStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminStatisticsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnLogout.setOnClickListener(v -> {
            UserManager.clearSession();
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private boolean isAdmin() {
        fpl.manhph61584.duan1_nhom3_app.network.dto.UserDto user = UserManager.getCurrentUser();
        return user != null && "admin".equals(user.getRole());
    }
}



