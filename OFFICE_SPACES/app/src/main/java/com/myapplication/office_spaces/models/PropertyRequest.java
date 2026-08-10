package com.myapplication.office_spaces.models;


public class PropertyRequest {

    private Integer requestId;
    private Integer propertyId;
    private Integer userId;
    private String requestType;
    private Double offerPrice;
    private String proposedStart;
    private String proposedEnd;
    private String message;
    private String status;
    private Integer reviewedBy;
    private String reviewedAt;
    private String createdAt;

    public PropertyRequest() {
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public Double getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(Double offerPrice) {
        this.offerPrice = offerPrice;
    }

    public String getProposedStart() {
        return proposedStart;
    }

    public void setProposedStart(String proposedStart) {
        this.proposedStart = proposedStart;
    }

    public String getProposedEnd() {
        return proposedEnd;
    }

    public void setProposedEnd(String proposedEnd) {
        this.proposedEnd = proposedEnd;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Integer reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}