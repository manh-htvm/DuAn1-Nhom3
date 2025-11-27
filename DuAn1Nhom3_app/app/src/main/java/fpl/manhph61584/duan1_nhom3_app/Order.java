package fpl.manhph61584.duan1_nhom3_app;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Order implements Serializable {

    private String _id;
    private List<OrderItem> items;
    private double totalAmount;
    private Map<String, Object> shippingAddress;

    // Getter
    public String getId() { return _id; }
    public List<OrderItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public Map<String, Object> getShippingAddress() { return shippingAddress; }

    // Tính tổng giá hiển thị trong Adapter (nếu cần)
    public double getTotalPrice() {
        return totalAmount;
    }

    // Flatten tất cả OrderItem
    public void flattenItems() {
        if (items != null) {
            for (OrderItem item : items) {
                item.flatten();
            }
        }
    }
}
