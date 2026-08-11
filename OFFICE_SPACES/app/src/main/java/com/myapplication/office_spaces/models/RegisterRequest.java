package com.myapplication.office_spaces.models;
public class
RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String role; // "USER", "OWNER", or "ADMIN" — must match backend enum exactly

    public RegisterRequest(String name, String email, String password, String phone, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }
}