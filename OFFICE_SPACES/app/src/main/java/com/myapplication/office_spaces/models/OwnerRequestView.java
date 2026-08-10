package com.myapplication.office_spaces.models;

public class OwnerRequestView {

    private Integer requestId;
    private Integer propertyId;
    private String propertyTitle;
    private Integer userId;
    private String requesterName;
    private String requestType;
    private Double offerPrice;
    private String proposedStart;
    private String proposedEnd;
    private String status;
    private String createdAt;

    public OwnerRequestView() {
    }

    public Integer getRequestId() {
        return requestId;
    }

    public Integer getPropertyId() {
        return propertyId;
    }

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public String getRequestType() {
        return requestType;
    }

    public Double getOfferPrice() {
        return offerPrice;
    }

    public String getProposedStart() {
        return proposedStart;
    }

    public String getProposedEnd() {
        return proposedEnd;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}