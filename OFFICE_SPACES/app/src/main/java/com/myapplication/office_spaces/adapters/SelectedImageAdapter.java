package com.myapplication.office_spaces.adapters;


import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.myapplication.office_spaces.R;

import java.util.ArrayList;
import java.util.List;

public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.ImageViewHolder> {

    public interface OnRemoveClickListener {
        void onRemove(int position);
    }

    private final List<Uri> imageUris = new ArrayList<>();
    private final OnRemoveClickListener listener;

    public SelectedImageAdapter(OnRemoveClickListener listener) {
        this.listener = listener;
    }

    public void setImages(List<Uri> uris) {
        imageUris.clear();
        imageUris.addAll(uris);
        notifyDataSetChanged();
    }

    public List<Uri> getImages() {
        return imageUris;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = imageUris.get(position);
        Glide.with(holder.itemView.getContext()).load(uri).into(holder.imgThumbnail);
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail, btnRemove;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}