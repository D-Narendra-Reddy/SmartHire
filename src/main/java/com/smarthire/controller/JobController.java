package com.smarthire.controller;

import com.smarthire.dto.ApiResponse;
import com.smarthire.dto.JobDto;
import com.smarthire.dto.UserDto;
import com.smarthire.service.JobService;
import com.smarthire.service.UserService;
import com.smarthire.util.SmartMatchUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final UserService userService;

    @Autowired
    public JobController(JobService jobService, UserService userService) {
        this.jobService = jobService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<JobDto>> createJob(@Valid @RequestBody JobDto jobDto, Authentication authentication) {
        JobDto createdJob = jobService.createJob(jobDto, authentication.getName());
        return new ResponseEntity<>(new ApiResponse<>(true, "Job posted successfully", createdJob), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobDto>>> getAllJobs(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "location", required = false) String location) {

        Page<JobDto> jobs = jobService.getAllJobs(pageNo, pageSize, sortBy, sortDir, keyword, location);
        return ResponseEntity.ok(new ApiResponse<>(true, "Jobs fetched successfully", jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDto>> getJobById(@PathVariable Long id, Authentication authentication) {
        JobDto job = jobService.getJobById(id);
        
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CANDIDATE"))) {
            try {
                UserDto candidate = userService.getUserProfile(authentication.getName());
                int match = SmartMatchUtil.calculateMatchPercentage(job.getSkillsRequired(), candidate.getSkills());
                job.setMatchPercentage(match);
            } catch (Exception e) {
                // Ignore if candidate profile fails to load
            }
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Job fetched successfully", job));
    }

    @GetMapping("/recruiter")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<List<JobDto>>> getJobsByRecruiter(Authentication authentication) {
        List<JobDto> jobs = jobService.getJobsByRecruiter(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Recruiter jobs fetched successfully", jobs));
    }

    @GetMapping("/recommended")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<List<JobDto>>> getRecommendedJobs(Authentication authentication) {
        List<JobDto> jobs = jobService.getRecommendedJobs(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Recommended jobs fetched successfully", jobs));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<JobDto>> updateJob(@PathVariable Long id, @Valid @RequestBody JobDto jobDto, Authentication authentication) {
        JobDto updatedJob = jobService.updateJob(id, jobDto, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Job updated successfully", updatedJob));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteJob(@PathVariable Long id, Authentication authentication) {
        jobService.deleteJob(id, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Job deleted successfully", null));
    }
}
