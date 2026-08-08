package com.myapplication.office_spaces.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.RangeSlider;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.activities.SearchResultActivity;

import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private static final String ARG_PRESET_TYPE = "preset_property_type";
    private static final String ARG_PRESET_LISTING = "preset_listing_type";

    public static final String EXTRA_PROPERTY_TYPE = "property_type";
    public static final String EXTRA_LISTING_TYPE = "listing_type";
    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_MIN_PRICE = "min_price";
    public static final String EXTRA_MAX_PRICE = "max_price";
    public static final String EXTRA_MIN_AREA = "min_area";
    public static final String EXTRA_MAX_AREA = "max_area";
    public static final String EXTRA_QUERY = "query";
    public static final String EXTRA_RADIUS_KM = "radius_km";

    public static SearchFragment newInstance(
            @Nullable String presetPropertyType,
            @Nullable String presetListingType) {

        SearchFragment fragment = new SearchFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PRESET_TYPE, presetPropertyType);
        args.putString(ARG_PRESET_LISTING, presetListingType);

        fragment.setArguments(args);

        return fragment;
    }

    private TextView tabRent;
    private TextView tabCoworking;

    private String selectedListingType = "Rent";

    private AutoCompleteTextView dropdownCity;
    private AutoCompleteTextView dropdownPropertyType;

    private RangeSlider sliderPrice;
    private RangeSlider sliderArea;

    private TextView txtPriceRangeValue;
    private TextView txtAreaValue;

    private EditText etSearchQuery;

    private com.google.android.material.chip.ChipGroup chipGroupRadius;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_search,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        setupDropdowns();

        setupTabs();

        setupSliders();

        setupClicks(view);

        String presetType = null;
        String presetListing = null;

        if (getArguments() != null) {

            presetType =
                    getArguments().getString(ARG_PRESET_TYPE);

            presetListing =
                    getArguments().getString(ARG_PRESET_LISTING);
        }

        if (presetType != null) {
            dropdownPropertyType.setText(
                    presetType,
                    false
            );
        }

        if (presetListing != null) {

            if (presetListing.equalsIgnoreCase("Rent")) {

                selectTab("Rent");

            } else if (presetListing.equalsIgnoreCase("Coworking")) {

                tabCoworking.performClick();

            } else {

                selectedListingType = "All";
            }
        }
    }

    private void initViews(View view) {

        etSearchQuery =
                view.findViewById(R.id.etSearchQuery);

        tabRent =
                view.findViewById(R.id.tabRent);

        tabCoworking =
                view.findViewById(R.id.tabCoworking);

        dropdownCity =
                view.findViewById(R.id.dropdownCity);

        dropdownPropertyType =
                view.findViewById(R.id.dropdownPropertyType);

        sliderPrice =
                view.findViewById(R.id.sliderPrice);

        sliderArea =
                view.findViewById(R.id.sliderArea);

        txtPriceRangeValue =
                view.findViewById(R.id.txtPriceRangeValue);

        txtAreaValue =
                view.findViewById(R.id.txtAreaValue);

        chipGroupRadius =
                view.findViewById(R.id.chipGroupRadius);
    }

    private void setupDropdowns() {

        ArrayAdapter<CharSequence> cityAdapter =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.cities,
                        android.R.layout.simple_dropdown_item_1line
                );

        dropdownCity.setAdapter(cityAdapter);

        ArrayAdapter<CharSequence> typeAdapter =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.property_types,
                        android.R.layout.simple_dropdown_item_1line
                );

        dropdownPropertyType.setAdapter(typeAdapter);
    }

    private void setupTabs() {

        tabRent.setOnClickListener(v -> selectTab("Rent"));

        tabCoworking.setOnClickListener(v -> {

            selectedListingType = "All";

            tabRent.setSelected(false);
            tabCoworking.setSelected(true);

            tabRent.setTextColor(getColorFor(false));
            tabCoworking.setTextColor(getColorFor(true));

            dropdownPropertyType.setText("Coworking", false);
        });

        selectTab("Rent");
    }

    private void selectTab(String type) {

        selectedListingType = type;

        tabRent.setSelected(type.equals("Rent"));
        tabCoworking.setSelected(false);

        tabRent.setTextColor(
                getColorFor(type.equals("Rent")));

        tabCoworking.setTextColor(
                getColorFor(false));
    }

    private int getColorFor(boolean selected) {

        return ContextCompat.getColor(
                requireContext(),
                selected
                        ? R.color.white
                        : R.color.textDark
        );
    }
    private void setupSliders() {

        sliderPrice.addOnChangeListener((slider, value, fromUser) ->
                updatePriceLabel());

        sliderArea.addOnChangeListener((slider, value, fromUser) ->
                updateAreaLabel());

        updatePriceLabel();
        updateAreaLabel();
    }

    private void updatePriceLabel() {

        List<Float> values = sliderPrice.getValues();

        float min = values.get(0);
        float max = values.get(1);

        String maxText;

        if (max >= sliderPrice.getValueTo()) {

            maxText = String.format(
                    Locale.getDefault(),
                    "₹%,.0f+",
                    max
            );

        } else {

            maxText = String.format(
                    Locale.getDefault(),
                    "₹%,.0f",
                    max
            );
        }

        txtPriceRangeValue.setText(
                String.format(
                        Locale.getDefault(),
                        "₹%,.0f - %s",
                        min,
                        maxText
                )
        );
    }

    private void updateAreaLabel() {

        List<Float> values = sliderArea.getValues();

        float min = values.get(0);
        float max = values.get(1);

        String maxText;

        if (max >= sliderArea.getValueTo()) {

            maxText = String.format(
                    Locale.getDefault(),
                    "%,.0f+",
                    max
            );

        } else {

            maxText = String.format(
                    Locale.getDefault(),
                    "%,.0f",
                    max
            );
        }

        txtAreaValue.setText(
                String.format(
                        Locale.getDefault(),
                        "%,.0f - %s",
                        min,
                        maxText
                )
        );
    }

    private void setupClicks(View view) {

        view.findViewById(R.id.txtClearAll)
                .setOnClickListener(v -> clearFilters());

        view.findViewById(R.id.btnViewResults)
                .setOnClickListener(v -> viewResults());
    }

    private void clearFilters() {

        etSearchQuery.setText("");

        dropdownCity.setText("", false);

        dropdownPropertyType.setText(
                "All Types",
                false
        );

        sliderPrice.setValues(
                sliderPrice.getValueFrom(),
                sliderPrice.getValueTo()
        );

        sliderArea.setValues(
                sliderArea.getValueFrom(),
                sliderArea.getValueTo()
        );

        selectedListingType = "All";

        tabRent.setSelected(false);
        tabCoworking.setSelected(true);

        tabRent.setTextColor(getColorFor(false));
        tabCoworking.setTextColor(getColorFor(true));

        if (chipGroupRadius != null) {
            chipGroupRadius.check(R.id.chipAnyDistance);
        }
    }

    /** Returns the radius (km) selected in the Distance chip group, or -1 for "Any distance". */
    private float getSelectedRadiusKm() {
        if (chipGroupRadius == null) return -1;
        int checkedId = chipGroupRadius.getCheckedChipId();
        if (checkedId == R.id.chip5km) return 5f;
        if (checkedId == R.id.chip10km) return 10f;
        if (checkedId == R.id.chip20km) return 20f;
        return -1;
    }

    private void viewResults() {

        List<Float> priceValues =
                sliderPrice.getValues();

        List<Float> areaValues =
                sliderArea.getValues();

        Intent intent = new Intent(
                requireContext(),
                SearchResultActivity.class
        );

        intent.putExtra(
                EXTRA_QUERY,
                etSearchQuery.getText().toString().trim()
        );

        intent.putExtra(
                EXTRA_LISTING_TYPE,
                selectedListingType
        );

        intent.putExtra(
                EXTRA_CITY,
                dropdownCity.getText().toString().trim()
        );

        intent.putExtra(
                EXTRA_MIN_PRICE,
                priceValues.get(0)
        );

        intent.putExtra(
                EXTRA_MAX_PRICE,
                priceValues.get(1)
        );

        intent.putExtra(
                EXTRA_MIN_AREA,
                areaValues.get(0)
        );

        intent.putExtra(
                EXTRA_MAX_AREA,
                areaValues.get(1)
        );

        intent.putExtra(
                EXTRA_RADIUS_KM,
                getSelectedRadiusKm()
        );

        String propertyType =
                dropdownPropertyType
                        .getText()
                        .toString()
                        .trim();

        if (!propertyType.isEmpty()) {

            intent.putExtra(
                    EXTRA_PROPERTY_TYPE,
                    propertyType
            );
        }

        startActivity(intent);
    }
}