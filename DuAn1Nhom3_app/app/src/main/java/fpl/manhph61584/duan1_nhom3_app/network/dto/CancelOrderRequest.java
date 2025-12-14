package fpl.manhph61584.duan1_nhom3_app.network.dto;

public class CancelOrderRequest {
    private String cancelReason;

    public CancelOrderRequest() {
    }

    public CancelOrderRequest(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}








