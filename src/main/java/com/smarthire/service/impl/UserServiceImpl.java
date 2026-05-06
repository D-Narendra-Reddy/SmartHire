package com.smarthire.service.impl;

import com.smarthire.dto.CompanyDto;
import com.smarthire.dto.UserDto;
import com.smarthire.entity.Company;
import com.smarthire.entity.Role;
import com.smarthire.entity.User;
import com.smarthire.exception.ResourceNotFoundException;
import com.smarthire.exception.ValidationException;
import com.smarthire.repository.CompanyRepository;
import com.smarthire.repository.ResumeRepository;
import com.smarthire.repository.UserRepository;
import com.smarthire.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ResumeRepository resumeRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, CompanyRepository companyRepository, ResumeRepository resumeRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.resumeRepository = resumeRepository;
    }

    @Override
    public UserDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return mapToDto(user);
    }

    @Override
    public UserDto updateUserProfile(UserDto userDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        user.setName(userDto.getName());
        user.setPhone(userDto.getPhone());
        user.setTitle(userDto.getTitle());
        user.setBio(userDto.getBio());
        user.setSkills(userDto.getSkills());
        user.setExperience(userDto.getExperience());
        user.setEducation(userDto.getEducation());

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    public CompanyDto getCompanyProfile(String recruiterEmail) {
        User user = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", recruiterEmail));

        Company company = companyRepository.findByRecruiterId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile", "recruiter", recruiterEmail));

        return mapToDto(company);
    }

    @Override
    public CompanyDto createOrUpdateCompanyProfile(CompanyDto companyDto, String recruiterEmail) {
        User user = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", recruiterEmail));

        boolean isRecruiter = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_RECRUITER"));
        if (!isRecruiter) {
            throw new ValidationException("Only recruiters can create a company profile.");
        }

        Company company = companyRepository.findByRecruiterId(user.getId()).orElse(new Company());

        company.setName(companyDto.getName());
        company.setDescription(companyDto.getDescription());
        company.setWebsite(companyDto.getWebsite());
        company.setLocation(companyDto.getLocation());
        company.setRecruiter(user);

        Company savedCompany = companyRepository.save(company);
        return mapToDto(savedCompany);
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setTitle(user.getTitle());
        dto.setBio(user.getBio());
        dto.setSkills(user.getSkills());
        dto.setExperience(user.getExperience());
        dto.setEducation(user.getEducation());
        dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

        int score = 0;
        if (user.getName() != null && !user.getName().isEmpty()) score += 8;
        if (user.getEmail() != null && !user.getEmail().isEmpty()) score += 8;
        if (user.getPhone() != null && !user.getPhone().isEmpty()) score += 8;
        if (user.getTitle() != null && !user.getTitle().isEmpty()) score += 8;
        if (user.getBio() != null && !user.getBio().isEmpty()) score += 8;
        
        if (user.getSkills() != null && !user.getSkills().isEmpty()) score += 20;
        if (user.getExperience() != null && !user.getExperience().isEmpty()) score += 10;
        if (user.getEducation() != null && !user.getEducation().isEmpty()) score += 10;
        
        if (resumeRepository.findByCandidateId(user.getId()).isPresent()) score += 20;

        dto.setProfileCompletenessScore(score);
        return dto;
    }

    private CompanyDto mapToDto(Company company) {
        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setWebsite(company.getWebsite());
        dto.setLocation(company.getLocation());
        dto.setCreatedAt(company.getCreatedAt());
        return dto;
    }
}
