package com.myapplication.office_spaces.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.Property;

import java.util.ArrayList;
import java.util.List;

public class PropertyAdminAdapter
        extends RecyclerView.Adapter<PropertyAdminAdapter.PropertyViewHolder> {

    public interface OnPropertyActionListener {
        void onApprove(Property property);
        void onReject(Property property);
    }

    private List<Property> propertyList = new ArrayList<>();
    private final OnPropertyActionListener listener;

    public PropertyAdminAdapter(OnPropertyActionListener listener) {
        this.listener = listener;
    }

    public void setProperties(List<Property> list) {
        propertyList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                 int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_property, parent, false);

        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder,
                                 int position) {

        Property property = propertyList.get(position);

        holder.txtTitle.setText(property.getTitle());

        holder.txtType.setText(
                property.getPropertyType()
                        + " • "
                        + property.getListingType());

        holder.txtCity.setText(property.getCity());

        String price = "₹" +
                String.format("%.0f", property.getPrice());

        if (property.getPriceUnit() != null &&
                !property.getPriceUnit().isEmpty()) {

            price += " / " + property.getPriceUnit();
        }

        holder.txtPrice.setText(price);

        String status = property.getApprovalStatus();

        if (status == null)
            status = "PENDING";

        holder.chipStatus.setText(status);

        switch (status.toUpperCase()) {

            case "APPROVED":

                holder.chipStatus.setChipBackgroundColorResource(
                        R.color.status_approved);

                holder.btnApprove.setEnabled(false);
                holder.btnReject.setEnabled(true);

                break;

            case "REJECTED":

                holder.chipStatus.setChipBackgroundColorResource(
                        R.color.status_rejected);

                holder.btnReject.setEnabled(false);
                holder.btnApprove.setEnabled(true);

                break;

            default:

                holder.chipStatus.setChipBackgroundColorResource(
                        R.color.status_pending);

                holder.btnApprove.setEnabled(true);
                holder.btnReject.setEnabled(true);
        }

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null)
                listener.onApprove(property);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null)
                listener.onReject(property);
        });
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtType;
        TextView txtCity;
        TextView txtPrice;

        Chip chipStatus;

        Button btnApprove;
        Button btnReject;

        PropertyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtType = itemView.findViewById(R.id.txtType);
            txtCity = itemView.findViewById(R.id.txtCity);
            txtPrice = itemView.findViewById(R.id.txtPrice);

            chipStatus = itemView.findViewById(R.id.chipStatus);

            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}