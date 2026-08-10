package com.myapplication.office_spaces.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.NotificationAdapter;
import com.myapplication.office_spaces.models.Notification;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(Notification notification) {
                ApiClient.getApiService(NotificationsActivity.this)
                        .markNotificationRead(notification.getNotificationId())
                        .enqueue(new Callback<Notification>() {
                            @Override
                            public void onResponse(Call<Notification> call,
                                                   Response<Notification> response) {
                                loadNotifications();
                            }

                            @Override
                            public void onFailure(Call<Notification> call,
                                                  Throwable t) {
                            }
                        });
            }

            @Override
            public void onPayClick(Notification notification) {
                // Handle payment if needed
            }
        });

        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {

        int userId = new SessionManager(this).getUserId();

        ApiClient.getApiService(this)
                .getNotifications(userId)
                .enqueue(new Callback<List<Notification>>() {

                    @Override
                    public void onResponse(Call<List<Notification>> call,
                                           Response<List<Notification>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setNotifications(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Notification>> call,
                                          Throwable t) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}