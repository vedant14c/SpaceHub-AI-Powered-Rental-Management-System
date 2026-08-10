package com.myapplication.office_spaces.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {

        void onNotificationClick(Notification notification);

        void onPayClick(Notification notification);

    }

    private List<Notification> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification n = notifications.get(position);

        holder.txtNotifTitle.setText(n.getTitle());
        holder.txtNotifMessage.setText(n.getMessage());
        holder.txtAvatarLetter.setText(firstLetterOf(n.getTitle()));
        holder.txtNotifTime.setText(n.getCreatedAt() != null ? n.getCreatedAt() : "");

        boolean unread = n.getIsRead() == null || !n.getIsRead();
        holder.imgUnreadDot.setVisibility(unread ? View.VISIBLE : View.GONE);
        holder.txtNotifTitle.setTypeface(null, unread ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        if ("PAYMENT".equals(n.getType())) {
            holder.btnPayNow.setVisibility(View.VISIBLE);
        } else {
            holder.btnPayNow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificationClick(n);
        });

        holder.btnPayNow.setOnClickListener(v -> {
            if (listener != null) listener.onPayClick(n);
        });
    }

    private String firstLetterOf(String text) {
        if (text == null || text.isEmpty()) return "?";
        return text.substring(0, 1).toUpperCase();
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView txtAvatarLetter, txtNotifTitle, txtNotifMessage, txtNotifTime;
        ImageView imgUnreadDot;
        Button btnPayNow;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAvatarLetter = itemView.findViewById(R.id.txtAvatarLetter);
            txtNotifTitle = itemView.findViewById(R.id.txtNotifTitle);
            txtNotifMessage = itemView.findViewById(R.id.txtNotifMessage);
            txtNotifTime = itemView.findViewById(R.id.txtNotifTime);
            imgUnreadDot = itemView.findViewById(R.id.imgUnreadDot);
            btnPayNow = itemView.findViewById(R.id.btnPayNow);
        }
    }
}