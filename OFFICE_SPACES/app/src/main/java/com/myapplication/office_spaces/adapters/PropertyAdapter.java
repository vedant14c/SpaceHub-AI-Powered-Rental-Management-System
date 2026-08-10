package com.myapplication.office_spaces.adapters;

import com.myapplication.office_spaces.models.Property;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.myapplication.office_spaces.network.ApiClient;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.myapplication.office_spaces.models.PropertyImage;
import com.myapplication.office_spaces.network.ApiClient;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.myapplication.office_spaces.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    public interface OnPropertyClickListener {
        void onPropertyClick(Property property);
        void onFavoriteClick(Property property, boolean isNowFavorite);
    }

    private List<Property> properties = new ArrayList<>();
    private final OnPropertyClickListener listener;

    public PropertyAdapter(OnPropertyClickListener listener) {
        this.listener = listener;
    }

    public void setProperties(List<Property> newProperties) {
        this.properties = newProperties != null ? newProperties : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeProperty(Property property) {
        int position = properties.indexOf(property);
        if (position != -1) {
            properties.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = properties.get(position);
        Log.d("BIND", "Binding position = " + position);

        holder.txtTitle.setText(property.getTitle());

        String location = (property.getCity() != null ? property.getCity() : "")
                + (property.getState() != null ? ", " + property.getState() : "");
        holder.txtLocation.setText(location);

        String priceUnit = property.getPriceUnit() != null ? " / " + property.getPriceUnit() : "";
        if (property.getPrice() != null) {
            holder.txtPrice.setText(String.format(Locale.getDefault(), "₹%,.0f%s", property.getPrice(), priceUnit));
        }

        if (property.getAreaSqft() != null) {
            holder.txtArea.setText(String.format(Locale.getDefault(), "%,.0f sq ft", property.getAreaSqft()));
        }

        if (property.getDistance() >= 0) {
            holder.txtDistance.setVisibility(View.VISIBLE);
            holder.txtDistance.setText(String.format(Locale.getDefault(), "%.1f km away", property.getDistance() / 1000.0));
        } else {
            holder.txtDistance.setVisibility(View.GONE);
        }

        // Load property image


        ApiClient.getApiService(holder.itemView.getContext())
                .getImagesByPropertyId(property.getPropertyId())
                .enqueue(new Callback<List<PropertyImage>>() {
                    @Override
                    public void onResponse(Call<List<PropertyImage>> call,
                                           Response<List<PropertyImage>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {

                            String imagePath = response.body().get(0).getImageUrl();

                            String imageUrl = ApiClient.BASE_URL + imagePath.replaceFirst("^/", "");

                            Log.d("IMAGE_URL", imageUrl);

                            Glide.with(holder.itemView.getContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.placeholder_office)
                                    .error(R.drawable.placeholder_office)
                                    .centerCrop()
                                    .into(holder.imgProperty);

                        } else {

                            holder.imgProperty.setImageResource(R.drawable.placeholder_office);

                        }
                    }

                    @Override
                    public void onFailure(Call<List<PropertyImage>> call,
                                          Throwable t) {

                        holder.imgProperty.setImageResource(
                                R.drawable.ic_category_office
                        );

                    }
                });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPropertyClick(property);
        });

        holder.imgFavorite.setImageResource(property.isFavorite() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        holder.imgFavorite.setOnClickListener(v -> {
            property.setFavorite(!property.isFavorite());
            holder.imgFavorite.setImageResource(property.isFavorite() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            if (listener != null) listener.onFavoriteClick(property, property.isFavorite());
        });
    }

    @Override
    public int getItemCount() {
        Log.d("COUNT", "ItemCount = " + properties.size());
        return properties.size();
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProperty, imgFavorite;
        TextView txtTitle, txtLocation, txtPrice, txtArea, txtDistance;

        PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProperty = itemView.findViewById(R.id.imgProperty);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtArea = itemView.findViewById(R.id.txtArea);
            txtDistance = itemView.findViewById(R.id.txtDistance);
        }
    }
}