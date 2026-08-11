package com.myapplication.office_spaces.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.PropertyRequest;

import java.util.List;
import java.util.Locale;

public class MyRequestAdapter extends RecyclerView.Adapter<MyRequestAdapter.ViewHolder> {

    public interface OnPayClickListener {
        void onPay(PropertyRequest request);
    }

    private final Context context;
    private final List<PropertyRequest> requests;
    private final OnPayClickListener payClickListener;

    public MyRequestAdapter(Context context,
                            List<PropertyRequest> requests,
                            OnPayClickListener listener) {
        this.context = context;
        this.requests = requests;
        this.payClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PropertyRequest request = requests.get(position);

        holder.txtId.setText(String.format(Locale.getDefault(), "Request #%d", request.getRequestId()));
        holder.txtType.setText(request.getRequestType() + " Request");

        if (request.getOfferPrice() != null && request.getOfferPrice() > 0) {
            holder.txtDetails.setText(String.format(Locale.getDefault(), "Offer: ₹%,.0f", request.getOfferPrice()));
        } else if (request.getProposedStart() != null) {
            holder.txtDetails.setText(String.format("%s to %s", request.getProposedStart(), request.getProposedEnd()));
        }

        String status = request.getStatus() != null ? request.getStatus().toLowerCase() : "pending";
        holder.txtStatus.setText(status.toUpperCase());

        // Apply status colors
        int color;
        if (status.equals("approved") || status.equals("accepted")) {
            color = ContextCompat.getColor(context, R.color.success);
            holder.btnPayNow.setVisibility(View.VISIBLE);
        } else if (status.equals("pending")) {
            color = ContextCompat.getColor(context, R.color.warning);
            holder.btnPayNow.setVisibility(View.GONE);
        } else if (status.equals("paid")) {
             color = ContextCompat.getColor(context, R.color.primary);
             holder.btnPayNow.setVisibility(View.GONE);
        } else {
            color = ContextCompat.getColor(context, R.color.danger);
            holder.btnPayNow.setVisibility(View.GONE);
        }
        holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(color));

        holder.btnPayNow.setOnClickListener(v -> {
            if (payClickListener != null) {
                payClickListener.onPay(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtType, txtStatus, txtDetails, txtId;
        MaterialButton btnPayNow;

        public ViewHolder(View itemView) {
            super(itemView);
            txtType = itemView.findViewById(R.id.txtType);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDetails = itemView.findViewById(R.id.txtDetails);
            txtId = itemView.findViewById(R.id.txtId);
            btnPayNow = itemView.findViewById(R.id.btnPayNow);
        }
    }
}