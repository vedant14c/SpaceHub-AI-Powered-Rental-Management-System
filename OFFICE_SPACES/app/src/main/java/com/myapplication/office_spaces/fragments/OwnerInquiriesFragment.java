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
import com.myapplication.office_spaces.adapters.OwnerInquiryAdapter;
import com.myapplication.office_spaces.models.Notification;
import com.myapplication.office_spaces.models.OwnerRequestView;
import com.myapplication.office_spaces.models.PropertyRequest;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerInquiriesFragment extends Fragment {

    private RecyclerView recyclerInquiries;
    private View progressBar;
    private TextView txtEmpty;
    private TabLayout tabLayout;
    private OwnerInquiryAdapter adapter;

    private List<OwnerRequestView> allRequests = new ArrayList<>();
    private int selectedTab = 0; // 0=New(pending), 1=Responded(accepted/rejected/cancelled)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_inquiries, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerInquiries = view.findViewById(R.id.recyclerInquiries);
        progressBar = view.findViewById(R.id.progressBar);
        txtEmpty = view.findViewById(R.id.txtEmpty);
        tabLayout = view.findViewById(R.id.tabLayout);

        setupRecycler();
        setupTabs();
        loadInquiries();
    }

    private void setupRecycler() {
        adapter = new OwnerInquiryAdapter(this::showActionDialog);
        recyclerInquiries.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerInquiries.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                renderCurrentTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void loadInquiries() {
        progressBar.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);

        int ownerId = new SessionManager(requireContext()).getUserId();

        ApiClient.getApiService(requireContext()).getRequestsByOwner(ownerId)
                .enqueue(new Callback<List<OwnerRequestView>>() {
                    @Override
                    public void onResponse(Call<List<OwnerRequestView>> call, Response<List<OwnerRequestView>> response) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);

                        if (!response.isSuccessful() || response.body() == null) {
                            txtEmpty.setVisibility(View.VISIBLE);
                            return;
                        }

                        allRequests = response.body();
                        for (OwnerRequestView r : allRequests) {
                            android.util.Log.d(
                                    "OWNER_STATUS",
                                    "ID=" + r.getRequestId() +
                                            " Status=" + r.getStatus()
                            );
                        }
                        renderCurrentTab();
                    }

                    @Override
                    public void onFailure(Call<List<OwnerRequestView>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        txtEmpty.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "Couldn't load inquiries. Check your connection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderCurrentTab() {
        List<OwnerRequestView> filtered = new ArrayList<>();

        for (OwnerRequestView r : allRequests) {
            boolean isPending = r.getStatus() == null || r.getStatus().equalsIgnoreCase("pending");
            if (selectedTab == 0 && isPending) {
                filtered.add(r);
            } else if (selectedTab == 1 && !isPending) {
                filtered.add(r);
            }
        }

        adapter.setRequests(filtered);
        txtEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showActionDialog(OwnerRequestView request) {
        boolean isPending = request.getStatus() == null || request.getStatus().equalsIgnoreCase("pending");

        if (!isPending) {
            Toast.makeText(requireContext(), "Already " + request.getStatus() + ".", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Property Request")
                .setMessage(
                        "Requester: " + request.getRequesterName()
                                + "\n\nProperty: " + request.getPropertyTitle()
                                + "\nType: " + request.getRequestType()
                                + "\n\nDo you want to approve this request?"
                )                .setMessage("Requesting: " + request.getRequestType() + " for " + request.getPropertyTitle())
                .setPositiveButton("Approve",(dialog, which) -> updateStatus(request, "approved"))                .setNegativeButton("Reject", (dialog, which) -> updateStatus(request, "rejected"))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void updateStatus(OwnerRequestView request, String newStatus) {
        ApiClient.getApiService(requireContext())
                .updateRequestStatus(request.getRequestId(), newStatus)
                .enqueue(new Callback<PropertyRequest>() {
                    @Override
                    public void onResponse(Call<PropertyRequest> call, Response<PropertyRequest> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {

                            PropertyRequest updatedRequest = response.body();

                            if (updatedRequest != null) {
                                Notification notification = new Notification();

                                notification.setUserId(updatedRequest.getUserId());
                                notification.setRequestId(updatedRequest.getRequestId());

                                notification.setTitle(
                                        "Request " +
                                                newStatus.substring(0, 1).toUpperCase() +
                                                newStatus.substring(1)
                                );

                                notification.setMessage(
                                        "Your " +
                                                updatedRequest.getRequestType() +
                                                " request has been " +
                                                newStatus +
                                                "."
                                );

                                ApiClient.getApiService(getContext()).addNotification(notification).enqueue(new Callback<Notification>() {
                                    @Override
                                    public void onResponse(Call<Notification> call, Response<Notification> response) {}

                                    @Override
                                    public void onFailure(Call<Notification> call, Throwable t) {}
                                });
                            }

                            Toast.makeText(getContext(), "Request " + newStatus + ".", Toast.LENGTH_SHORT).show();
                            loadInquiries();
                        } else {
                            Toast.makeText(getContext(), "Couldn't update the request.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PropertyRequest> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Couldn't reach the server.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}