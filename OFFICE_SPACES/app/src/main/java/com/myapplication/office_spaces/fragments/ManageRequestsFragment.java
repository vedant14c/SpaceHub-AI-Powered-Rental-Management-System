package com.myapplication.office_spaces.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.RequestAdapter;
import com.myapplication.office_spaces.models.Notification;
import com.myapplication.office_spaces.models.PropertyRequest;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageRequestsFragment extends Fragment
        implements RequestAdapter.OnRequestActionListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;

    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_manage_requests,
                container,
                false);

        recyclerView = view.findViewById(R.id.recyclerRequests);
        progressBar = view.findViewById(R.id.progressBar);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        apiService = ApiClient.getApiService(requireContext());

        loadRequests();

        return view;
    }

    private void loadRequests() {

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);

        apiService.getAllRequests(null).enqueue(new Callback<List<PropertyRequest>>() {

            @Override
            public void onResponse(Call<List<PropertyRequest>> call,
                                   Response<List<PropertyRequest>> response) {

                if (!isAdded()) return;

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {

                    List<PropertyRequest> list = response.body();

                    if (list.isEmpty()) {
                        txtEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);

                        RequestAdapter adapter =
                                new RequestAdapter(
                                        requireContext(),
                                        list,
                                        ManageRequestsFragment.this
                                );

                        recyclerView.setAdapter(adapter);
                    }

                } else {
                    txtEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<PropertyRequest>> call,
                                  Throwable t) {

                if (!isAdded()) return;

                progressBar.setVisibility(View.GONE);
                txtEmpty.setVisibility(View.VISIBLE);

                Toast.makeText(
                        requireContext(),
                        "Unable to load requests",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    @Override
    public void onApprove(PropertyRequest request) {
        updateStatus(request.getRequestId(), "APPROVED");
    }

    @Override
    public void onReject(PropertyRequest request) {
        updateStatus(request.getRequestId(), "REJECTED");
    }

    private void updateStatus(Integer requestId, String status) {

        apiService.updateRequestStatus(requestId, status)
                .enqueue(new Callback<PropertyRequest>() {

                    @Override
                    public void onResponse(Call<PropertyRequest> call,
                                           Response<PropertyRequest> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful()) {

                            PropertyRequest updatedRequest = response.body();

                            if (updatedRequest != null) {
                                Notification notification = new Notification();

                                notification.setUserId(updatedRequest.getUserId());
                                notification.setRequestId(updatedRequest.getRequestId());

                                notification.setTitle(
                                        "Request " +
                                                status.substring(0, 1).toUpperCase() +
                                                status.substring(1)
                                );

                                notification.setMessage(
                                        "Your " +
                                                updatedRequest.getRequestType() +
                                                " request has been " +
                                                status +
                                                "."
                                );

                                apiService.addNotification(notification).enqueue(new Callback<Notification>() {
                                    @Override
                                    public void onResponse(Call<Notification> call, Response<Notification> response) {}

                                    @Override
                                    public void onFailure(Call<Notification> call, Throwable t) {}
                                });
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Request Updated",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadRequests();

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    "Update Failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PropertyRequest> call,
                                          Throwable t) {

                        if (!isAdded()) return;

                        Toast.makeText(
                                requireContext(),
                                "Server Error",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}