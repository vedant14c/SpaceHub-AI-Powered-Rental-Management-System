package com.myapplication.office_spaces.fragments;

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

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.BookingAdapter;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.models.PropertyRequest;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingListPageFragment extends Fragment {

    private static final String ARG_BUCKET = "arg_bucket";

    private RecyclerView recyclerBookings;
    private View progressBar, layoutEmpty;
    private BookingAdapter adapter;
    private BookingAdapter.BookingBucket bucket;

    private List<PropertyRequest> allRequests = new ArrayList<>();
    private Map<Integer, Property> propertiesById = new HashMap<>();

    public static BookingListPageFragment newInstance(BookingAdapter.BookingBucket bucket) {
        BookingListPageFragment fragment = new BookingListPageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BUCKET, bucket);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bucket = (BookingAdapter.BookingBucket) getArguments().getSerializable(ARG_BUCKET);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bookings_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerBookings = view.findViewById(R.id.recyclerBookings);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        adapter = new BookingAdapter(request -> {
            // Click listener
        });
        recyclerBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerBookings.setAdapter(adapter);

        loadBookings();
    }

    private void loadBookings() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        int userId = new SessionManager(requireContext()).getUserId();

        ApiClient.getApiService(requireContext()).getRequestsByUser(userId, null)
                .enqueue(new Callback<List<PropertyRequest>>() {
                    @Override
                    public void onResponse(Call<List<PropertyRequest>> call, Response<List<PropertyRequest>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            filterAndFetch(response.body());
                        } else {
                            progressBar.setVisibility(View.GONE);
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PropertyRequest>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void filterAndFetch(List<PropertyRequest> requests) {
        List<PropertyRequest> filtered = new ArrayList<>();
        Set<Integer> propertyIds = new HashSet<>();
        for (PropertyRequest r : requests) {
            if (BookingAdapter.classify(r) == bucket) {
                filtered.add(r);
                if (r.getPropertyId() != null) propertyIds.add(r.getPropertyId());
            }
        }

        if (filtered.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            adapter.setData(new ArrayList<>(), new HashMap<>());
            return;
        }

        allRequests = filtered;
        fetchProperties(propertyIds);
    }

    private void fetchProperties(Set<Integer> ids) {
        AtomicInteger remaining = new AtomicInteger(ids.size());
        for (Integer id : ids) {
            ApiClient.getApiService(requireContext()).getPropertyById(id).enqueue(new Callback<Property>() {
                @Override
                public void onResponse(Call<Property> call, Response<Property> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        propertiesById.put(id, response.body());
                    }
                    checkFinished(remaining);
                }

                @Override
                public void onFailure(Call<Property> call, Throwable t) {
                    checkFinished(remaining);
                }
            });
        }
    }

    private void checkFinished(AtomicInteger remaining) {
        if (remaining.decrementAndGet() == 0 && isAdded()) {
            progressBar.setVisibility(View.GONE);
            adapter.setData(allRequests, propertiesById);
            layoutEmpty.setVisibility(allRequests.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}