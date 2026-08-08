package com.myapplication.office_spaces.models;

public class AdminDashboard {

    private long totalUsers;
    private long propertyOwners;
    private long rentersBuyers;
    private long admins;

    private long totalListings;
    private long pendingListings;
    private long approvedListings;
    private long rejectedListings;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getPropertyOwners() {
        return propertyOwners;
    }

    public void setPropertyOwners(long propertyOwners) {
        this.propertyOwners = propertyOwners;
    }

    public long getRentersBuyers() {
        return rentersBuyers;
    }

    public void setRentersBuyers(long rentersBuyers) {
        this.rentersBuyers = rentersBuyers;
    }

    public long getAdmins() {
        return admins;
    }

    public void setAdmins(long admins) {
        this.admins = admins;
    }

    public long getTotalListings() {
        return totalListings;
    }

    public void setTotalListings(long totalListings) {
        this.totalListings = totalListings;
    }

    public long getPendingListings() {
        return pendingListings;
    }

    public void setPendingListings(long pendingListings) {
        this.pendingListings = pendingListings;
    }

    public long getApprovedListings() {
        return approvedListings;
    }

    public void setApprovedListings(long approvedListings) {
        this.approvedListings = approvedListings;
    }

    public long getRejectedListings() {
        return rejectedListings;
    }

    public void setRejectedListings(long rejectedListings) {
        this.rejectedListings = rejectedListings;
    }
}