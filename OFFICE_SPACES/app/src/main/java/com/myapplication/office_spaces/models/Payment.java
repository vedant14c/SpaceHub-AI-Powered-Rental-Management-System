package com.myapplication.office_spaces.models;

public class Payment {

    private int paymentId;
    private int requestId;
    private int userId;
    private double amount;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private String status;

    public int getPaymentId() {
        return paymentId;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public String getStatus() {
        return status;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}