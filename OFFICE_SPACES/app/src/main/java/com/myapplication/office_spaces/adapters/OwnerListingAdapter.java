package com.myapplication.office_spaces.adapters;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.models.PropertyImage;
import com.myapplication.office_spaces.network.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerListingAdapter extends RecyclerView.Adapter<OwnerListingAdapter.ListingViewHolder> {

    public interface OnListingActionListener {
        void onListingClick(Property property);
        void onEditClick(Property property);
        void onDeleteClick(Property property);
    }

    public enum ListingBucket { ACTIVE, PENDING, INACTIVE }

    private List<Property> properties = new ArrayList<>();
    private final OnListingActionListener listener;

    public OwnerListingAdapter(OnListingActionListener listener) {
        this.listener = listener;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties != null ? properties : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static ListingBucket classify(Property p) {
        if (p.getStatus() != null && p.getStatus().equalsIgnoreCase("Inactive")) {
            return ListingBucket.INACTIVE;
        }
        return Boolean.TRUE.equals(p.getIsApproved()) ? ListingBucket.ACTIVE : ListingBucket.PENDING;
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_listing, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        Property p = properties.get(position);

        holder.txtTitle.setText(p.getTitle());

        String unit = p.getPriceUnit() != null ? " / " + p.getPriceUnit() : "";
        if (p.getPrice() != null) {
            holder.txtPrice.setText(String.format(Locale.getDefault(), "\u20B9%,.0f%s", p.getPrice(), unit));
        }

        // Load property image
        ApiClient.getApiService(holder.itemView.getContext())
                .getImagesByPropertyId(p.getPropertyId())
                .enqueue(new Callback<List<PropertyImage>>() {
                    @Override
                    public void onResponse(Call<List<PropertyImage>> call,
                                           Response<List<PropertyImage>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            String imagePath = response.body().get(0).getImageUrl();
                            String imageUrl = ApiClient.BASE_URL + imagePath.replaceFirst("^/", "");
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
                    public void onFailure(Call<List<PropertyImage>> call, Throwable t) {
                        holder.imgProperty.setImageResource(R.drawable.placeholder_office);
                    }
                });

        ListingBucket bucket = classify(p);
        switch (bucket) {
            case ACTIVE:
                holder.txtStatus.setText("Active");
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_active);
                break;
            case PENDING:
                holder.txtStatus.setText("Pending");
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_upcoming);
                break;
            case INACTIVE:
                holder.txtStatus.setText("Inactive");
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_completed);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onListingClick(p);
        });
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(p);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    static class ListingViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProperty, btnEdit, btnDelete;
        TextView txtTitle, txtPrice, txtStatus;

        ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProperty = itemView.findViewById(R.id.imgProperty);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}