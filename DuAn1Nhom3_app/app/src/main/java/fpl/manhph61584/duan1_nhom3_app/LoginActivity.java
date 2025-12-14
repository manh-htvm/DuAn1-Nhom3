package fpl.manhph61584.duan1_nhom3_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import fpl.manhph61584.duan1_nhom3_app.network.ApiService;
import fpl.manhph61584.duan1_nhom3_app.network.dto.LoginRequest;
import fpl.manhph61584.duan1_nhom3_app.network.dto.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.regex.Pattern;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar loginProgress;
    private CheckBox checkboxRememberMe;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        loginProgress = findViewById(R.id.loginProgress);
        checkboxRememberMe = findViewById(R.id.checkboxRememberMe);
        apiService = ApiClient.getApiService();

        String savedEmail = UserManager.getSavedEmail(this);
        if (!savedEmail.isEmpty()) {
            edtUsername.setText(savedEmail);
            checkboxRememberMe.setChecked(true);
        }

        if (UserManager.restoreSession(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void attemptLogin() {
        String email = edtUsername.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email không đúng định dạng", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        apiService.login(new LoginRequest(email, pass)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    boolean rememberMe = checkboxRememberMe.isChecked();
                    UserManager.saveSession(body.getUser(), body.getToken(), LoginActivity.this, rememberMe);
                    
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                } else {
                    String errorMsg = "Sai email hoặc mật khẩu!";
                    if (response.code() == 403) {
                        errorMsg = "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.";
                    } else if (response.errorBody() != null) {
                        try {
                            String errorStr = response.errorBody().string();
                            try {
                                JsonObject jsonObject = new Gson().fromJson(errorStr, JsonObject.class);
                                if (jsonObject.has("message")) {
                                    errorMsg = jsonObject.get("message").getAsString();
                                }
                            } catch (Exception e) {
                                errorMsg = errorStr;
                            }
                        } catch (Exception e) {
                            errorMsg = "Lỗi đăng nhập: " + response.message();
                        }
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        tvRegister.setEnabled(!loading);
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailPattern, email);
    }
}