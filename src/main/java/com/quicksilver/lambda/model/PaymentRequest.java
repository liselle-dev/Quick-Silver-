package com.quicksilver.lambda.model;

public class PaymentRequest {

    private String userId;
    private Double amount;
    private String paymentMethod;

    public PaymentRequest() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void set
