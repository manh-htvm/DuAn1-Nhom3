package fpl.manhph61584.duan1_nhom3_app;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fpl.manhph61584.duan1_nhom3_app.network.dto.OrderDto;
import fpl.manhph61584.duan1_nhom3_app.network.dto.OrderItemDto;
import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import fpl.manhph61584.duan1_nhom3_app.network.dto.ReviewRequest;
import fpl.manhph61584.duan1_nhom3_app.network.dto.ReviewResponse;
import fpl.manhph61584.duan1_nhom3_app.UserManager;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private final android.content.Context context;
    private List<OrderDto> orders;

    public OrderHistoryAdapter(android.content.Context context, List<OrderDto> orders) {
        this.context = context;
        this.orders = new ArrayList<>(orders);
    }

    public void updateOrders(List<OrderDto> newOrders) {
        this.orders = new ArrayList<>(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderDto order = orders.get(position);
        
        // Lấy sản phẩm đầu tiên để hiển thị
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            OrderItemDto firstItem = order.getItems().get(0);
            if (firstItem.getProduct() != null) {
                holder.bind(firstItem.getProduct(), order.getFinalAmount(), order.getId(), order);
            }
        }
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProduct;
        private TextView txtProductName, txtPrice;
        private Button btnBuyAgain, btnRate;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);
            btnRate = itemView.findViewById(R.id.btnRate);
        }

        public void bind(Product product, double finalAmount, String orderId, OrderDto order) {
            txtProductName.setText(product.getName());
            txtPrice.setText(String.format("%,.0f₫", finalAmount));

            // Load ảnh
            String imageUrl = product.getImage();
            if (imageUrl != null && imageUrl.startsWith("/uploads/")) {
                imageUrl = "http://10.0.2.2:3000" + imageUrl;
            }
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .into(imgProduct);

            btnBuyAgain.setOnClickListener(v -> {
                // Mua lại: chuyển đến CartActivity với buy_now=true
                Intent intent = new Intent(context, CartActivity.class);
                intent.putExtra("buy_now", true);
                intent.putExtra("product_id", product.getId());
                // Lấy thông tin từ order item nếu có
                if (order != null && order.getItems() != null && !order.getItems().isEmpty()) {
                    OrderItemDto firstItem = order.getItems().get(0);
                    if (firstItem.getProduct() != null && firstItem.getProduct().getId().equals(product.getId())) {
                        intent.putExtra("quantity", firstItem.getQuantity());
                        intent.putExtra("color", firstItem.getColor() != null ? firstItem.getColor() : "Mặc định");
                        intent.putExtra("size", firstItem.getSize() != null ? firstItem.getSize() : "Free size");
                    }
                }
                context.startActivity(intent);
            });

            btnRate.setOnClickListener(v -> {
                // Đánh giá: mở dialog đánh giá
                showReviewDialog(product.getId());
            });
        }
    }

    private void showReviewDialog(String productId) {
        if (!(context instanceof android.app.Activity)) {
            return;
        }
        
        android.app.Activity activity = (android.app.Activity) context;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_review, null);
        builder.setView(dialogView);

        ImageView star1 = dialogView.findViewById(R.id.star1);
        ImageView star2 = dialogView.findViewById(R.id.star2);
        ImageView star3 = dialogView.findViewById(R.id.star3);
        ImageView star4 = dialogView.findViewById(R.id.star4);
        ImageView star5 = dialogView.findViewById(R.id.star5);
        TextView txtRatingValue = dialogView.findViewById(R.id.txtRatingValue);
        EditText edtComment = dialogView.findViewById(R.id.edtReviewComment);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelReview);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitReview);

        final int[] selectedRating = {0};
        ImageView[] stars = {star1, star2, star3, star4, star5};

        View.OnClickListener starClickListener = v -> {
            int rating = 0;
            for (int i = 0; i < stars.length; i++) {
                if (v == stars[i]) {
                    rating = i + 1;
                    break;
                }
            }
            selectedRating[0] = rating;
            updateStars(stars, rating);
            txtRatingValue.setText(rating + " sao");
        };

        for (ImageView star : stars) {
            star.setOnClickListener(starClickListener);
        }

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            if (selectedRating[0] == 0) {
                Toast.makeText(activity, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }

            String token = UserManager.getAuthToken();
            if (token == null) {
                Toast.makeText(activity, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            String comment = edtComment.getText().toString().trim();
            ReviewRequest request = new ReviewRequest(productId, selectedRating[0], comment);

            String authHeader = "Bearer " + token;
            ApiClient.getApiService().createReview(authHeader, request).enqueue(new Callback<ReviewResponse>() {
                @Override
                public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(activity, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        String errorMsg = "Lỗi đánh giá";
                        if (response.errorBody() != null) {
                            try {
                                String errorStr = response.errorBody().string();
                                com.google.gson.JsonObject jsonObject = new com.google.gson.Gson().fromJson(errorStr, com.google.gson.JsonObject.class);
                                if (jsonObject.has("message")) {
                                    errorMsg = jsonObject.get("message").getAsString();
                                }
                            } catch (Exception e) {
                                errorMsg += ": " + response.message();
                            }
                        }
                        Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ReviewResponse> call, Throwable t) {
                    Toast.makeText(activity, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void updateStars(ImageView[] stars, int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_empty);
            }
        }
    }
}


