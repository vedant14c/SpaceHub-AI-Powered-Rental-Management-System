package com.myapplication.office_spaces.activities;


import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.Notification;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.models.PropertyImage;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The backend has no dedicated "Inquiry" entity — the closest existing
 * mechanism is Notification (NotificationType.PROPERTY), sent to the
 * property's owner via POST /notifications.
 */
public class SendInquiryActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "property_id";
    public static final String EXTRA_PROPERTY_TITLE = "property_title";

    private int propertyId;
    private Property property;

    private ImageView imgProperty;
    private TextView txtTitle, txtLocation;
    private TextInputEditText etName, etEmail, etPhone, etMessage;
    private MaterialButton btnSend;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_inquiry);

        propertyId = getIntent().getIntExtra(EXTRA_PROPERTY_ID, -1);
        sessionManager = new SessionManager(this);

        if (propertyId == -1) {
            Toast.makeText(this, "Invalid property.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        prefillFromSession();
        setupClicks();
        loadProperty();
    }

    private void initViews() {
        imgProperty = findViewById(R.id.imgProperty);
        txtTitle = findViewById(R.id.txtTitle);
        txtLocation = findViewById(R.id.txtLocation);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etMessage = findViewById(R.id.etMessage);

        btnSend = findViewById(R.id.btnSend);

        String presetTitle = getIntent().getStringExtra(EXTRA_PROPERTY_TITLE);
        if (presetTitle != null) txtTitle.setText(presetTitle);
    }

    private void prefillFromSession() {
        if (sessionManager.getName() != null) etName.setText(sessionManager.getName());
        if (sessionManager.getEmail() != null) etEmail.setText(sessionManager.getEmail());
    }

    private void setupClicks() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendInquiry());
    }

    private void loadProperty() {
        ApiClient.getApiService(this).getPropertyById(propertyId).enqueue(new Callback<Property>() {
            @Override
            public void onResponse(Call<Property> call, Response<Property> response) {
                if (response.isSuccessful() && response.body() != null) {
                    property = response.body();
                    txtTitle.setText(property.getTitle());

                    String location = (property.getCity() != null ? property.getCity() : "")
                            + (property.getState() != null ? ", " + property.getState() : "");
                    txtLocation.setText(location);
                }
            }

            @Override
            public void onFailure(Call<Property> call, Throwable t) {
                // Non-fatal — the pre-filled title/id is still enough to send the inquiry.
            }
        });

        ApiClient.getApiService(this).getImagesByPropertyId(propertyId).enqueue(new Callback<List<PropertyImage>>() {
            @Override
            public void onResponse(Call<List<PropertyImage>> call, Response<List<PropertyImage>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Glide.with(SendInquiryActivity.this).load(response.body().get(0).getImageUrl()).into(imgProperty);
                }
            }

            @Override
            public void onFailure(Call<List<PropertyImage>> call, Throwable t) { /* keep placeholder */ }
        });
    }

    private void sendInquiry() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String message = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";

        if (name.isEmpty()) {
            etName.setError("Enter your name");
            etName.requestFocus();
            return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (message.isEmpty()) {
            etMessage.setError("Enter a message");
            etMessage.requestFocus();
            return;
        }

        if (property == null || property.getOwnerId() == null) {
            Toast.makeText(this, "Property details still loading — try again in a moment.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = "New Inquiry: " + property.getTitle();
        String body = String.format(
                "From: %s\nEmail: %s\nPhone: %s\n\n%s",
                name, email, phone.isEmpty() ? "-" : phone, message);

        Notification notification = new Notification(property.getOwnerId(), title, body, "PROPERTY");

        btnSend.setEnabled(false);
        btnSend.setText("Sending...");

        ApiClient.getApiService(this).addNotification(notification).enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {
                btnSend.setEnabled(true);
                btnSend.setText("Send Inquiry");

                if (response.isSuccessful()) {
                    Toast.makeText(SendInquiryActivity.this,
                            "Inquiry sent! The owner will get back to you soon.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(SendInquiryActivity.this,
                            "Couldn't send your inquiry. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Notification> call, Throwable t) {
                btnSend.setEnabled(true);
                btnSend.setText("Send Inquiry");
                Toast.makeText(SendInquiryActivity.this,
                        "Couldn't reach the server. Check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}