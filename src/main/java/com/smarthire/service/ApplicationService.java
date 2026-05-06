package com.smarthire.service;

import com.smarthire.dto.JobApplicationDto;

import java.util.List;

public interface ApplicationService {
    JobApplicationDto applyForJob(Long jobId, String candidateEmail);
    List<JobApplicationDto> getApplicationsByCandidate(String candidateEmail);
    List<JobApplicationDto> getApplicationsForJob(Long jobId, String recruiterEmail, String sortBy);
    JobApplicationDto updateApplicationStatus(Long applicationId, String status, String recruiterEmail);
    void withdrawApplication(Long applicationId, String candidateEmail);
}
