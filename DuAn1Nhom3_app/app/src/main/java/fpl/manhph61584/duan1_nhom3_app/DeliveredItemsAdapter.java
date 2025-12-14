package fpl.manhph61584.duan1_nhom3_app;

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

import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import fpl.manhph61584.duan1_nhom3_app.network.dto.OrderItemDto;
import fpl.manhph61584.duan1_nhom3_app.UserManager;

public class DeliveredItemsAdapter extends RecyclerView.Adapter<DeliveredItemsAdapter.ItemViewHolder> {

    private final android.content.Context context;
    private List<OrderItemDto> items;

    public DeliveredItemsAdapter(android.content.Context context, List<OrderItemDto> items) {
        this.context = context;
        this.items = new ArrayList<>(items);
    }

    public void updateItems(List<OrderItemDto> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_status, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        OrderItemDto item = items.get(position);
        
        if (item.getProduct() != null) {
            Product product = item.getProduct();
            holder.txtName.setText(product.getName());
            holder.txtVariant.setText("Màu: " + (item.getColor() != null ? item.getColor() : "Mặc định") + 
                                     " | Size: " + (item.getSize() != null ? item.getSize() : "Free size"));
            holder.txtQuantity.setText("Số lượng: " + item.getQuantity());
            
            double itemTotal = item.getPrice() * item.getQuantity();
            holder.txtPrice.setText(String.format("%,.0f₫", itemTotal));

            // Load ảnh
            String imageUrl = product.getImage();
            if (imageUrl != null && imageUrl.startsWith("/uploads/")) {
                imageUrl = "http://10.0.2.2:3000" + imageUrl;
            }
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgProduct);

            // Hiển thị và xử lý nút "Đánh giá"
            holder.btnReview.setVisibility(View.VISIBLE);
            holder.btnReview.setOnClickListener(v -> {
                if (context instanceof android.app.Activity) {
                    android.app.Activity activity = (android.app.Activity) context;
                    if (activity instanceof OrderHistoryActivity) {
                        ((OrderHistoryActivity) activity).showReviewDialog(product.getId());
                    }
                }
            });

            // Hiển thị và xử lý nút "Mua lại" - chuyển thẳng đến trang thanh toán (chế độ "Mua ngay")
            holder.btnBuyAgain.setVisibility(View.VISIBLE);
            holder.btnBuyAgain.setOnClickListener(v -> {
                // Chuyển thẳng đến trang thanh toán với sản phẩm này (chế độ "Mua ngay")
                if (context instanceof android.app.Activity) {
                    android.content.Intent intent = new android.content.Intent(context, CartActivity.class);
                    intent.putExtra("buy_now", true);
                    intent.putExtra("product_id", product.getId());
                    intent.putExtra("quantity", item.getQuantity());
                    intent.putExtra("color", item.getColor() != null && !item.getColor().isEmpty() ? item.getColor() : "Mặc định");
                    intent.putExtra("size", item.getSize() != null && !item.getSize().isEmpty() ? item.getSize() : "Free size");
                    context.startActivity(intent);
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).overridePendingTransition(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        );
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtVariant, txtQuantity, txtPrice;
        Button btnReview, btnBuyAgain;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.orderImgProduct);
            txtName = itemView.findViewById(R.id.orderTxtName);
            txtVariant = itemView.findViewById(R.id.orderTxtVariant);
            txtQuantity = itemView.findViewById(R.id.orderTxtQuantity);
            txtPrice = itemView.findViewById(R.id.orderTxtPrice);
            btnReview = itemView.findViewById(R.id.btnReview);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);
        }
    }

}

