package com.myapplication.office_spaces.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviews = new ArrayList<>();

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        
        // Note: Backend Review model has userId but not the user name.
        // For now we'll show "Anonymous" or "User #ID" since we don't have a user join here.
        holder.txtReviewerName.setText(String.format("User #%d", review.getUserId()));
        
        holder.txtReviewRating.setText(String.format(Locale.getDefault(), "%.1f", review.getRating()));
        holder.txtComment.setText(review.getComment());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtReviewerName, txtReviewRating, txtComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReviewerName = itemView.findViewById(R.id.txtReviewerName);
            txtReviewRating = itemView.findViewById(R.id.txtReviewRating);
            txtComment = itemView.findViewById(R.id.txtComment);
        }
    }
}