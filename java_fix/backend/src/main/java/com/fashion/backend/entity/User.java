package com.fashion.backend.entity;

public record User(Integer id, String fullname, String email, String password,
                   String role, Integer status) {}
