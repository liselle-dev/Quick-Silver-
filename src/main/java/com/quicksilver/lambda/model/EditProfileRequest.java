package com.quicksilver.lambda.model;

public class EditProfileRequest {

    private String userId;
    private String newEmail;
    private String newPhone;

    // Default constructor
    public EditProfileRequest() {}

    // Constructor with all fields
    public EditProfileRequest(String userId, String newEmail, String newPhone) {
        this.userId = userId;
        this.newEmail = newEmail;
        this.newPhone = newPhone;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getNewPhone() {
        return newPhone;
    }

    public void setNewPhone(String newPhone) {
        this.newPhone = newPhone;
    }

    @Override
    public String toString() {
        return "EditProfileRequest{" +
                "userId='" + userId + '\'' +
                ", newEmail='" + newEmail + '\'' +
                ", newPhone='" + newPhone + '\'' +
                '}';
    }
}
