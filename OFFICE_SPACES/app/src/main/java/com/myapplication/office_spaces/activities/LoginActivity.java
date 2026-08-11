package com.myapplication.office_spaces.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.LoginRequest;
import com.myapplication.office_spaces.models.LoginResponse;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView txtForgot, txtRegister;
    private ImageButton btnBack;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        initViews();
        clickEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtForgot = findViewById(R.id.txtForgot);
        txtRegister = findViewById(R.id.txtRegister);

    }

    private void clickEvents() {
        btnBack.setOnClickListener(v -> finish());

        btnLogin.setOnClickListener(view -> attemptLogin());

        txtRegister.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });




    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Enter Email");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter Valid Email");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Enter Password");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(email, password);

        ApiClient.getApiService(this).login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                LoginResponse body = response.body();

                if (!body.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, body.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                sessionManager.saveSession(body.getUserId(), body.getName(), body.getEmail(), body.getRole(), body.getToken());

                // Send FCM token to server
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {

                            System.out.println("========== FCM ==========");

                            if (!task.isSuccessful()) {
                                System.out.println("FAILED TO GET TOKEN");
                                task.getException().printStackTrace();
                                return;
                            }

                            String fcmToken = task.getResult();

                            System.out.println("FCM TOKEN = " + fcmToken);

                            ApiClient.getApiService(LoginActivity.this)
                                    .updateFcmToken(fcmToken)
                                    .enqueue(new Callback<ResponseBody>() {

                                        @Override
                                        public void onResponse(Call<ResponseBody> call,
                                                               Response<ResponseBody> response) {

                                            System.out.println("UPDATE RESPONSE = " + response.code());

                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call,
                                                              Throwable t) {

                                            System.out.println("UPDATE FAILED");
                                            t.printStackTrace();
                                        }
                                    });
                        });

                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                Intent intent;

                String role = body.getRole();

                if ("OWNER".equalsIgnoreCase(role)) {

                    intent = new Intent(LoginActivity.this, OwnerMainActivity.class);

                } else if ("ADMIN".equalsIgnoreCase(role)) {

                    intent = new Intent(LoginActivity.this, AdminMainActivity.class);

                } else if ("RENTER".equalsIgnoreCase(role)
        || "BUYER".equalsIgnoreCase(role)
        || "USER".equalsIgnoreCase(role)) {

                intent = new Intent(LoginActivity.this, RenterMainActivity.class);

                } else {

                    Toast.makeText(
                            LoginActivity.this,
                            "Unknown user role: " + role,
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Couldn't reach the server. Check your connection and try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Logging in..." : "LOGIN");
    }
}