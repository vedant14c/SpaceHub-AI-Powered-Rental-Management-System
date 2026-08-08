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
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.RegisterRequest;
import com.myapplication.office_spaces.network.ApiClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputEditText etName, etEmail, etMobile, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView txtLogin;

    // Role chosen on RoleSelectionActivity. Defaults to USER if this screen
    // is opened directly (e.g. from Login's "Register" link).
    private String selectedRole = "USER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getIntent().hasExtra(RoleSelectionActivity.EXTRA_ROLE)) {
            selectedRole = getIntent().getStringExtra(RoleSelectionActivity.EXTRA_ROLE);
        }

        initViews();
        clickEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);
    }

    private void clickEvents() {
        btnBack.setOnClickListener(v -> finish());

        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void validateAndRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) { etName.setError("Enter Full Name"); etName.requestFocus(); return; }
        if (email.isEmpty()) { etEmail.setError("Enter Email"); etEmail.requestFocus(); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Enter Valid Email"); etEmail.requestFocus(); return; }
        if (mobile.isEmpty()) { etMobile.setError("Enter Mobile Number"); etMobile.requestFocus(); return; }
        if (mobile.length() != 10) { etMobile.setError("Enter Valid Mobile Number"); etMobile.requestFocus(); return; }
        if (password.isEmpty()) { etPassword.setError("Enter Password"); etPassword.requestFocus(); return; }
        if (password.length() < 6) { etPassword.setError("Password must contain at least 6 characters"); etPassword.requestFocus(); return; }
        if (confirmPassword.isEmpty()) { etConfirmPassword.setError("Confirm Password"); etConfirmPassword.requestFocus(); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Password does not match"); etConfirmPassword.requestFocus(); return; }

        registerUser(name, email, mobile, password);
    }

    private void registerUser(String name, String email, String mobile, String password) {
        setLoading(true);

        RegisterRequest request = new RegisterRequest(name, email, password, mobile, selectedRole);

        ApiClient.getApiService(this).register(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registration Successful. Please login.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed. That email may already be in use.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Couldn't reach the server. Check your connection and try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
        btnRegister.setText(loading ? "Registering..." : "Create Account");
    }
}