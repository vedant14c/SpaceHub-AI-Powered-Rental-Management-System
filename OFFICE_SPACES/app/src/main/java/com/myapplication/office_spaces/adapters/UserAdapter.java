package com.myapplication.office_spaces.adapters;

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
import com.myapplication.office_spaces.models.AdminUserView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onActivate(AdminUserView user);
        void onDeactivate(AdminUserView user);
    }

    private final Context context;
    private final OnUserActionListener listener;
    private List<AdminUserView> users = new ArrayList<>();

    public UserAdapter(Context context, OnUserActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setUsers(List<AdminUserView> users) {
        this.users = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_user, parent, false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        AdminUserView user = users.get(position);

        holder.txtName.setText(user.getName());
        holder.txtEmail.setText(user.getEmail());
        holder.txtRole.setText(user.getRole());

        if (Boolean.TRUE.equals(user.getIsActive())) {

            holder.chipStatus.setText("ACTIVE");
            holder.chipStatus.setChipBackgroundColorResource(R.color.status_active);

            holder.btnToggle.setText("Deactivate");

            holder.btnToggle.setOnClickListener(v ->
                    listener.onDeactivate(user));

        } else {

            holder.chipStatus.setText("INACTIVE");
            holder.chipStatus.setChipBackgroundColorResource(R.color.status_inactive);

            holder.btnToggle.setText("Activate");

            holder.btnToggle.setOnClickListener(v ->
                    listener.onActivate(user));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        TextView txtEmail;
        TextView txtRole;
        Chip chipStatus;
        Button btnToggle;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtRole = itemView.findViewById(R.id.txtRole);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnToggle = itemView.findViewById(R.id.btnToggle);
        }
    }
}