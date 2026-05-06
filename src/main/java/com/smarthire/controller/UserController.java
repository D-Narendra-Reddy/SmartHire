package com.smarthire.controller;

import com.smarthire.dto.ApiResponse;
import com.smarthire.dto.CompanyDto;
import com.smarthire.dto.UserDto;
import com.smarthire.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(Authentication authentication) {
        UserDto user = userService.getUserProfile(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile fetched successfully", user));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@RequestBody UserDto userDto, Authentication authentication) {
        UserDto updatedUser = userService.updateUserProfile(userDto, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", updatedUser));
    }

    @GetMapping("/company")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<CompanyDto>> getCompanyProfile(Authentication authentication) {
        CompanyDto company = userService.getCompanyProfile(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Company profile fetched successfully", company));
    }

    @PostMapping("/company")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<CompanyDto>> createOrUpdateCompanyProfile(@RequestBody CompanyDto companyDto, Authentication authentication) {
        CompanyDto updatedCompany = userService.createOrUpdateCompanyProfile(companyDto, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Company profile saved successfully", updatedCompany));
    }
}
