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
                holder.bind(firstItem.getProduct(), order.getFinalAmount(), order.getId());
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

        public void bind(Product product, double finalAmount, String orderId) {
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
                // Mua lại: chuyển đến chi tiết sản phẩm
                Intent intent = new Intent(context, DetailProductActivity.class);
                intent.putExtra("id", product.getId());
                context.startActivity(intent);
            });

            btnRate.setOnClickListener(v -> {
                // Đánh giá: chuyển đến chi tiết sản phẩm để đánh giá
                Intent intent = new Intent(context, DetailProductActivity.class);
                intent.putExtra("id", product.getId());
                context.startActivity(intent);
            });
        }
    }
}

