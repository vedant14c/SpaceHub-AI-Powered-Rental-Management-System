package com.myapplication.office_spaces.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApprovalAdapter extends RecyclerView.Adapter<ApprovalAdapter.ApprovalViewHolder> {

    public interface OnApprovalActionListener {
        void onApprove(Property property);
        void onReject(Property property);
    }

    private List<Property> properties = new ArrayList<>();
    private boolean showActions = true; // false for Approved/Rejected tabs (read-only)
    private final OnApprovalActionListener listener;

    public ApprovalAdapter(OnApprovalActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<Property> properties, boolean showActions) {
        this.properties = properties != null ? properties : new ArrayList<>();
        this.showActions = showActions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ApprovalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_approval_listing, parent, false);
        return new ApprovalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApprovalViewHolder holder, int position) {
        Property p = properties.get(position);

        holder.txtTitle.setText(p.getTitle());

        String location = (p.getCity() != null ? p.getCity() : "")
                + (p.getState() != null ? ", " + p.getState() : "");
        holder.txtOwnerAndLocation.setText(location);

        if (p.getPrice() != null) {
            String unit = p.getPriceUnit() != null ? " / " + p.getPriceUnit() : "";
            holder.txtPrice.setText(String.format(Locale.getDefault(), "\u20B9%,.0f%s", p.getPrice(), unit));
        }

        if (showActions) {
            holder.actionButtons.setVisibility(View.VISIBLE);
            holder.txtStatusLabel.setVisibility(View.GONE);

            holder.btnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onApprove(p);
            });
            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(p);
            });
        } else {
            holder.actionButtons.setVisibility(View.GONE);
            holder.txtStatusLabel.setVisibility(View.VISIBLE);

            boolean approved = Boolean.TRUE.equals(p.getIsApproved());
            holder.txtStatusLabel.setText(approved ? "Approved" : "Rejected");
            holder.txtStatusLabel.setBackgroundResource(approved ? R.drawable.bg_status_active : R.drawable.bg_status_cancelled);
        }
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    static class ApprovalViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProperty, btnApprove, btnReject;
        TextView txtTitle, txtOwnerAndLocation, txtPrice, txtStatusLabel;
        View actionButtons;

        ApprovalViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProperty = itemView.findViewById(R.id.imgProperty);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtOwnerAndLocation = itemView.findViewById(R.id.txtOwnerAndLocation);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            actionButtons = itemView.findViewById(R.id.actionButtons);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            txtStatusLabel = itemView.findViewById(R.id.txtStatusLabel);
        }
    }
}