package fpl.manhph61584.duan1_nhom3_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onViewDetail(Order order);
        void onUpdateStatus(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void updateOrders(List<Order> orders) {
        this.orderList = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtOrderStatus, txtOrderDate, txtOrderItems, txtOrderTotal;
        Button btnViewDetail, btnUpdateStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtOrderStatus = itemView.findViewById(R.id.txtOrderStatus);
            txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
            txtOrderItems = itemView.findViewById(R.id.txtOrderItems);
            txtOrderTotal = itemView.findViewById(R.id.txtOrderTotal);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }

        void bind(Order order) {
            // Order ID
            String orderId = order.getId();
            if (orderId != null && orderId.length() > 8) {
                orderId = "#" + orderId.substring(0, 8);
            }
            txtOrderId.setText("Đơn hàng " + (orderId != null ? orderId : ""));

            // Status
            String status = order.getStatus() != null ? order.getStatus() : "pending";
            txtOrderStatus.setText(getStatusText(status));
            txtOrderStatus.setBackgroundColor(getStatusColor(status));

            // Date
            String dateStr = "Ngày đặt: ";
            if (order.getCreatedAt() != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date date = sdf.parse(order.getCreatedAt());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    dateStr += outputFormat.format(date);
                } catch (Exception e) {
                    dateStr += order.getCreatedAt();
                }
            }
            txtOrderDate.setText(dateStr);

            // Items count
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            txtOrderItems.setText(itemCount + " sản phẩm");

            // Total
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            txtOrderTotal.setText("Tổng: " + formatter.format(order.getTotalAmount()) + "₫");

            // Click listeners
            btnViewDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetail(order);
                }
            });

            btnUpdateStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateStatus(order);
                }
            });
        }

        private String getStatusText(String status) {
            switch (status.toLowerCase()) {
                case "pending": return "Chờ xử lý";
                case "confirmed": return "Đã xác nhận";
                case "shipping": return "Đang giao";
                case "delivered": return "Đã giao";
                case "cancelled": return "Đã hủy";
                default: return status;
            }
        }

        private int getStatusColor(String status) {
            switch (status.toLowerCase()) {
                case "pending": return 0xFFFF9800; // Orange
                case "confirmed": return 0xFF2196F3; // Blue
                case "shipping": return 0xFF9C27B0; // Purple
                case "delivered": return 0xFF4CAF50; // Green
                case "cancelled": return 0xFFF44336; // Red
                default: return 0xFF757575; // Grey
            }
        }
    }
}

