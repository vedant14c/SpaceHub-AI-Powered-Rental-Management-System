package com.myapplication.office_spaces.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.ReviewAdapter;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.models.PropertyImage;
import com.myapplication.office_spaces.models.PublicUserView;
import com.myapplication.office_spaces.models.Review;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropertyDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "property_id";

    private ImageView imgHeader, btnFavorite;
    private TextView txtTitle, txtRating, txtLocation, txtPrice, txtMaintenance;
    private TextView txtStatArea, txtStatType, txtStatFloor;
    private TextView txtDescription, txtFullAddress;
    private ChipGroup chipGroupAmenities;
    private RecyclerView recyclerReviews;
    private ReviewAdapter reviewAdapter;
    private com.google.android.material.button.MaterialButton btnViewOnMap;

    private int propertyId;
    private Property currentProperty;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        propertyId = getIntent().getIntExtra(EXTRA_PROPERTY_ID, -1);
        sessionManager = new SessionManager(this);

        if (propertyId == -1) {
            Toast.makeText(this, "Invalid property.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClicks();
        loadProperty();
        loadImages();
        loadReviews();
    }

    private void initViews() {
        imgHeader = findViewById(R.id.imgHeader);
        btnFavorite = findViewById(R.id.btnFavorite);

        txtTitle = findViewById(R.id.txtTitle);
        txtRating = findViewById(R.id.txtRating);
        txtLocation = findViewById(R.id.txtLocation);
        txtPrice = findViewById(R.id.txtPrice);
        txtMaintenance = findViewById(R.id.txtMaintenance);

        txtStatArea = findViewById(R.id.txtStatArea);
        txtStatType = findViewById(R.id.txtStatType);
        txtStatFloor = findViewById(R.id.txtStatFloor);

        txtDescription = findViewById(R.id.txtDescription);
        txtFullAddress = findViewById(R.id.txtFullAddress);
        btnViewOnMap = findViewById(R.id.btnViewOnMap);
        chipGroupAmenities = findViewById(R.id.chipGroupAmenities);
        recyclerReviews = findViewById(R.id.recyclerReviews);

        reviewAdapter = new ReviewAdapter();
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setAdapter(reviewAdapter);
    }

    private void setupClicks() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnFavorite.setOnClickListener(v -> {
            if (currentProperty != null) {
                boolean newFavState = !currentProperty.isFavorite();
                currentProperty.setFavorite(newFavState);
                sessionManager.setFavorite(propertyId, newFavState);
                btnFavorite.setImageResource(newFavState ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            }
        });

        findViewById(R.id.btnCall).setOnClickListener(v -> {
            if (currentProperty != null) {
                loadOwnerPhoneAndDial(currentProperty.getOwnerId());
            }
        });

        findViewById(R.id.btnMessage).setOnClickListener(v -> {
            Intent intent = new Intent(this, SendInquiryActivity.class);
            intent.putExtra(SendInquiryActivity.EXTRA_PROPERTY_ID, propertyId);
            if (currentProperty != null) {
                intent.putExtra(SendInquiryActivity.EXTRA_PROPERTY_TITLE, currentProperty.getTitle());
            }
            startActivity(intent);
        });

        findViewById(R.id.btnBookNow).setOnClickListener(v -> {
            Intent intent = new Intent(this, BookPropertyActivity.class);
            intent.putExtra(BookPropertyActivity.EXTRA_PROPERTY_ID, propertyId);
            startActivity(intent);
        });

        btnViewOnMap.setOnClickListener(v -> {
            if (currentProperty != null) {
                openGoogleMaps(currentProperty.getLatitude(), currentProperty.getLongitude(), currentProperty.getTitle());
            }
        });
    }

    private void loadProperty() {
        ApiClient.getApiService(this).getPropertyById(propertyId).enqueue(new Callback<Property>() {
            @Override
            public void onResponse(Call<Property> call, Response<Property> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(PropertyDetailsActivity.this, "Property not found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentProperty = response.body();
                currentProperty.setFavorite(sessionManager.isFavorite(propertyId));
                bindProperty(currentProperty);
            }

            @Override
            public void onFailure(Call<Property> call, Throwable t) {
                Toast.makeText(PropertyDetailsActivity.this,
                        "Couldn't load property. Check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProperty(Property p) {
        txtTitle.setText(p.getTitle());

        btnFavorite.setImageResource(p.isFavorite() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

        String location = (p.getCity() != null ? p.getCity() : "")
                + (p.getState() != null ? ", " + p.getState() : "");
        txtLocation.setText(location);

        if (p.getPrice() != null) {
            String unit = p.getPriceUnit() != null ? " / " + p.getPriceUnit() : "";
            txtPrice.setText(String.format(Locale.getDefault(), "₹%,.0f%s", p.getPrice(), unit));
        }

        // Backend has no maintenance field — hide it.
        txtMaintenance.setVisibility(android.view.View.GONE);

        if (p.getAreaSqft() != null) {
            txtStatArea.setText(String.format(Locale.getDefault(), "%,.0f", p.getAreaSqft()));
        }
        txtStatType.setText(p.getPropertyType() != null ? p.getPropertyType() : "-");

        if (p.getFloorNumber() != null && p.getTotalFloors() != null) {
            txtStatFloor.setText(String.format(Locale.getDefault(), "%d / %d", p.getFloorNumber(), p.getTotalFloors()));
        } else {
            txtStatFloor.setText("-");
        }

        txtDescription.setText(p.getDescription() != null ? p.getDescription() : "No description provided.");

        String fullAddress = (p.getAddress() != null ? p.getAddress() : "")
                + (p.getCity() != null ? ", " + p.getCity() : "")
                + (p.getState() != null ? ", " + p.getState() : "");
        txtFullAddress.setText(fullAddress.isEmpty() ? "Address not available" : fullAddress);

        chipGroupAmenities.removeAllViews();
        addChip(p.getPropertyType());
        addChip(p.getListingType());
        if (p.getStatus() != null) addChip(p.getStatus());
    }

    private void addChip(String label) {
        if (label == null || label.isEmpty()) return;
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setChipBackgroundColorResource(R.color.lightBlue);
        chip.setTextColor(ContextCompat.getColor(this, R.color.primary));
        chip.setClickable(false);
        chip.setCheckable(false);
        chipGroupAmenities.addView(chip);
    }

    private void loadImages() {
        ApiClient.getApiService(this)
                .getImagesByPropertyId(propertyId)
                .enqueue(new Callback<List<PropertyImage>>() {

                    @Override
                    public void onResponse(Call<List<PropertyImage>> call,
                                           Response<List<PropertyImage>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {

                            String imagePath = response.body().get(0).getImageUrl();

                            String imageUrl;

                            if (imagePath.startsWith("http")) {
                                imageUrl = imagePath;
                            } else {
                                imageUrl = ApiClient.BASE_URL
                                        + imagePath.replaceFirst("^/", "");
                            }

                            Log.d("DETAIL_IMAGE", imageUrl);

                            Glide.with(PropertyDetailsActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.placeholder_office)
                                    .error(R.drawable.placeholder_office)
                                    .centerCrop()
                                    .into(imgHeader);

                        } else {

                            imgHeader.setImageResource(R.drawable.placeholder_office);

                        }
                    }

                    @Override
                    public void onFailure(Call<List<PropertyImage>> call,
                                          Throwable t) {

                        imgHeader.setImageResource(R.drawable.placeholder_office);

                        Log.e("DETAIL_IMAGE", "Failed to load", t);
                    }
                });
    }

    private void loadReviews() {
        ApiClient.getApiService(this).getReviewsByProperty(propertyId).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    txtRating.setText("No reviews yet");
                    findViewById(R.id.txtReviewHeader).setVisibility(android.view.View.GONE);
                    recyclerReviews.setVisibility(android.view.View.GONE);
                    return;
                }

                List<Review> reviews = response.body();
                float sum = 0;
                for (Review r : reviews) {
                    if (r.getRating() != null) sum += r.getRating();
                }
                float avg = sum / reviews.size();
                txtRating.setText(String.format(Locale.getDefault(), "%.1f (%d)", avg, reviews.size()));

                findViewById(R.id.txtReviewHeader).setVisibility(android.view.View.VISIBLE);
                recyclerReviews.setVisibility(android.view.View.VISIBLE);
                reviewAdapter.setReviews(reviews);
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                txtRating.setText("");
            }
        });
    }

    private void loadOwnerPhoneAndDial(Integer ownerId) {
        if (ownerId == null) return;

        ApiClient.getApiService(this).getPublicUserProfile(ownerId).enqueue(new Callback<PublicUserView>() {
            @Override
            public void onResponse(Call<PublicUserView> call,
                                   Response<PublicUserView> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getPhone() != null) {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + response.body().getPhone()));
                    startActivity(dialIntent);
                } else {
                    Toast.makeText(PropertyDetailsActivity.this, "Owner phone number unavailable.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PublicUserView> call, Throwable t) {
                Toast.makeText(PropertyDetailsActivity.this, "Couldn't fetch owner details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGoogleMaps(Double lat, Double lng, String label) {
        if (lat == null || lng == null || lat == 0 || lng == 0) {
            Toast.makeText(this, "Location coordinates not available.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Force Satellite mode in external app using &t=k
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + Uri.encode(lat + "," + lng + "(" + label + ")") + "&t=k");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback: browser with satellite mode
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng + "&basemap=satellite")));
        }
    }
}