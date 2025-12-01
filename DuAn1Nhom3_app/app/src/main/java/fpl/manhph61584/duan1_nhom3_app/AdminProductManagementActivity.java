package fpl.manhph61584.duan1_nhom3_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import fpl.manhph61584.duan1_nhom3_app.network.ApiService;
import fpl.manhph61584.duan1_nhom3_app.UserManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductManagementActivity extends AppCompatActivity {

    private RecyclerView rcvProducts;
    private EditText edtSearch;
    private Button btnAddProduct;
    private AdminProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_management);

        initViews();
        setupRecyclerView();
        loadProducts();
        setupSearch();
        setupAddButton();
    }

    private void initViews() {
        rcvProducts = findViewById(R.id.rcvProducts);
        edtSearch = findViewById(R.id.edtSearch);
        btnAddProduct = findViewById(R.id.btnAddProduct);
    }

    private void setupRecyclerView() {
        rcvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminProductAdapter(productList);
        rcvProducts.setAdapter(adapter);
    }

    private void loadProducts() {
        ApiClient.getApiService().getProducts(null, null).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    filteredList.clear();
                    filteredList.addAll(productList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(AdminProductManagementActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupAddButton() {
        btnAddProduct.setOnClickListener(v -> {
            showAddProductDialog();
        });
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        EditText edtProductName = dialogView.findViewById(R.id.edtProductName);
        EditText edtProductDescription = dialogView.findViewById(R.id.edtProductDescription);
        EditText edtProductPrice = dialogView.findViewById(R.id.edtProductPrice);
        EditText edtProductStock = dialogView.findViewById(R.id.edtProductStock);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText edtColors = dialogView.findViewById(R.id.edtColors);
        EditText edtSizes = dialogView.findViewById(R.id.edtSizes);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        ImageView imgSelected = dialogView.findViewById(R.id.imgSelected);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelAdd);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitAdd);

        // Load categories
        ApiClient.getApiService().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    List<String> categoryNames = new ArrayList<>();
                    for (Category cat : categories) {
                        categoryNames.add(cat.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminProductManagementActivity.this, android.R.layout.simple_spinner_item, categoryNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AdminProductManagementActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });

        Uri[] selectedImageUri = {null};
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri imageUri = result.getData().getData();
                    selectedImageUri[0] = imageUri;
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        imgSelected.setImageBitmap(bitmap);
                        imgSelected.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        Toast.makeText(AdminProductManagementActivity.this, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String name = edtProductName.getText().toString().trim();
            String description = edtProductDescription.getText().toString().trim();
            String priceStr = edtProductPrice.getText().toString().trim();
            String stockStr = edtProductStock.getText().toString().trim();
            String colorsStr = edtColors.getText().toString().trim();
            String sizesStr = edtSizes.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri[0] == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }

            if (spinnerCategory.getSelectedItemPosition() == -1) {
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy category ID
            ApiClient.getApiService().getCategories().enqueue(new Callback<List<Category>>() {
                @Override
                public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Category> categories = response.body();
                        if (spinnerCategory.getSelectedItemPosition() < categories.size()) {
                            Category selectedCategory = categories.get(spinnerCategory.getSelectedItemPosition());
                            createProduct(name, description, priceStr, stockStr, selectedCategory.getId(), colorsStr, sizesStr, selectedImageUri[0], dialog);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Category>> call, Throwable t) {
                    Toast.makeText(AdminProductManagementActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void createProduct(String name, String description, String priceStr, String stockStr, String categoryId, String colorsStr, String sizesStr, Uri imageUri, AlertDialog dialog) {
        try {
            // Upload ảnh trước
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
            inputStream.close();

            String mimeType = getContentResolver().getType(imageUri);
            if (mimeType == null) mimeType = "image/jpeg";
            
            RequestBody imageRequestBody = RequestBody.create(MediaType.parse(mimeType), imageBytes);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", "product.jpg", imageRequestBody);

            // Tạo các RequestBody
            RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name);
            RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description != null ? description : "");
            RequestBody priceBody = RequestBody.create(MediaType.parse("text/plain"), priceStr);
            RequestBody stockBody = RequestBody.create(MediaType.parse("text/plain"), stockStr);
            RequestBody soldBody = RequestBody.create(MediaType.parse("text/plain"), "0");
            RequestBody categoryBody = RequestBody.create(MediaType.parse("text/plain"), categoryId);
            RequestBody colorsBody = RequestBody.create(MediaType.parse("text/plain"), colorsStr.isEmpty() ? "[]" : colorsStr);
            RequestBody sizesBody = RequestBody.create(MediaType.parse("text/plain"), sizesStr.isEmpty() ? "[]" : sizesStr);
            RequestBody variantsBody = RequestBody.create(MediaType.parse("text/plain"), "[]");

            String token = "Bearer " + UserManager.getAuthToken();
            ApiClient.getApiService().createProduct(token, imagePart, nameBody, descriptionBody, priceBody, stockBody, soldBody, categoryBody, colorsBody, sizesBody, variantsBody).enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminProductManagementActivity.this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadProducts();
                    } else {
                        Toast.makeText(AdminProductManagementActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    Toast.makeText(AdminProductManagementActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi đọc ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {
        private List<Product> products;

        public AdminProductAdapter(List<Product> products) {
            this.products = products;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Product product = filteredList.get(position);
            holder.txtName.setText(product.getName());
            holder.txtPrice.setText(product.getPrice() + "₫");
            holder.txtStock.setText("Stock: " + product.getStock() + " | Sold: " + product.getSold());

            String imageUrl = product.getImage();
            if (imageUrl != null && imageUrl.startsWith("/uploads/")) {
                imageUrl = "http://10.0.2.2:3000" + imageUrl;
            }
            Glide.with(AdminProductManagementActivity.this).load(imageUrl).into(holder.imgProduct);

            holder.btnEdit.setOnClickListener(v -> {
                Toast.makeText(AdminProductManagementActivity.this, "Chức năng sửa sẽ được triển khai", Toast.LENGTH_SHORT).show();
            });

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(AdminProductManagementActivity.this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa sản phẩm này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        Toast.makeText(AdminProductManagementActivity.this, "Chức năng xóa sẽ được triển khai", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            });
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView txtName, txtPrice, txtStock;
            Button btnEdit, btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                imgProduct = itemView.findViewById(R.id.imgProduct);
                txtName = itemView.findViewById(R.id.txtName);
                txtPrice = itemView.findViewById(R.id.txtPrice);
                txtStock = itemView.findViewById(R.id.txtStock);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}

