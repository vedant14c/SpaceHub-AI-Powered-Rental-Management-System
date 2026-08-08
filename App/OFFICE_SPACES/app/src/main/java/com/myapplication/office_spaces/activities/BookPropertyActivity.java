package com.myapplication.office_spaces.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.models.PropertyImage;
import com.myapplication.office_spaces.models.PropertyRequest;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookPropertyActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "property_id";

    private int propertyId;
    private Property property;

    private final Calendar selectedDate = Calendar.getInstance();
    private final Calendar selectedTime = Calendar.getInstance();
    private boolean dateChosen = false;
    private boolean timeChosen = false;

    private static final double[] DURATION_MONTHS = {0.25, 1, 3, 6, 12};

    private ImageView imgProperty;
    private TextView txtTitle, txtLocation, txtPrice;
    private TextView txtSelectedDate, txtSelectedTime;
    private AutoCompleteTextView dropdownDuration;
    private TextInputEditText etMessage;
    private TextView txtTotalAmount;
    private MaterialButton btnConfirm;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_property);

        propertyId = getIntent().getIntExtra(EXTRA_PROPERTY_ID, -1);
        sessionManager = new SessionManager(this);

        if (propertyId == -1) {
            Toast.makeText(this, "Invalid property.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupDurationDropdown();
        setupClicks();
        loadProperty();
    }

    private void initViews() {
        imgProperty = findViewById(R.id.imgProperty);
        txtTitle = findViewById(R.id.txtTitle);
        txtLocation = findViewById(R.id.txtLocation);
        txtPrice = findViewById(R.id.txtPrice);

        txtSelectedDate = findViewById(R.id.txtSelectedDate);
        txtSelectedTime = findViewById(R.id.txtSelectedTime);
        dropdownDuration = findViewById(R.id.dropdownDuration);

        etMessage = findViewById(R.id.etMessage);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    private void setupDurationDropdown() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.booking_durations, android.R.layout.simple_dropdown_item_1line);
        dropdownDuration.setAdapter(adapter);
        dropdownDuration.setText("1 Month", false);
        dropdownDuration.setOnItemClickListener((parent, view, position, id) -> recalculateTotal());
    }

    private void setupClicks() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.rowDate).setOnClickListener(v -> showDatePicker());
        findViewById(R.id.rowTime).setOnClickListener(v -> showTimePicker());
        btnConfirm.setOnClickListener(v -> confirmBooking());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            dateChosen = true;
            txtSelectedDate.setText(String.format(Locale.getDefault(), "%02d %s %d",
                    dayOfMonth, monthName(month), year));
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedTime.set(Calendar.MINUTE, minute);
            timeChosen = true;
            txtSelectedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, selectedTime.get(Calendar.HOUR_OF_DAY), selectedTime.get(Calendar.MINUTE), false)
                .show();
    }

    private String monthName(int monthIndex) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[monthIndex];
    }

    private void loadProperty() {
        ApiClient.getApiService(this).getPropertyById(propertyId).enqueue(new Callback<Property>() {
            @Override
            public void onResponse(@NonNull Call<Property> call, @NonNull Response<Property> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(BookPropertyActivity.this, "Property not found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                property = response.body();
                bindProperty(property);
            }

            @Override
            public void onFailure(@NonNull Call<Property> call, @NonNull Throwable t) {
                Toast.makeText(BookPropertyActivity.this, "Couldn't load property.", Toast.LENGTH_SHORT).show();
            }
        });

        ApiClient.getApiService(this).getImagesByPropertyId(propertyId).enqueue(new Callback<List<PropertyImage>>() {
            @Override
            public void onResponse(@NonNull Call<List<PropertyImage>> call, @NonNull Response<List<PropertyImage>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Glide.with(BookPropertyActivity.this).load(response.body().get(0).getImageUrl()).into(imgProperty);
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<PropertyImage>> call, @NonNull Throwable t) { }
        });
    }

    private void bindProperty(Property p) {
        txtTitle.setText(p.getTitle());
        String location = (p.getCity() != null ? p.getCity() : "") + (p.getState() != null ? ", " + p.getState() : "");
        txtLocation.setText(location);

        String unit = p.getPriceUnit() != null ? " / " + p.getPriceUnit() : "";
        if (p.getPrice() != null) {
            txtPrice.setText(String.format(Locale.getDefault(), "\u20B9%,.0f%s", p.getPrice(), unit));
        }
        recalculateTotal();
    }

    private void recalculateTotal() {
        if (property == null || property.getPrice() == null) return;
        String duration = dropdownDuration.getText().toString();
        int index = indexOfDuration(duration);
        double months = index >= 0 ? DURATION_MONTHS[index] : 1.0;
        double total = property.getPrice() * months;
        txtTotalAmount.setText(String.format(Locale.getDefault(), "\u20B9%,.0f", total));
    }

    private int indexOfDuration(String duration) {
        String[] options = getResources().getStringArray(R.array.booking_durations);
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(duration)) return i;
        }
        return -1;
    }

    private void confirmBooking() {
        if (property == null) return;
        if (!dateChosen || !timeChosen) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        PropertyRequest request = new PropertyRequest();
        request.setPropertyId(propertyId);
        request.setUserId(sessionManager.getUserId());
        
        // Standardize Request Type for Backend Enum: [RENTAL, PURCHASE]
        String listingType = property.getListingType();
        if (listingType != null && listingType.equalsIgnoreCase("Buy")) {
            request.setRequestType("PURCHASE");
        } else {
            request.setRequestType("RENTAL");
        }

        String message = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        request.setMessage(message.isEmpty() ? null : message);

        String duration = dropdownDuration.getText().toString();
        int index = indexOfDuration(duration);
        double months = index >= 0 ? DURATION_MONTHS[index] : 1.0;
        request.setOfferPrice(property.getPrice() * months);

        String startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH));
        request.setProposedStart(startDate);

        Calendar end = (Calendar) selectedDate.clone();
        end.add(Calendar.MONTH, (int) Math.ceil(months));
        String endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                end.get(Calendar.YEAR), end.get(Calendar.MONTH) + 1, end.get(Calendar.DAY_OF_MONTH));
        request.setProposedEnd(endDate);

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Booking...");

        ApiClient.getApiService(this).addRequest(request).enqueue(new Callback<PropertyRequest>() {
            @Override
            public void onResponse(@NonNull Call<PropertyRequest> call, @NonNull Response<PropertyRequest> response) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Confirm Booking");
                if (response.isSuccessful()) {
                    Toast.makeText(BookPropertyActivity.this, "Request sent!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(BookPropertyActivity.this, "Failed to book (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PropertyRequest> call, @NonNull Throwable t) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Confirm Booking");
                Toast.makeText(BookPropertyActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}