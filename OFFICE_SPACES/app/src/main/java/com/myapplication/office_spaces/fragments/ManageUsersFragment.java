package com.myapplication.office_spaces.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.AdminUserAdapter;
import com.myapplication.office_spaces.models.AdminUserView;
import com.myapplication.office_spaces.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageUsersFragment extends Fragment {

    private RecyclerView recyclerUsers;
    private TextView txtEmpty;
    private TabLayout tabLayout;

    private AdminUserAdapter adapter;

    private List<AdminUserView> allUsers = new ArrayList<>();

    private int selectedTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_manage_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerUsers = view.findViewById(R.id.recyclerUsers);
        txtEmpty = view.findViewById(R.id.txtEmpty);
        tabLayout = view.findViewById(R.id.tabLayout);

        adapter = new AdminUserAdapter(this::confirmToggleUser);

        recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerUsers.setAdapter(adapter);

        setupTabs();

        loadUsers();
    }

    private void setupTabs() {

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                selectedTab = tab.getPosition();

                renderCurrentTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void loadUsers() {

        ApiClient.getApiService(requireContext())
                .getAllUsers()
                .enqueue(new Callback<List<AdminUserView>>() {

                    @Override
                    public void onResponse(Call<List<AdminUserView>> call,
                                           Response<List<AdminUserView>> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {

                            allUsers = response.body();

                            renderCurrentTab();

                        } else {

                            Toast.makeText(getContext(),
                                    "Unable to load users",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AdminUserView>> call,
                                          Throwable t) {

                        if (!isAdded()) return;

                        Toast.makeText(getContext(),
                                "Server connection failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderCurrentTab() {

        List<AdminUserView> filtered = new ArrayList<>();

        for (AdminUserView user : allUsers) {

            if (selectedTab == 0) {

                filtered.add(user);

            } else if (selectedTab == 1 &&
                    "OWNER".equalsIgnoreCase(user.getRole())) {

                filtered.add(user);

            } else if (selectedTab == 2 &&
                    "USER".equalsIgnoreCase(user.getRole())) {

                filtered.add(user);
            }
        }

        adapter.setUsers(filtered);

        txtEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmToggleUser(AdminUserView user) {

        boolean active = Boolean.TRUE.equals(user.getIsActive());

        String action = active ? "Deactivate" : "Activate";

        String message = active
                ? "This will prevent the user from logging into the app."
                : "This will restore the user's access.";

        new AlertDialog.Builder(requireContext())
                .setTitle(action + " " + user.getName() + "?")
                .setMessage(message)
                .setPositiveButton(action, (dialog, which) -> {

                    if (active) {
                        deactivateUser(user.getId());
                    } else {
                        activateUser(user.getId());
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void activateUser(int id) {

        ApiClient.getApiService(requireContext())
                .activateUser(id)
                .enqueue(new Callback<AdminUserView>() {

                    @Override
                    public void onResponse(Call<AdminUserView> call,
                                           Response<AdminUserView> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful()) {

                            Toast.makeText(getContext(),
                                    "User activated",
                                    Toast.LENGTH_SHORT).show();

                            loadUsers();

                        } else {

                            Toast.makeText(getContext(),
                                    "Activation failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AdminUserView> call,
                                          Throwable t) {

                        if (!isAdded()) return;

                        Toast.makeText(getContext(),
                                "Server connection failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deactivateUser(int id) {

        ApiClient.getApiService(requireContext())
                .deactivateUser(id)
                .enqueue(new Callback<AdminUserView>() {

                    @Override
                    public void onResponse(Call<AdminUserView> call,
                                           Response<AdminUserView> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful()) {

                            Toast.makeText(getContext(),
                                    "User deactivated",
                                    Toast.LENGTH_SHORT).show();

                            loadUsers();

                        } else {

                            Toast.makeText(getContext(),
                                    "Deactivation failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AdminUserView> call,
                                          Throwable t) {

                        if (!isAdded()) return;

                        Toast.makeText(getContext(),
                                "Server connection failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}