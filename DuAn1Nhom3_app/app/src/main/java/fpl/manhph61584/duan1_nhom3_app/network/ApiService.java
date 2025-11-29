package fpl.manhph61584.duan1_nhom3_app.network;

import java.util.List;

import fpl.manhph61584.duan1_nhom3_app.Order;
import fpl.manhph61584.duan1_nhom3_app.Product;
import fpl.manhph61584.duan1_nhom3_app.Voucher;
import fpl.manhph61584.duan1_nhom3_app.network.dto.LoginRequest;
import fpl.manhph61584.duan1_nhom3_app.network.dto.LoginResponse;
import fpl.manhph61584.duan1_nhom3_app.network.dto.RegisterRequest;
import fpl.manhph61584.duan1_nhom3_app.network.dto.RegisterResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Auth endpoints
    @POST("users/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("users/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Product endpoints
    @GET("products")
    Call<List<Product>> getProducts(@Query("search") String search);

    @GET("products/{id}")
    Call<Product> getProductDetail(@Path("id") String id);

    // Order endpoints
    @GET("orders")
    Call<List<Order>> getAllOrders();

    @GET("orders/{id}")
    Call<Order> getOrderDetail(@Path("id") String id);

    @PUT("orders/{id}/status")
    Call<Order> updateOrderStatus(@Path("id") String id, @Body OrderStatusUpdateRequest request);

    // Voucher endpoints
    @GET("vouchers")
    Call<List<Voucher>> getAllVouchers();

    @GET("vouchers/{id}")
    Call<Voucher> getVoucherDetail(@Path("id") String id);

    @POST("vouchers")
    Call<Voucher> createVoucher(@Body Voucher voucher);

    @PUT("vouchers/{id}")
    Call<Voucher> updateVoucher(@Path("id") String id, @Body Voucher voucher);

    @DELETE("vouchers/{id}")
    Call<Void> deleteVoucher(@Path("id") String id);

    // Request classes
    class OrderStatusUpdateRequest {
        private String status;

        public OrderStatusUpdateRequest(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

