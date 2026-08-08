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
import com.myapplication.office_spaces.models.PropertyRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnBookingClickListener {
        void onBookingClick(PropertyRequest request);
    }

    /** UI-level bucket, distinct from the backend's lowercase RequestStatus enum. */
    public enum BookingBucket { UPCOMING, ACTIVE, HISTORY }

    private List<PropertyRequest> requests = new ArrayList<>();
    private Map<Integer, Property> propertiesById = new HashMap<>();
    private final OnBookingClickListener listener;

    public BookingAdapter(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<PropertyRequest> requests, Map<Integer, Property> propertiesById) {
        this.requests = requests != null ? requests : new ArrayList<>();
        this.propertiesById = propertiesById != null ? propertiesById : new HashMap<>();
        notifyDataSetChanged();
    }

    public static BookingBucket classify(PropertyRequest r) {
        String status = r.getStatus() != null ? r.getStatus().toLowerCase() : "pending";

        if (status.equals("cancelled") || status.equals("rejected")) return BookingBucket.HISTORY;
        if (status.equals("pending")) return BookingBucket.UPCOMING;

        // accepted: Active if the end date is today or in the future, else History (Completed).
        if (r.getProposedEnd() == null || r.getProposedEnd().isEmpty()) return BookingBucket.ACTIVE;

        String today = todayIso();
        return r.getProposedEnd().compareTo(today) >= 0 ? BookingBucket.ACTIVE : BookingBucket.HISTORY;
    }

    private static String todayIso() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        return String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d",
                c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH));
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        PropertyRequest request = requests.get(position);
        Property property = propertiesById.get(request.getPropertyId());

        holder.txtTitle.setText(property != null ? property.getTitle() : "Property #" + request.getPropertyId());

        if (property != null && property.getPrice() != null) {
            String unit = property.getPriceUnit() != null ? " / " + property.getPriceUnit() : "";
            holder.txtPrice.setText(String.format(java.util.Locale.getDefault(), "\u20B9%,.0f%s", property.getPrice(), unit));
        } else {
            holder.txtPrice.setText("");
        }

        String dateText = request.getProposedStart() != null ? "Since " + request.getProposedStart() : "";
        holder.txtDate.setText(dateText);

        BookingBucket bucket = classify(request);
        String status = request.getStatus() != null ? request.getStatus().toLowerCase() : "pending";

        switch (bucket) {
            case UPCOMING:
                holder.txtStatus.setText("Upcoming");
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_upcoming);
                break;
            case ACTIVE:
                holder.txtStatus.setText("Active");
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_active);
                break;
            case HISTORY:
                if (status.equals("cancelled") || status.equals("rejected")) {
                    holder.txtStatus.setText("Cancelled");
                    holder.txtStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                } else {
                    holder.txtStatus.setText("Completed");
                    holder.txtStatus.setBackgroundResource(R.drawable.bg_status_completed);
                }
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBookingClick(request);
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProperty;
        TextView txtTitle, txtPrice, txtDate, txtStatus;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProperty = itemView.findViewById(R.id.imgProperty);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}