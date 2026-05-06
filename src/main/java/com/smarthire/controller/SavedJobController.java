package com.smarthire.controller;

import com.smarthire.dto.ApiResponse;
import com.smarthire.dto.JobDto;
import com.smarthire.service.SavedJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-jobs")
@PreAuthorize("hasRole('CANDIDATE')")
public class SavedJobController {

    private final SavedJobService savedJobService;

    @Autowired
    public SavedJobController(SavedJobService savedJobService) {
        this.savedJobService = savedJobService;
    }

    @PostMapping("/{jobId}")
    public ResponseEntity<ApiResponse<String>> saveJob(@PathVariable Long jobId, Authentication authentication) {
        savedJobService.saveJob(jobId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Job saved successfully", null));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<ApiResponse<String>> unsaveJob(@PathVariable Long jobId, Authentication authentication) {
        savedJobService.unsaveJob(jobId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Job removed from saved list", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobDto>>> getSavedJobs(Authentication authentication) {
        List<JobDto> jobs = savedJobService.getSavedJobs(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Saved jobs fetched successfully", jobs));
    }
}
