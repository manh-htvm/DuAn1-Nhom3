package fpl.manhph61584.duan1_nhom3_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import fpl.manhph61584.duan1_nhom3_app.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromotionManagementActivity extends AppCompatActivity {

    private RecyclerView rcvVouchers;
    private ProgressBar progressBar;
    private VoucherAdapter adapter;
    private List<Voucher> voucherList = new ArrayList<>();
    private ImageView btnRefresh;
    private Button btnAddVoucher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_management);

        initViews();
        loadVouchers();
    }

    private void initViews() {
        rcvVouchers = findViewById(R.id.rcvVouchers);
        progressBar = findViewById(R.id.progressBar);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnAddVoucher = findViewById(R.id.btnAddVoucher);

        adapter = new VoucherAdapter(this, new ArrayList<>());
        adapter.setOnVoucherClickListener(new VoucherAdapter.OnVoucherClickListener() {
            @Override
            public void onEdit(Voucher voucher) {
                showVoucherForm(voucher);
            }

            @Override
            public void onDelete(Voucher voucher) {
                showDeleteConfirmDialog(voucher);
            }
        });

        rcvVouchers.setLayoutManager(new LinearLayoutManager(this));
        rcvVouchers.setAdapter(adapter);

        btnRefresh.setOnClickListener(v -> loadVouchers());
        btnAddVoucher.setOnClickListener(v -> showVoucherForm(null));
    }

    private void loadVouchers() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        Call<List<Voucher>> call = apiService.getAllVouchers();

        call.enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    voucherList = response.body();
                    adapter.updateVouchers(voucherList);
                } else {
                    Toast.makeText(PromotionManagementActivity.this, "Không thể tải danh sách voucher", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PromotionManagementActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showVoucherForm(Voucher voucher) {
        View formView = getLayoutInflater().inflate(R.layout.dialog_voucher_form, null);
        
        EditText edtCode = formView.findViewById(R.id.edtCode);
        EditText edtName = formView.findViewById(R.id.edtName);
        EditText edtDescription = formView.findViewById(R.id.edtDescription);
        Spinner spinnerDiscountType = formView.findViewById(R.id.spinnerDiscountType);
        EditText edtDiscount = formView.findViewById(R.id.edtDiscount);
        EditText edtMinPurchase = formView.findViewById(R.id.edtMinPurchase);
        EditText edtMaxDiscount = formView.findViewById(R.id.edtMaxDiscount);
        EditText edtUsageLimit = formView.findViewById(R.id.edtUsageLimit);
        EditText edtStartDate = formView.findViewById(R.id.edtStartDate);
        EditText edtEndDate = formView.findViewById(R.id.edtEndDate);
        CheckBox chkActive = formView.findViewById(R.id.chkActive);

        // Setup spinner
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                Arrays.asList("Số tiền", "Phần trăm")
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiscountType.setAdapter(spinnerAdapter);

        // Fill form if editing
        boolean isEdit = voucher != null;
        if (isEdit) {
            edtCode.setText(voucher.getCode());
            edtName.setText(voucher.getName());
            edtDescription.setText(voucher.getDescription());
            if ("percent".equals(voucher.getDiscountType())) {
                spinnerDiscountType.setSelection(1);
            }
            edtDiscount.setText(String.valueOf(voucher.getDiscount()));
            edtMinPurchase.setText(String.valueOf(voucher.getMinPurchase()));
            edtMaxDiscount.setText(String.valueOf(voucher.getMaxDiscount()));
            edtUsageLimit.setText(String.valueOf(voucher.getUsageLimit()));
            edtStartDate.setText(voucher.getStartDate());
            edtEndDate.setText(voucher.getEndDate());
            chkActive.setChecked(voucher.isActive());
            edtCode.setEnabled(false); // Không cho sửa mã khi edit
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa voucher" : "Thêm voucher mới")
                .setView(formView)
                .setPositiveButton(isEdit ? "Cập nhật" : "Thêm", (dialog, which) -> {
                    // Validate and save
                    if (validateForm(edtCode, edtName, edtDiscount, edtMinPurchase, edtMaxDiscount, 
                            edtUsageLimit, edtStartDate, edtEndDate)) {
                        try {
                            Voucher newVoucher = new Voucher();
                            if (isEdit) {
                                newVoucher.setId(voucher.getId());
                            }
                            newVoucher.setCode(edtCode.getText().toString().trim());
                            newVoucher.setName(edtName.getText().toString().trim());
                            newVoucher.setDescription(edtDescription.getText().toString().trim());
                            newVoucher.setDiscountType(spinnerDiscountType.getSelectedItemPosition() == 0 ? "amount" : "percent");
                            newVoucher.setDiscount(Double.parseDouble(edtDiscount.getText().toString()));
                            newVoucher.setMinPurchase(Double.parseDouble(edtMinPurchase.getText().toString()));
                            newVoucher.setMaxDiscount(Double.parseDouble(edtMaxDiscount.getText().toString()));
                            newVoucher.setUsageLimit(Integer.parseInt(edtUsageLimit.getText().toString()));
                            newVoucher.setStartDate(edtStartDate.getText().toString().trim());
                            newVoucher.setEndDate(edtEndDate.getText().toString().trim());
                            newVoucher.setActive(chkActive.isChecked());

                            if (isEdit) {
                                updateVoucher(newVoucher);
                            } else {
                                createVoucher(newVoucher);
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null);

        builder.show();
    }

    private boolean validateForm(EditText edtCode, EditText edtName, EditText edtDiscount,
                                 EditText edtMinPurchase, EditText edtMaxDiscount,
                                 EditText edtUsageLimit, EditText edtStartDate, EditText edtEndDate) {
        if (edtCode.getText().toString().trim().isEmpty()) {
            edtCode.setError("Vui lòng nhập mã voucher");
            return false;
        }
        if (edtName.getText().toString().trim().isEmpty()) {
            edtName.setError("Vui lòng nhập tên voucher");
            return false;
        }
        if (edtDiscount.getText().toString().trim().isEmpty()) {
            edtDiscount.setError("Vui lòng nhập giá trị giảm");
            return false;
        }
        if (edtMinPurchase.getText().toString().trim().isEmpty()) {
            edtMinPurchase.setText("0");
        }
        if (edtMaxDiscount.getText().toString().trim().isEmpty()) {
            edtMaxDiscount.setText("0");
        }
        if (edtUsageLimit.getText().toString().trim().isEmpty()) {
            edtUsageLimit.setError("Vui lòng nhập số lần sử dụng");
            return false;
        }
        if (edtStartDate.getText().toString().trim().isEmpty()) {
            edtStartDate.setError("Vui lòng nhập ngày bắt đầu");
            return false;
        }
        if (edtEndDate.getText().toString().trim().isEmpty()) {
            edtEndDate.setError("Vui lòng nhập ngày kết thúc");
            return false;
        }
        return true;
    }

    private void createVoucher(Voucher voucher) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        Call<Voucher> call = apiService.createVoucher(voucher);

        call.enqueue(new Callback<Voucher>() {
            @Override
            public void onResponse(Call<Voucher> call, Response<Voucher> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(PromotionManagementActivity.this, "Thêm voucher thành công", Toast.LENGTH_SHORT).show();
                    loadVouchers();
                } else {
                    Toast.makeText(PromotionManagementActivity.this, "Thêm voucher thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Voucher> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PromotionManagementActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateVoucher(Voucher voucher) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        Call<Voucher> call = apiService.updateVoucher(voucher.getId(), voucher);

        call.enqueue(new Callback<Voucher>() {
            @Override
            public void onResponse(Call<Voucher> call, Response<Voucher> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(PromotionManagementActivity.this, "Cập nhật voucher thành công", Toast.LENGTH_SHORT).show();
                    loadVouchers();
                } else {
                    Toast.makeText(PromotionManagementActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Voucher> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PromotionManagementActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog(Voucher voucher) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa voucher " + voucher.getCode() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteVoucher(voucher))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteVoucher(Voucher voucher) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        Call<Void> call = apiService.deleteVoucher(voucher.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(PromotionManagementActivity.this, "Xóa voucher thành công", Toast.LENGTH_SHORT).show();
                    loadVouchers();
                } else {
                    Toast.makeText(PromotionManagementActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PromotionManagementActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

