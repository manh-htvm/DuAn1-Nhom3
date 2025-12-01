package fpl.manhph61584.duan1_nhom3_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminUserManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        TextView txtInfo = findViewById(R.id.txtInfo);
        txtInfo.setText("Quản lý khách hàng\n\nChức năng này sẽ được triển khai với API endpoints từ server.");
    }
}

