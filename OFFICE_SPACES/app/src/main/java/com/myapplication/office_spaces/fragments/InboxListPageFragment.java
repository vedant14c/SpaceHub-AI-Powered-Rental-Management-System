package com.myapplication.office_spaces.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.NotificationAdapter;
import com.myapplication.office_spaces.models.Notification;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InboxListPageFragment extends Fragment {

    private static final String ARG_TAB_TYPE = "arg_tab_type";

    private RecyclerView recyclerInbox;
    private View progressBar, layoutEmpty;
    private NotificationAdapter adapter;
    private int tabType; // 0=All, 1=Owner, 2=System

    public static InboxListPageFragment newInstance(int tabType) {
        InboxListPageFragment fragment = new InboxListPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_TYPE, tabType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabType = getArguments().getInt(ARG_TAB_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_inbox_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerInbox = view.findViewById(R.id.recyclerInbox);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        setupRecycler();
        loadNotifications();
    }

    private void setupRecycler() {
        adapter = new NotificationAdapter(new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(Notification notification) {
                markAsRead(notification);
            }

            @Override
            public void onPayClick(Notification notification) { }
        });
        recyclerInbox.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerInbox.setAdapter(adapter);
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        int userId = new SessionManager(requireContext()).getUserId();
        ApiClient.getApiService(requireContext()).getNotificationsByUser(userId)
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            renderList(response.body());
                        } else {
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void renderList(List<Notification> all) {
        List<Notification> filtered = new ArrayList<>();
        for (Notification n : all) {
            String type = n.getType() != null ? n.getType() : "SYSTEM";
            boolean isSystem = type.equalsIgnoreCase("SYSTEM");

            if (tabType == 0) filtered.add(n);
            else if (tabType == 1 && !isSystem) filtered.add(n);
            else if (tabType == 2 && isSystem) filtered.add(n);
        }
        adapter.setNotifications(filtered);
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void markAsRead(Notification notification) {
        if (notification.getIsRead() != null && notification.getIsRead()) return;
        if (notification.getNotificationId() == null) return;

        ApiClient.getApiService(requireContext())
                .markNotificationRead(notification.getNotificationId())
                .enqueue(new Callback<Notification>() {
                    @Override
                    public void onResponse(Call<Notification> call, Response<Notification> response) {
                        if (!isAdded()) return;
                        loadNotifications();
                    }

                    @Override
                    public void onFailure(Call<Notification> call, Throwable t) {}
                });
    }
}