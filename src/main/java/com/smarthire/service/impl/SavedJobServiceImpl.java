package com.smarthire.service.impl;

import com.smarthire.dto.CompanyDto;
import com.smarthire.dto.JobDto;
import com.smarthire.entity.Job;
import com.smarthire.entity.SavedJob;
import com.smarthire.entity.User;
import com.smarthire.exception.ResourceNotFoundException;
import com.smarthire.exception.ValidationException;
import com.smarthire.repository.JobRepository;
import com.smarthire.repository.SavedJobRepository;
import com.smarthire.repository.UserRepository;
import com.smarthire.service.SavedJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavedJobServiceImpl implements SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    @Autowired
    public SavedJobServiceImpl(SavedJobRepository savedJobRepository, JobRepository jobRepository, UserRepository userRepository) {
        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void saveJob(Long jobId, String candidateEmail) {
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", candidateEmail));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (savedJobRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new ValidationException("Job is already saved.");
        }

        SavedJob savedJob = new SavedJob();
        savedJob.setJob(job);
        savedJob.setCandidate(candidate);

        savedJobRepository.save(savedJob);
    }

    @Override
    public void unsaveJob(Long jobId, String candidateEmail) {
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", candidateEmail));

        SavedJob savedJob = savedJobRepository.findByJobIdAndCandidateId(jobId, candidate.getId())
                .orElseThrow(() -> new ValidationException("Job is not saved by this user."));

        savedJobRepository.delete(savedJob);
    }

    @Override
    public List<JobDto> getSavedJobs(String candidateEmail) {
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", candidateEmail));

        List<SavedJob> savedJobs = savedJobRepository.findByCandidateId(candidate.getId());

        return savedJobs.stream().map(saved -> mapToDto(saved.getJob())).collect(Collectors.toList());
    }

    private JobDto mapToDto(Job job) {
        JobDto dto = new JobDto();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setSkillsRequired(job.getSkillsRequired());
        dto.setSalary(job.getSalary());
        dto.setExperienceRequired(job.getExperienceRequired());
        dto.setLocation(job.getLocation());
        dto.setEmploymentType(job.getEmploymentType().name());
        dto.setApplicationDeadline(job.getApplicationDeadline());
        dto.setCreatedAt(job.getCreatedAt());

        CompanyDto companyDto = new CompanyDto();
        companyDto.setId(job.getCompany().getId());
        companyDto.setName(job.getCompany().getName());
        companyDto.setLocation(job.getCompany().getLocation());
        dto.setCompany(companyDto);

        return dto;
    }
}
