package fpl.manhph61584.duan1_nhom3_app;

public class Voucher {
    private String _id;
    private String code;
    private String name;
    private String description;
    private double discount; // Số tiền giảm hoặc phần trăm
    private String discountType; // "amount" hoặc "percent"
    private double minPurchase; // Giá trị đơn hàng tối thiểu
    private double maxDiscount; // Giảm giá tối đa (nếu là phần trăm)
    private int usageLimit; // Số lần sử dụng tối đa
    private int usedCount; // Số lần đã sử dụng
    private String startDate;
    private String endDate;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;

    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public double getMinPurchase() { return minPurchase; }
    public void setMinPurchase(double minPurchase) { this.minPurchase = minPurchase; }

    public double getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(double maxDiscount) { this.maxDiscount = maxDiscount; }

    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}

