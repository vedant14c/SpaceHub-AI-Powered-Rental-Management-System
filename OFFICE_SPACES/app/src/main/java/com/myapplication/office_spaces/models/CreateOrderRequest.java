package com.myapplication.office_spaces.models;

public class CreateOrderRequest {

    private int userId;

    public CreateOrderRequest(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}