package fpl.manhph61584.duan1_nhom3_app.network;

import java.util.List;

import fpl.manhph61584.duan1_nhom3_app.Category;
import fpl.manhph61584.duan1_nhom3_app.Product;
import fpl.manhph61584.duan1_nhom3_app.Review;
import fpl.manhph61584.duan1_nhom3_app.Voucher;
import fpl.manhph61584.duan1_nhom3_app.network.dto.LoginRequest;
import fpl.manhph61584.duan1_nhom3_app.network.dto.LoginResponse;
import fpl.manhph61584.duan1_nhom3_app.network.dto.ProductRatingResponse;
import fpl.manhph61584.duan1_nhom3_app.network.dto.RegisterRequest;
import fpl.manhph61584.duan1_nhom3_app.network.dto.RegisterResponse;
import fpl.manhph61584.duan1_nhom3_app.network.dto.AddToCartRequest;
import fpl.manhph61584.duan1_nhom3_app.network.dto.CartResponse;
import fpl.manhph61584.duan1_nhom3_app.network.dto.ReviewRequest;
import fpl.manhph61584.duan1_nhom3_app.network.dto.ReviewResponse;
import fpl.manhph61584.duan1_nhom3_app.network.dto.ReplyRequest;
import fpl.manhph61584.duan1_nhom3_app.network.dto.UpdateProfileRequest;
import fpl.manhph61584.duan1_nhom3_app.network.dto.UpdateProfileResponse;
import fpl.manhph61584.duan1_nhom3_app.network.dto.UploadResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    // Cart endpoints
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
}







