package com.smarthire.service;

import com.smarthire.dto.CompanyDto;
import com.smarthire.dto.UserDto;

public interface UserService {
    UserDto getUserProfile(String email);
    UserDto updateUserProfile(UserDto userDto, String email);
    CompanyDto getCompanyProfile(String recruiterEmail);
    CompanyDto createOrUpdateCompanyProfile(CompanyDto companyDto, String recruiterEmail);
}
