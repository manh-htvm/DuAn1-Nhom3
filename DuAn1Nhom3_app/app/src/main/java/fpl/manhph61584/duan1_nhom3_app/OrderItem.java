package fpl.manhph61584.duan1_nhom3_app;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Map;

public class OrderItem implements Serializable {

    @SerializedName("product")
    private Object productRaw; // String (productId) hoặc Product object

    private int quantity;

    private String productId;
    private String name;
    private String image;
    private double price;

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void flatten() {
        if (productRaw == null) return;

        if (productRaw instanceof Map) { // Gson sẽ parse object thành Map
            Map<String, Object> p = (Map<String, Object>) productRaw;
            productId = p.get("_id") != null ? p.get("_id").toString() : "";
            name = p.get("name") != null ? p.get("name").toString() : "";
            image = p.get("image") != null ? p.get("image").toString() : "";
            price = p.get("price") != null ? Double.parseDouble(p.get("price").toString()) : 0;
        } else if (productRaw instanceof String) {
            productId = (String) productRaw;
        }
    }

}
