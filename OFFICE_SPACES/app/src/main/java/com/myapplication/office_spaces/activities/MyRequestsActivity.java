package com.myapplication.office_spaces.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.MyRequestAdapter;
import com.myapplication.office_spaces.models.CreateOrderRequest;
import com.myapplication.office_spaces.models.CreateOrderResponse;
import com.myapplication.office_spaces.models.Payment;
import com.myapplication.office_spaces.models.PropertyRequest;
import com.myapplication.office_spaces.models.VerifyPaymentRequest;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyRequestsActivity extends AppCompatActivity
        implements PaymentResultWithDataListener {

    private RecyclerView recyclerView;
    private android.view.View layoutEmpty;
    private MyRequestAdapter adapter;

    private final List<PropertyRequest> requestList = new ArrayList<>();

    private SessionManager sessionManager;

    private int currentRequestId = -1;
    private String currentOrderId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        Checkout.preload(getApplicationContext());

        sessionManager = new SessionManager(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Requests");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerRequests);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyRequestAdapter(
                this,
                requestList,
                request -> {

                    currentRequestId = request.getRequestId();

                    CreateOrderRequest body =
                            new CreateOrderRequest(sessionManager.getUserId());

                    ApiClient.getApiService(this)
                            .createOrder(request.getRequestId(), body)
                            .enqueue(new Callback<CreateOrderResponse>() {

                                @Override
                                public void onResponse(Call<CreateOrderResponse> call,
                                                       Response<CreateOrderResponse> response) {

                                    Log.e("PAYMENT", "HTTP Code = " + response.code());
                                    Log.e("PAYMENT", "Successful = " + response.isSuccessful());

                                    if (response.errorBody() != null) {
                                        try {
                                            Log.e("PAYMENT",
                                                    response.errorBody().string());
                                        } catch (Exception ignored) {
                                        }
                                    }

                                    if (!response.isSuccessful()
                                            || response.body() == null) {

                                        Toast.makeText(
                                                MyRequestsActivity.this,
                                                "Unable to create payment order",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        return;
                                    }

                                    CreateOrderResponse order = response.body();

                                    Log.e("PAYMENT", "keyId = " + order.getKeyId());
                                    Log.e("PAYMENT", "orderId = " + order.getOrderId());
                                    Log.e("PAYMENT", "currency = " + order.getCurrency());
                                    Log.e("PAYMENT", "amount = " + order.getAmount());

                                    currentOrderId = order.getOrderId();

                                    Checkout checkout = new Checkout();
                                    checkout.setKeyID(order.getKeyId());

                                    try {

                                        JSONObject options = new JSONObject();

                                        options.put("name", "Office Spaces");
                                        options.put("description", "Office Booking Payment");
                                        options.put("currency", order.getCurrency());
                                        options.put("amount", order.getAmount());
                                        options.put("order_id", order.getOrderId());

                                        checkout.open(
                                                MyRequestsActivity.this,
                                                options
                                        );

                                    } catch (Exception e) {

                                        Log.e("PAYMENT",
                                                "Checkout Error",
                                                e);

                                        Toast.makeText(
                                                MyRequestsActivity.this,
                                                e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }

                                @Override
                                public void onFailure(
                                        Call<CreateOrderResponse> call,
                                        Throwable t) {

                                    Log.e("PAYMENT", "Failure", t);

                                    Toast.makeText(
                                            MyRequestsActivity.this,
                                            t.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            });
                });

        recyclerView.setAdapter(adapter);

        loadRequests();
    }
    private void loadRequests() {

        ApiClient.getApiService(this)
                .getMyRequests(sessionManager.getUserId())
                .enqueue(new Callback<List<PropertyRequest>>() {

                    @Override
                    public void onResponse(
                            Call<List<PropertyRequest>> call,
                            Response<List<PropertyRequest>> response) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            requestList.clear();
                            requestList.addAll(response.body());

                            adapter.notifyDataSetChanged();
                            layoutEmpty.setVisibility(requestList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);

                        } else {

                            Toast.makeText(
                                    MyRequestsActivity.this,
                                    "Unable to load requests",
                                    Toast.LENGTH_SHORT
                            ).show();
                            layoutEmpty.setVisibility(android.view.View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<PropertyRequest>> call,
                            Throwable t) {

                        Log.e("PAYMENT", "Load Requests Failed", t);

                        Toast.makeText(
                                MyRequestsActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
    @Override
    public void onPaymentSuccess(String razorpayPaymentId,
                                 PaymentData paymentData) {

        VerifyPaymentRequest request = new VerifyPaymentRequest();

        request.setRequestId(currentRequestId);
        request.setRazorpayOrderId(paymentData.getOrderId());
        request.setRazorpayPaymentId(paymentData.getPaymentId());
        request.setRazorpaySignature(paymentData.getSignature());

        ApiClient.getApiService(this)
                .verifyPayment(request)
                .enqueue(new Callback<Payment>() {

                    @Override
                    public void onResponse(Call<Payment> call,
                                           Response<Payment> response) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    MyRequestsActivity.this,
                                    "Payment Successful",
                                    Toast.LENGTH_LONG
                            ).show();

                            loadRequests();

                        } else {

                            Toast.makeText(
                                    MyRequestsActivity.this,
                                    "Payment Verification Failed",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Payment> call,
                                          Throwable t) {

                        Log.e("PAYMENT", "Verify Payment Failed", t);

                        Toast.makeText(
                                MyRequestsActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    @Override
    public void onPaymentError(int code,
                               String message,
                               PaymentData paymentData) {

        Log.e("PAYMENT",
                "Payment Error : " + code + " : " + message);

        Toast.makeText(
                this,
                "Payment Failed : " + message,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}