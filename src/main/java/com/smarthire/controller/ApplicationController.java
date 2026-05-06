package com.smarthire.controller;

import com.smarthire.dto.ApiResponse;
import com.smarthire.dto.JobApplicationDto;
import com.smarthire.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/job/{jobId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<JobApplicationDto>> applyForJob(@PathVariable Long jobId, Authentication authentication) {
        JobApplicationDto application = applicationService.applyForJob(jobId, authentication.getName());
        return new ResponseEntity<>(new ApiResponse<>(true, "Applied successfully", application), HttpStatus.CREATED);
    }

    @GetMapping("/candidate")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<List<JobApplicationDto>>> getApplicationsByCandidate(Authentication authentication) {
        List<JobApplicationDto> applications = applicationService.getApplicationsByCandidate(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Applications fetched successfully", applications));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<List<JobApplicationDto>>> getApplicationsForJob(
            @PathVariable Long jobId, 
            @RequestParam(required = false) String sortBy,
            Authentication authentication) {
        List<JobApplicationDto> applications = applicationService.getApplicationsForJob(jobId, authentication.getName(), sortBy);
        return ResponseEntity.ok(new ApiResponse<>(true, "Applicants fetched successfully", applications));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<JobApplicationDto>> updateStatus(@PathVariable Long id, @RequestParam String status, Authentication authentication) {
        JobApplicationDto application = applicationService.updateApplicationStatus(id, status, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Status updated successfully", application));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<String>> withdrawApplication(@PathVariable Long id, Authentication authentication) {
        applicationService.withdrawApplication(id, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Application withdrawn successfully", null));
    }
}
