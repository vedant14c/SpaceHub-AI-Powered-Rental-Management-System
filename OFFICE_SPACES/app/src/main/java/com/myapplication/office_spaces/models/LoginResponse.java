package com.myapplication.office_spaces.models;


public class LoginResponse {
    private int userId;
    private String name;
    private String email;
    private String role;
    private String message;
    private String token;

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getMessage() { return message; }
    public String getToken() { return token; }

    // Backend returns HTTP 200 even on bad credentials, with only "message" set
    public boolean isSuccessful() { return token != null && !token.isEmpty(); }
}