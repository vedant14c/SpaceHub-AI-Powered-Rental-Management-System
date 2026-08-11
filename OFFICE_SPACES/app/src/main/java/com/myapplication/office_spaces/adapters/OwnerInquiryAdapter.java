package com.myapplication.office_spaces.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.OwnerRequestView;

import java.util.ArrayList;
import java.util.List;

public class OwnerInquiryAdapter extends RecyclerView.Adapter<OwnerInquiryAdapter.InquiryViewHolder> {

    public interface OnInquiryClickListener {
        void onInquiryClick(OwnerRequestView request);
    }

    private List<OwnerRequestView> requests = new ArrayList<>();
    private final OnInquiryClickListener listener;

    public OwnerInquiryAdapter(OnInquiryClickListener listener) {
        this.listener = listener;
    }

    public void setRequests(List<OwnerRequestView> requests) {
        this.requests = requests != null ? requests : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InquiryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_inquiry, parent, false);
        return new InquiryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InquiryViewHolder holder, int position) {
        OwnerRequestView r = requests.get(position);

        String requesterName = r.getRequesterName() != null
                ? r.getRequesterName()
                : "Someone";

        holder.txtRequesterName.setText(requesterName);

        holder.txtAvatarLetter.setText(
                requesterName.substring(0,1).toUpperCase());

        holder.txtPropertyTitle.setText(r.getPropertyTitle());

        holder.txtRequestType.setText(
                "Type : " + r.getRequestType());

        if (r.getOfferPrice() != null) {
            holder.txtOfferPrice.setText("Offer : ₹" + r.getOfferPrice());
        } else {
            holder.txtOfferPrice.setText("Offer : --");
        }

        if (r.getProposedStart() != null && r.getProposedEnd() != null) {
            holder.txtDates.setText(
                    r.getProposedStart()
                            + " → "
                            + r.getProposedEnd());
        } else {
            holder.txtDates.setText("No proposed dates");
        }

        String status = r.getStatus();

        if (status == null) {
            holder.txtStatus.setText("Pending");
        } else {
            switch (status.toLowerCase()) {
                case "paid":
                    holder.txtStatus.setText("Paid");
                    break;
                case "approved":
                    holder.txtStatus.setText("Approved");
                    break;
                case "accepted":
                    holder.txtStatus.setText("Accepted");
                    break;
                case "rejected":
                    holder.txtStatus.setText("Rejected");
                    break;
                default:
                    holder.txtStatus.setText("Pending");
            }
        }


        if (status == null || status.trim().isEmpty()) {
            status = "Pending";
        }

        holder.txtStatus.setText(status.substring(0,1).toUpperCase() + status.substring(1).toLowerCase());
        holder.txtStatus.setText(
                r.getStatus() == null
                        ? "Pending"
                        : r.getStatus().substring(0, 1).toUpperCase()
                        + r.getStatus().substring(1).toLowerCase()
        );
        holder.txtTime.setText(
                r.getCreatedAt() != null
                        ? r.getCreatedAt().toString()
                        : "");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onInquiryClick(r);
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class InquiryViewHolder extends RecyclerView.ViewHolder {
        TextView txtAvatarLetter, txtRequesterName, txtPropertyTitle, txtStatus, txtTime,txtRequestType,txtOfferPrice,txtDates;


        InquiryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAvatarLetter = itemView.findViewById(R.id.txtAvatarLetter);
            txtRequesterName = itemView.findViewById(R.id.txtRequesterName);
            txtPropertyTitle = itemView.findViewById(R.id.txtPropertyTitle);
            txtRequestType = itemView.findViewById(R.id.txtRequestType);
            txtOfferPrice = itemView.findViewById(R.id.txtOfferPrice);
            txtDates = itemView.findViewById(R.id.txtDates);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}