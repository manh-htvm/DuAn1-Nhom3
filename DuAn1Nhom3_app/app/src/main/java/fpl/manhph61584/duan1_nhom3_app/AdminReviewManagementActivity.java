package fpl.manhph61584.duan1_nhom3_app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import fpl.manhph61584.duan1_nhom3_app.network.ApiService;
import fpl.manhph61584.duan1_nhom3_app.network.dto.ReplyRequest;
import fpl.manhph61584.duan1_nhom3_app.network.dto.ReviewResponse;
import fpl.manhph61584.duan1_nhom3_app.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReviewManagementActivity extends AppCompatActivity {

    private RecyclerView rcvReviews;
    private ReviewAdapter adapter;
    private List<Review> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_review_management);

        rcvReviews = findViewById(R.id.rcvReviews);
        rcvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter(reviewList);
        rcvReviews.setAdapter(adapter);

        loadReviews();
    }

    private void loadReviews() {
        String token = "Bearer " + UserManager.getAuthToken();
        ApiClient.getApiService().getAdminReviews(token, null, null, (Integer) null).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reviewList.clear();
                    reviewList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                Toast.makeText(AdminReviewManagementActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
        private List<Review> reviews;

        public ReviewAdapter(List<Review> reviews) {
            this.reviews = reviews;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_review, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Review review = reviews.get(position);
            holder.txtUser.setText(review.getUser() != null ? review.getUser().getName() : "Unknown");
            holder.txtRating.setText("⭐ " + review.getRating());
            holder.txtComment.setText(review.getComment());
            if (review.getAdminReply() != null && !review.getAdminReply().isEmpty()) {
                holder.txtReply.setText("Phản hồi: " + review.getAdminReply());
                holder.txtReply.setVisibility(View.VISIBLE);
            } else {
                holder.txtReply.setVisibility(View.GONE);
            }

            holder.btnReply.setOnClickListener(v -> showReplyDialog(review));
            holder.btnDelete.setOnClickListener(v -> deleteReview(review));
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtUser, txtRating, txtComment, txtReply;
            Button btnReply, btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                txtUser = itemView.findViewById(R.id.txtUser);
                txtRating = itemView.findViewById(R.id.txtRating);
                txtComment = itemView.findViewById(R.id.txtComment);
                txtReply = itemView.findViewById(R.id.txtReply);
                btnReply = itemView.findViewById(R.id.btnReply);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

    private void showReplyDialog(Review review) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Phản hồi đánh giá");

        final EditText input = new EditText(this);
        if (review.getAdminReply() != null) {
            input.setText(review.getAdminReply());
        }
        builder.setView(input);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String reply = input.getText().toString().trim();
            if (!TextUtils.isEmpty(reply)) {
                submitReply(review.getId(), reply);
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void submitReply(String reviewId, String reply) {
        String token = "Bearer " + UserManager.getAuthToken();
        ReplyRequest request = new ReplyRequest(reply);
        ApiClient.getApiService().replyReview(token, reviewId, request).enqueue(new Callback<Review>() {
            @Override
            public void onResponse(Call<Review> call, Response<Review> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminReviewManagementActivity.this, "Phản hồi thành công", Toast.LENGTH_SHORT).show();
                    loadReviews();
                } else {
                    Toast.makeText(AdminReviewManagementActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Review> call, Throwable t) {
                Toast.makeText(AdminReviewManagementActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReview(Review review) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa đánh giá này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                String token = "Bearer " + UserManager.getAuthToken();
                ApiClient.getApiService().deleteReview(token, review.getId()).enqueue(new Callback<ReviewResponse>() {
                    @Override
                    public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminReviewManagementActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            loadReviews();
                        } else {
                            Toast.makeText(AdminReviewManagementActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ReviewResponse> call, Throwable t) {
                        Toast.makeText(AdminReviewManagementActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}

