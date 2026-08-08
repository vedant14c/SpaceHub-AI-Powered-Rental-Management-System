package com.myapplication.office_spaces.activities;


import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.SelectedImageAdapter;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddListingActivity extends AppCompatActivity {

    private boolean isEditMode = false;
    private int editingPropertyId = -1;
    private int currentStep = 1;
    private static final int TOTAL_STEPS = 4;

    private View step1Group, step2Group, step3Group, step4Group;
    private TextView step1Circle, step2Circle, step3Circle, step4Circle;
    private MaterialButton btnNext, btnPrevious;

    private TextInputEditText etTitle, etPrice, etArea;
    private AutoCompleteTextView dropdownPropertyType, dropdownListingType, dropdownPriceUnit;

    private TextInputEditText etAddress, etCity, etState, etZipCode;

    private TextInputEditText etDescription, etFloorNumber, etTotalFloors;

    private RecyclerView recyclerSelectedImages;
    private TextView txtReviewSummary;
    private SelectedImageAdapter imageAdapter;

    private SessionManager sessionManager;
    private Integer createdPropertyId = null;

    private final ActivityResultLauncher<String> pickImagesLauncher =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    List<Uri> current = new ArrayList<>(imageAdapter.getImages());
                    current.addAll(uris);
                    imageAdapter.setImages(current);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_listing);

        sessionManager = new SessionManager(this);

        initViews();
        setupDropdowns();
        setupImagePicker();
        setupClicks();
        updateStepUi();

        editingPropertyId = getIntent().getIntExtra("propertyId", -1);

        if (editingPropertyId != -1) {
            isEditMode = true;
            btnNext.setText("Update Listing");
            loadPropertyForEditing();
        }
    }

    private void loadPropertyForEditing() {

        ApiClient.getApiService(this)
                .getPropertyById(editingPropertyId)
                .enqueue(new Callback<Property>() {

                    @Override
                    public void onResponse(Call<Property> call,
                                           Response<Property> response) {

                        if (!response.isSuccessful() || response.body() == null)
                            return;

                        Property p = response.body();

                        etTitle.setText(p.getTitle());
                        etPrice.setText(String.valueOf(p.getPrice()));
                        etArea.setText(String.valueOf(p.getAreaSqft()));

                        dropdownPropertyType.setText(
                                p.getPropertyType(), false);

                        dropdownListingType.setText(
                                p.getListingType(), false);

                        dropdownPriceUnit.setText(
                                p.getPriceUnit(), false);

                        etAddress.setText(p.getAddress());
                        etCity.setText(p.getCity());
                        etState.setText(p.getState());
                        etZipCode.setText(p.getZipCode());

                        etDescription.setText(p.getDescription());

                        if (p.getFloorNumber() != null)
                            etFloorNumber.setText(String.valueOf(p.getFloorNumber()));

                        if (p.getTotalFloors() != null)
                            etTotalFloors.setText(String.valueOf(p.getTotalFloors()));
                    }

                    @Override
                    public void onFailure(Call<Property> call, Throwable t) {

                        Toast.makeText(
                                AddListingActivity.this,
                                "Couldn't load property",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void initViews() {
        step1Group = findViewById(R.id.step1Group);
        step2Group = findViewById(R.id.step2Group);
        step3Group = findViewById(R.id.step3Group);
        step4Group = findViewById(R.id.step4Group);

        step1Circle = findViewById(R.id.step1Circle);
        step2Circle = findViewById(R.id.step2Circle);
        step3Circle = findViewById(R.id.step3Circle);
        step4Circle = findViewById(R.id.step4Circle);

        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);

        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        etArea = findViewById(R.id.etArea);
        dropdownPropertyType = findViewById(R.id.dropdownPropertyType);
        dropdownListingType = findViewById(R.id.dropdownListingType);
        dropdownPriceUnit = findViewById(R.id.dropdownPriceUnit);

        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        etZipCode = findViewById(R.id.etZipCode);

        etDescription = findViewById(R.id.etDescription);
        etFloorNumber = findViewById(R.id.etFloorNumber);
        etTotalFloors = findViewById(R.id.etTotalFloors);

        recyclerSelectedImages = findViewById(R.id.recyclerSelectedImages);
        txtReviewSummary = findViewById(R.id.txtReviewSummary);
    }

    private void setupDropdowns() {
        String[] types = {"Private Office", "Coworking", "Meeting Room", "Virtual Office"};
        dropdownPropertyType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types));

        ArrayAdapter<CharSequence> listingAdapter = ArrayAdapter.createFromResource(
                this, R.array.listing_types_owner, android.R.layout.simple_dropdown_item_1line);
        dropdownListingType.setAdapter(listingAdapter);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.price_units, android.R.layout.simple_dropdown_item_1line);
        dropdownPriceUnit.setAdapter(unitAdapter);
    }

    private void setupImagePicker() {
        imageAdapter = new SelectedImageAdapter(position -> {
            List<Uri> current = new ArrayList<>(imageAdapter.getImages());
            current.remove(position);
            imageAdapter.setImages(current);
        });
        recyclerSelectedImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerSelectedImages.setAdapter(imageAdapter);

        findViewById(R.id.btnPickImages).setOnClickListener(v -> pickImagesLauncher.launch("image/*"));
    }

    private void setupClicks() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> {
            if (currentStep < TOTAL_STEPS) {
                if (!validateCurrentStep()) return;
                currentStep++;
                updateStepUi();
                if (currentStep == TOTAL_STEPS) updateReviewSummary();
            } else {
                if (!validateCurrentStep()) return;
                submitListing();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                updateStepUi();
            }
        });
    }


    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                if (isEmpty(etTitle)) { etTitle.setError("Enter a title"); return false; }
                if (dropdownPropertyType.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Select a property type", Toast.LENGTH_SHORT).show(); return false;
                }
                if (isEmpty(etPrice)) { etPrice.setError("Enter a price"); return false; }
                return true;
            case 2:
                if (isEmpty(etAddress)) { etAddress.setError("Enter an address"); return false; }
                if (isEmpty(etCity)) { etCity.setError("Enter a city"); return false; }
                if (isEmpty(etState)) { etState.setError("Enter a state"); return false; }
                return true;
            case 3:
                return true;
            case 4:
                return true;
            default:
                return true;
        }
    }

    private boolean isEmpty(TextInputEditText field) {
        return field.getText() == null || field.getText().toString().trim().isEmpty();
    }

    private void updateStepUi() {
        step1Group.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        step2Group.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        step3Group.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);
        step4Group.setVisibility(currentStep == 4 ? View.VISIBLE : View.GONE);

        setStepCircle(step1Circle, currentStep >= 1);
        setStepCircle(step2Circle, currentStep >= 2);
        setStepCircle(step3Circle, currentStep >= 3);
        setStepCircle(step4Circle, currentStep >= 4);

        btnPrevious.setVisibility(currentStep == 1 ? View.INVISIBLE : View.VISIBLE);
        if (currentStep == TOTAL_STEPS) {
            btnNext.setText(isEditMode ? "Update Listing" : "Submit Listing");
        } else {
            btnNext.setText("Next");
        }
    }

    private void setStepCircle(TextView circle, boolean active) {
        circle.setBackgroundResource(active ? R.drawable.bg_step_circle_active : R.drawable.bg_step_circle_inactive);
        circle.setTextColor(getResources().getColor(active ? R.color.white : R.color.textGray));
    }

    private void updateReviewSummary() {
        String summary = String.format(
                "%s\n%s\n\u20B9%s / %s\n%s sq ft\n\n%s, %s, %s %s\n\n%s",
                textOf(etTitle),
                textOf(dropdownPropertyType),
                textOf(etPrice), textOf(dropdownPriceUnit),
                textOf(etArea),
                textOf(etAddress), textOf(etCity), textOf(etState), textOf(etZipCode),
                textOf(etDescription));
        txtReviewSummary.setText(summary);
    }

    private String textOf(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private String textOf(AutoCompleteTextView field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void submitListing() {
        Property property = new Property();
        property.setOwnerId(sessionManager.getUserId());
        property.setTitle(textOf(etTitle));
        property.setPropertyType(textOf(dropdownPropertyType));
        property.setListingType("Rent");

        try {
            property.setPrice(Double.parseDouble(textOf(etPrice)));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid price", Toast.LENGTH_SHORT).show();
            return;
        }
        property.setPriceUnit(textOf(dropdownPriceUnit).isEmpty() ? "month" : textOf(dropdownPriceUnit));

        if (!textOf(etArea).isEmpty()) {
            try {
                property.setAreaSqft(Double.parseDouble(textOf(etArea)));
            } catch (NumberFormatException ignored) {
            }
        }

        property.setAddress(textOf(etAddress));
        property.setCity(textOf(etCity));
        property.setState(textOf(etState));
        property.setZipCode(textOf(etZipCode));
        property.setDescription(textOf(etDescription));

        // Convert address to coordinates
        try {
            android.location.Geocoder geocoder = new android.location.Geocoder(this);
            String fullAddress = property.getAddress() + ", " + property.getCity() + ", " + property.getState();
            java.util.List<android.location.Address> addresses = geocoder.getFromLocationName(fullAddress, 1);
            if (addresses != null && !addresses.isEmpty()) {
                property.setLatitude(addresses.get(0).getLatitude());
                property.setLongitude(addresses.get(0).getLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!textOf(etFloorNumber).isEmpty()) {
            try {
                property.setFloorNumber(Integer.parseInt(textOf(etFloorNumber)));
            } catch (NumberFormatException ignored) {
            }
        }
        if (!textOf(etTotalFloors).isEmpty()) {
            try {
                property.setTotalFloors(Integer.parseInt(textOf(etTotalFloors)));
            } catch (NumberFormatException ignored) {
            }
        }

        btnNext.setEnabled(false);

        if (isEditMode) {

            btnNext.setText("Updating...");

            ApiClient.getApiService(this)
                    .updateProperty(editingPropertyId, property)
                    .enqueue(new Callback<Property>() {

                        @Override
                        public void onResponse(Call<Property> call,
                                               Response<Property> response) {

                            btnNext.setEnabled(true);
                            btnNext.setText("Update Listing");

                            if (!response.isSuccessful() || response.body() == null) {
                                Toast.makeText(AddListingActivity.this,
                                        "Couldn't update listing.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Toast.makeText(AddListingActivity.this,
                                    "Listing updated successfully.",
                                    Toast.LENGTH_SHORT).show();

                            if (imageAdapter.getImages().isEmpty()) {
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                uploadImages(editingPropertyId);
                            }
                        }

                        @Override
                        public void onFailure(Call<Property> call,
                                              Throwable t) {

                            btnNext.setEnabled(true);
                            btnNext.setText("Update Listing");

                            Toast.makeText(AddListingActivity.this,
                                    "Server error.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {

            btnNext.setText("Submitting...");

            ApiClient.getApiService(this)
                    .addProperty(property)
                    .enqueue(new Callback<Property>() {

                        @Override
                        public void onResponse(Call<Property> call,
                                               Response<Property> response) {

                            if (!response.isSuccessful() || response.body() == null) {

                                btnNext.setEnabled(true);
                                btnNext.setText("Submit Listing");

                                Toast.makeText(
                                        AddListingActivity.this,
                                        "Couldn't create listing.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            createdPropertyId = response.body().getPropertyId();

                            if (imageAdapter.getImages().isEmpty()) {
                                finishSuccessfully();
                            } else {
                                uploadImages(createdPropertyId);
                            }
                        }

                        @Override
                        public void onFailure(Call<Property> call,
                                              Throwable t) {

                            btnNext.setEnabled(true);
                            btnNext.setText("Submit Listing");

                            Toast.makeText(
                                    AddListingActivity.this,
                                    "Couldn't reach server.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        }
    }
    private void uploadImages(int propertyId) {

        List<MultipartBody.Part> parts = new ArrayList<>();

        for (Uri uri : imageAdapter.getImages()) {
            MultipartBody.Part part = uriToMultipart(uri);
            if (part != null) {
                parts.add(part);
            }
        }

        if (parts.isEmpty()) {
            finishSuccessfully();
            return;
        }

        ApiClient.getApiService(this)
                .uploadPropertyImages(
                        propertyId,
                        parts.toArray(new MultipartBody.Part[0]))
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {

                        finishSuccessfully();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call,
                                          Throwable t) {

                        Toast.makeText(
                                AddListingActivity.this,
                                "Listing created, but image upload failed.",
                                Toast.LENGTH_LONG
                        ).show();

                        finishSuccessfully();
                    }
                });
    }



    /** Copies the picked image into a temp cache file so OkHttp can stream it as multipart form data. */
    private MultipartBody.Part uriToMultipart(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile("upload_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), tempFile);
            return MultipartBody.Part.createFormData("files", tempFile.getName(), requestBody);
        } catch (Exception e) {
            return null;
        }
    }

    private void finishSuccessfully() {
        Toast.makeText(this, "Listing submitted for review!", Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
        finish();
    }
}