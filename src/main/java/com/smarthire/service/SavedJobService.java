package com.smarthire.service;

import com.smarthire.dto.JobDto;

import java.util.List;

public interface SavedJobService {
    void saveJob(Long jobId, String candidateEmail);
    void unsaveJob(Long jobId, String candidateEmail);
    List<JobDto> getSavedJobs(String candidateEmail);
}
