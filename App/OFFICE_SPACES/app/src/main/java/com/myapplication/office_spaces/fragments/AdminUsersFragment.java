package com.myapplication.office_spaces.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.UserAdapter;
import com.myapplication.office_spaces.models.AdminUserView;
import com.myapplication.office_spaces.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsersFragment extends Fragment {

    private RecyclerView recyclerUsers;
    private SearchView searchUsers;
    private View progressBar;

    private UserAdapter adapter;

    private List<AdminUserView> allUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_admin_users,
                container,
                false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerUsers = view.findViewById(R.id.recyclerUsers);
        searchUsers = view.findViewById(R.id.searchUsers);
        progressBar = view.findViewById(R.id.progressBar);

        setupRecycler();

        loadUsers();
        searchUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }

        });
    }

    private void setupRecycler() {

        adapter = new UserAdapter(requireContext(),
                new UserAdapter.OnUserActionListener() {

                    @Override
                    public void onActivate(AdminUserView user) {
                        activateUser(user.getId());
                    }

                    @Override
                    public void onDeactivate(AdminUserView user) {
                        deactivateUser(user.getId());
                    }

                });

        recyclerUsers.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        recyclerUsers.setAdapter(adapter);
    }

    private void loadUsers() {

        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService(requireContext())
                .getAllUsers()
                .enqueue(new Callback<List<AdminUserView>>() {

                    @Override
                    public void onResponse(Call<List<AdminUserView>> call,
                                           Response<List<AdminUserView>> response) {

                        if (!isAdded())
                            return;

                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null) {

                            allUsers = response.body();

                            adapter.setUsers(allUsers);

                        } else {

                            Toast.makeText(getContext(),
                                    "Unable to load users.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AdminUserView>> call,
                                          Throwable t) {

                        if (!isAdded())
                            return;

                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(getContext(),
                                "Server connection failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
    private void filterUsers(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            adapter.setUsers(allUsers);
            return;
        }

        List<AdminUserView> filtered = new ArrayList<>();

        keyword = keyword.toLowerCase().trim();

        for (AdminUserView user : allUsers) {

            if ((user.getName() != null &&
                    user.getName().toLowerCase().contains(keyword))

                    ||

                    (user.getEmail() != null &&
                            user.getEmail().toLowerCase().contains(keyword))

                    ||

                    (user.getRole() != null &&
                            user.getRole().toLowerCase().contains(keyword))) {

                filtered.add(user);
            }
        }

        adapter.setUsers(filtered);
    }
}