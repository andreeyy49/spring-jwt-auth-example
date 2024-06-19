package com.example.spring_jwt_auth_example.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
public class AppController {

    // Будет доступен всем
    @GetMapping("/all")
    public String allAccess() {
        return "Public response data";
    }

    // Будет доступен ADMIN'ам
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin response data";
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public String moderatorAccess() {
        return "Manager response data";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public String userAccess() {
        return "User response data";
    }

}
