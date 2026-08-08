package com.myapplication.office_spaces.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.PropertyRequest;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    public interface OnRequestActionListener {
        void onApprove(PropertyRequest request);
        void onReject(PropertyRequest request);
    }

    private final Context context;
    private final List<PropertyRequest> requestList;
    private final OnRequestActionListener listener;

    public RequestAdapter(Context context,
                          List<PropertyRequest> requestList,
                          OnRequestActionListener listener) {
        this.context = context;
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_request, parent, false);

        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {

        PropertyRequest request = requestList.get(position);

        holder.txtProperty.setText("Property ID : " + request.getPropertyId());
        holder.txtUser.setText("User ID : " + request.getUserId());
        holder.txtType.setText("Type : " + request.getRequestType());

        if (request.getOfferPrice() != null) {
            holder.txtAmount.setText("Offer : ₹" + request.getOfferPrice());
        } else {
            holder.txtAmount.setText("Offer : --");
        }

        holder.chipStatus.setText(request.getStatus());

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        } else {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        }

        holder.btnApprove.setOnClickListener(v ->

                new AlertDialog.Builder(context)
                        .setTitle("Approve Request")
                        .setMessage("Approve this request?")
                        .setPositiveButton("Approve",
                                (dialog, which) -> listener.onApprove(request))
                        .setNegativeButton("Cancel", null)
                        .show());

        holder.btnReject.setOnClickListener(v ->

                new AlertDialog.Builder(context)
                        .setTitle("Reject Request")
                        .setMessage("Reject this request?")
                        .setPositiveButton("Reject",
                                (dialog, which) -> listener.onReject(request))
                        .setNegativeButton("Cancel", null)
                        .show());

    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView txtProperty;
        TextView txtUser;
        TextView txtType;
        TextView txtAmount;

        Chip chipStatus;

        Button btnApprove;
        Button btnReject;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            txtProperty = itemView.findViewById(R.id.txtProperty);
            txtUser = itemView.findViewById(R.id.txtUser);
            txtType = itemView.findViewById(R.id.txtType);
            txtAmount = itemView.findViewById(R.id.txtAmount);

            chipStatus = itemView.findViewById(R.id.chipStatus);

            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}