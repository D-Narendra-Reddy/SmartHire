package com.smarthire.service;

import com.smarthire.dto.UserDto;

import java.util.List;

public interface AdminService {
    List<UserDto> getAllUsers();
    void deleteUser(Long userId);
    void deleteJobByAdmin(Long jobId);
}
