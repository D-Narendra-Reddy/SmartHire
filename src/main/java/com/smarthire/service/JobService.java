package com.smarthire.service;

import com.smarthire.dto.JobDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface JobService {
    JobDto createJob(JobDto jobDto, String recruiterEmail);
    JobDto getJobById(Long id);
    Page<JobDto> getAllJobs(int pageNo, int pageSize, String sortBy, String sortDir, String keyword, String location);
    List<JobDto> getJobsByRecruiter(String recruiterEmail);
    JobDto updateJob(Long id, JobDto jobDto, String recruiterEmail);
    void deleteJob(Long id, String email);
    List<JobDto> getRecommendedJobs(String candidateEmail);
}
