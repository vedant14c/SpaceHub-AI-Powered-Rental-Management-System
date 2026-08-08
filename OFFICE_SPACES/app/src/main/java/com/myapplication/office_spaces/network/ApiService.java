package com.myapplication.office_spaces.network;


import com.myapplication.office_spaces.models.AdminDashboard;
import com.myapplication.office_spaces.models.CreateOrderRequest;
import com.myapplication.office_spaces.models.CreateOrderResponse;
import com.myapplication.office_spaces.models.LoginRequest;
import com.myapplication.office_spaces.models.LoginResponse;
import com.myapplication.office_spaces.models.Payment;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.models.PropertyImage;
import com.myapplication.office_spaces.models.PublicUserView;
import com.myapplication.office_spaces.models.RegisterRequest;
import com.myapplication.office_spaces.models.Review;
import okhttp3.MultipartBody;
import com.myapplication.office_spaces.models.MyProfileView;
import com.myapplication.office_spaces.models.PropertyRequest;
import com.myapplication.office_spaces.models.Notification;
import com.myapplication.office_spaces.models.OwnerRequestView;
import com.myapplication.office_spaces.models.AdminUserView;
import com.myapplication.office_spaces.models.VerifyPaymentRequest;
import com.myapplication.office_spaces.models.SmartSearchRequest;

import retrofit2.http.Query;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import retrofit2.http.DELETE;
import retrofit2.http.PUT;


public interface ApiService {

    @Multipart
    @POST("properties/images/{id}")
    Call<ResponseBody> uploadPropertyImages(
            @Path("id") int propertyId,
            @Part MultipartBody.Part[] files
    );


    @PUT("requests/status/{id}")
    Call<PropertyRequest> updateRequestStatus(
            @Path("id") int requestId,
            @Query("status") String status
    );
    @GET("requests/owner/{ownerId}")
    Call<List<OwnerRequestView>> getRequestsByOwner(
            @Path("ownerId") int ownerId
    );



    @GET("requests")
    Call<List<PropertyRequest>> getAllRequests(
            @Query("type") String type
    );

    @GET("requests/my/{userId}")
    Call<List<PropertyRequest>> getRequestsByUser(
            @Path("userId") int userId,
            @Query("type") String type
    );

    @POST("notifications")
    Call<Notification> addNotification(@Body Notification notification);

    @GET("notifications/user/{userId}")
    Call<List<Notification>> getNotificationsByUser(
            @Path("userId") int userId
    );

    @POST("requests")
    Call<PropertyRequest> addRequest(@Body PropertyRequest request);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("register")
    Call<ResponseBody> register(@Body RegisterRequest request);

    @GET("properties/approved")
    Call<List<Property>> getApprovedProperties();

    /**
     * AI-based location search (server-side): properties within radiusKm of the given
     * coordinates, nearest first. See PropertyRecommendationService on the backend.
     */
    @POST("search/smart")
    Call<List<Property>> smartSearch(@Body SmartSearchRequest request);

    @GET("properties/nearby")
    Call<List<Property>> getNearbyProperties(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("radiusKm") double radiusKm
    );

    /**
     * AI-based property recommendations (server-side): same-city listings first, then
     * nearest by distance. All params optional.
     */
    @GET("properties/recommended")
    Call<List<Property>> getRecommendedProperties(
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("city") String city
    );

    @GET("properties/{id}")
    Call<Property> getPropertyById(@Path("id") int id);

    @GET("properties/owner/{ownerId}")
    Call<List<Property>> getPropertiesByOwner(@Path("ownerId") int ownerId);

    @GET("properties/images/{id}")
    Call<List<PropertyImage>> getImagesByPropertyId(
            @Path("id") int propertyId
    );

    @GET("reviews/property/{propertyId}")
    Call<List<Review>> getReviewsByProperty(@Path("propertyId") int propertyId);

    @GET("users/me")
    Call<MyProfileView> getMyProfile();

    @PUT("users/me/preferences")
    Call<MyProfileView> updatePreferences(@Body MyProfileView profile);

    @GET("users/{userId}")
    Call<PublicUserView> getPublicUserProfile(@Path("userId") int userId);

    @GET("requests/my/{userId}")
    Call<List<PropertyRequest>> getMyRequests(
            @Path("userId") int userId
    );

    @GET("notifications/user/{userId}")
    Call<List<Notification>> getNotifications(
            @Path("userId") int userId
    );

    @PUT("notifications/read/{id}")
    Call<Notification> markNotificationRead(
            @Path("id") int id
    );





    // PROPERTIES
    @GET("properties")
    Call<List<Property>> getAllProperties();



    @POST("properties")
    Call<Property> addProperty(@Body Property property);

    @PUT("properties/{id}")
    Call<Property> updateProperty(
            @Path("id") int id,
            @Body Property property
    );

    @DELETE("properties/{id}")
    Call<String> deleteProperty(@Path("id") int id);

    @PUT("properties/approve/{id}")
    Call<Property> approveProperty(@Path("id") int id);

    @PUT("properties/reject/{id}")
    Call<Property> rejectProperty(@Path("id") int id);

    @GET("/users/dashboard")
    Call<AdminDashboard> getDashboardStats();

    // ===================== ADMIN USERS =====================

    @GET("users")
    Call<List<AdminUserView>> getAllUsers();

    @PUT("users/activate/{id}")
    Call<AdminUserView> activateUser(
            @Path("id") int id
    );

    @PUT("users/deactivate/{id}")
    Call<AdminUserView> deactivateUser(
            @Path("id") int id
    );

    @POST("payments/create-order/{requestId}")
    Call<CreateOrderResponse> createOrder(
            @Path("requestId") int requestId,
            @Body CreateOrderRequest request
    );

    @POST("payments/verify")
    Call<Payment> verifyPayment(
            @Body VerifyPaymentRequest request
    );

    @DELETE("properties/images/{imageId}")
    Call<ResponseBody> deletePropertyImage(
            @Path("imageId") int imageId
    );
}