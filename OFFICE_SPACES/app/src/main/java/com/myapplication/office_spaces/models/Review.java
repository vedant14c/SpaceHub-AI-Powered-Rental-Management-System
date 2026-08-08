package com.myapplication.office_spaces.models;

public class Review {
    private Integer id;
    private Integer propertyId;
    private Integer userId;
    private Float rating;
    private String comment;

    public Integer getId() { return id; }
    public Integer getPropertyId() { return propertyId; }
    public Integer getUserId() { return userId; }
    public Float getRating() { return rating; }
    public String getComment() { return comment; }
}