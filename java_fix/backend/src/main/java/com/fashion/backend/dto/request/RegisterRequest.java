package com.fashion.backend.dto.request;

public record RegisterRequest(String fullname, String email, String username, String account,
                              String password, String phone, String address) {}
