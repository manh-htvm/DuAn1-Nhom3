package fpl.manhph61584.duan1_nhom3_app.network;

import java.util.List;
import java.util.Map;

import fpl.manhph61584.duan1_nhom3_app.Category; 
import fpl.manhph61584.duan1_nhom3_app.Order; 
import fpl.manhph61584.duan1_nhom3_app.Product;

public interface ApiService {

    // Auth endpoints
    @POST("users/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("users/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Category endpoints
    @GET("categories")
    Call<List<Category>> getCategories();

    // Product endpoints
    @GET("products")
    Call<List<Product>> getProducts(@Query("search") String search, @Query("category") String categoryId);

    @GET("products/{id}")
    Call<Product> getProductDetail(@Path("id") String id);

    // Review endpoints 
    @GET("reviews/product/{productId}")
    Call<List<Review>> getProductReviews(@Path("productId") String productId);

    @GET("reviews/product/{productId}/rating")
    Call<ProductRatingResponse> getProductRating(@Path("productId") String productId);

    @POST("reviews")
    Call<ReviewResponse> createReview(@Header("Authorization") String token, @Body ReviewRequest request);

    @POST("reviews/{reviewId}/reply")
    Call<Review> replyReview(@Header("Authorization") String token, @Path("reviewId") String reviewId, @Body ReplyRequest request);

    @DELETE("reviews/{reviewId}")
    Call<ReviewResponse> deleteReview(@Header("Authorization") String token, @Path("reviewId") String reviewId);


    @GET("cart")
    Call<CartResponse> getCart(@Header("Authorization") String token);

    @POST("cart")
    Call<CartResponse> addToCart(@Header("Authorization") String token, @Body AddToCartRequest request);

    @DELETE("cart/{productId}")
    Call<CartResponse> removeFromCart(@Header("Authorization") String token, @Path("productId") String productId);

    // Voucher endpoints 
    @GET("vouchers")
    Call<List<Voucher>> getVouchers(@Header("Authorization") String token); // Token can be null, server handles optional auth
    
    @GET("vouchers/public")
    Call<List<Voucher>> getPublicVouchers();

    // User profile endpoints 
    @PUT("users/profile")
    Call<UpdateProfileResponse> updateProfile(@Header("Authorization") String token, @Body UpdateProfileRequest request);

    // Upload endpoints 
    @Multipart
    @POST("upload")
    Call<UploadResponse> uploadImage(@Part MultipartBody.Part image);

    // Order endpoints 
    @GET("orders/{userId}")
    Call<List<Order>> getOrders(@Path("userId") String userId);

    @POST("orders")
    Call<Map<String, Object>> createOrder(@Body Map<String, Object> body);
}