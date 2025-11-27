package fpl.manhph61584.duan1_nhom3_app;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    public interface OnOrderUpdatedListener {
        void onOrderCreated();
    }

    private Context context;
    private List<Order> orderList;
    private String userId;
    private OnOrderUpdatedListener listener;

    public OrderHistoryAdapter(Context context, List<Order> orderList, String userId) {
        this.context = context;
        this.orderList = orderList;
        this.userId = userId;
    }

    public void setOnOrderUpdatedListener(OnOrderUpdatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
        android.view.View view = android.view.LayoutInflater.from(context)
                .inflate(R.layout.row_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Order order = orderList.get(position);

        if (order.getItems() == null || order.getItems().isEmpty()) return;

        order.flattenItems(); // flatten all items

        OrderItem item = order.getItems().get(0);

        h.tvName.setText(item.getName());
        h.tvPrice.setText(String.format("%,.0f₫", order.getTotalAmount()));

        Glide.with(context).load(item.getImage()).into(h.imgProduct);

        // ----- Mua lại -----
        h.btnBuyAgain.setOnClickListener(v -> {
            item.flatten();

            if (item.getProductId() == null || item.getProductId().isEmpty()) {
                Toast.makeText(context, "Sản phẩm không còn tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mở DetailProductActivity với productId
            Intent intent = new Intent(context, DetailProductActivity.class);
            intent.putExtra("id", item.getProductId()); // key "id" để DetailProductActivity lấy
            context.startActivity(intent);
        });


        // ----- Đánh giá -----
        h.btnReview.setOnClickListener(v -> {
            Toast.makeText(context, "Chức năng đánh giá đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice;
        Button btnBuyAgain, btnReview;

        public ViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);
            btnReview = itemView.findViewById(R.id.btnReview);
        }
    }
}
