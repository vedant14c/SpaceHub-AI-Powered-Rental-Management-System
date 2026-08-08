package com.myapplication.office_spaces.activities;




import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.AdminUserAdapter;
import com.myapplication.office_spaces.models.AdminUserView;
import com.myapplication.office_spaces.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BlockUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private AdminUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_users);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter = new AdminUserAdapter(this::confirmToggleUser);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(adapter);

        loadUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers(); // refresh in case Manage Users changed something
    }

    private void loadUsers() {

        ApiClient.getApiService(this)
                .getAllUsers()
                .enqueue(new Callback<List<AdminUserView>>() {

                    @Override
                    public void onResponse(Call<List<AdminUserView>> call,
                                           Response<List<AdminUserView>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setUsers(response.body());
                        } else {
                            Toast.makeText(BlockUsersActivity.this,
                                    "Unable to load users.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AdminUserView>> call,
                                          Throwable t) {

                        Toast.makeText(BlockUsersActivity.this,
                                "Server connection failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmToggleUser(AdminUserView user) {

        boolean isActive = Boolean.TRUE.equals(user.getIsActive());

        String action = isActive ? "Deactivate" : "Activate";

        String message = isActive
                ? "This will prevent the user from logging in."
                : "This will restore the user's access.";

        new AlertDialog.Builder(this)
                .setTitle(action + " " + user.getName() + "?")
                .setMessage(message)
                .setPositiveButton(action, (dialog, which) -> {

                    if (isActive) {
                        deactivateUser(user.getId());
                    } else {
                        activateUser(user.getId());
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void activateUser(int id) {

        ApiClient.getApiService(this)
                .activateUser(id)
                .enqueue(new Callback<AdminUserView>() {

                    @Override
                    public void onResponse(Call<AdminUserView> call,
                                           Response<AdminUserView> response) {

                        if (response.isSuccessful()) {

                            Toast.makeText(BlockUsersActivity.this,
                                    "User activated",
                                    Toast.LENGTH_SHORT).show();

                            loadUsers();

                        } else {

                            Toast.makeText(BlockUsersActivity.this,
                                    "Activation failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AdminUserView> call,
                                          Throwable t) {

                        Toast.makeText(BlockUsersActivity.this,
                                "Server connection failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deactivateUser(int id) {

        ApiClient.getApiService(this)
                .deactivateUser(id)
                .enqueue(new Callback<AdminUserView>() {

                    @Override
                    public void onResponse(Call<AdminUserView> call,
                                           Response<AdminUserView> response) {

                        if (response.isSuccessful()) {

                            Toast.makeText(BlockUsersActivity.this,
                                    "User deactivated",
                                    Toast.LENGTH_SHORT).show();

                            loadUsers();

                        } else {

                            Toast.makeText(BlockUsersActivity.this,
                                    "Deactivation failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AdminUserView> call,
                                          Throwable t) {

                        Toast.makeText(BlockUsersActivity.this,
                                "Server connection failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}