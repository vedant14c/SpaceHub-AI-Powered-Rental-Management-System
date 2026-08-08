package com.myapplication.office_spaces.models;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("reviewId")
    private Integer id;
    
    private Integer propertyId;
    
    @SerializedName("reviewerId")
    private Integer userId;
    
    private Float rating;
    private String comment;
    private String createdAt;

    public Integer getId() { return id; }
    public Integer getPropertyId() { return propertyId; }
    public Integer getUserId() { return userId; }
    public Float getRating() { return rating; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }
}