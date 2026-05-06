package com.smarthire.service.impl;

import com.smarthire.dto.JobApplicationDto;
import com.smarthire.dto.JobDto;
import com.smarthire.dto.ResumeDto;
import com.smarthire.dto.UserDto;
import com.smarthire.entity.ApplicationStatus;
import com.smarthire.entity.Job;
import com.smarthire.entity.JobApplication;
import com.smarthire.entity.Resume;
import com.smarthire.entity.User;
import com.smarthire.exception.ResourceNotFoundException;
import com.smarthire.exception.ValidationException;
import com.smarthire.repository.JobApplicationRepository;
import com.smarthire.repository.JobRepository;
import com.smarthire.repository.ResumeRepository;
import com.smarthire.repository.UserRepository;
import com.smarthire.service.ApplicationService;
import com.smarthire.service.UserService;
import com.smarthire.util.SmartMatchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final UserService userService;

    @Autowired
    public ApplicationServiceImpl(JobApplicationRepository applicationRepository,
                                  JobRepository jobRepository,
                                  UserRepository userRepository,
                                  ResumeRepository resumeRepository,
                                  UserService userService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.userService = userService;
    }

    @Override
    public JobApplicationDto applyForJob(Long jobId, String candidateEmail) {
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", candidateEmail));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new ValidationException("You have already applied for this job.");
        }

        Resume resume = resumeRepository.findByCandidateId(candidate.getId())
                .orElseThrow(() -> new ValidationException("Please upload your resume before applying."));

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setResume(resume);

        JobApplication savedApplication = applicationRepository.save(application);
        return mapToDto(savedApplication);
    }

    @Override
    public List<JobApplicationDto> getApplicationsByCandidate(String candidateEmail) {
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", candidateEmail));

        List<JobApplication> applications = applicationRepository.findByCandidateId(candidate.getId());
        return applications.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<JobApplicationDto> getApplicationsForJob(Long jobId, String recruiterEmail, String sortBy) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (!job.getCompany().getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new ValidationException("You don't have permission to view applications for this job.");
        }

        List<JobApplication> applications = applicationRepository.findByJobId(jobId);
        List<JobApplicationDto> dtos = applications.stream().map(this::mapToDto).collect(Collectors.toList());

        if (sortBy != null && !sortBy.isEmpty()) {
            if (sortBy.equalsIgnoreCase("match")) {
                dtos.sort((a, b) -> Integer.compare(
                        b.getMatchPercentage() != null ? b.getMatchPercentage() : 0,
                        a.getMatchPercentage() != null ? a.getMatchPercentage() : 0));
            } else if (sortBy.equalsIgnoreCase("completeness")) {
                dtos.sort((a, b) -> Integer.compare(
                        b.getCandidate().getProfileCompletenessScore() != null ? b.getCandidate().getProfileCompletenessScore() : 0,
                        a.getCandidate().getProfileCompletenessScore() != null ? a.getCandidate().getProfileCompletenessScore() : 0));
            } else if (sortBy.equalsIgnoreCase("experience")) {
                // Experience is string, but we can do a basic string compare or attempt parsing if numbers. Simple string sort for now.
                dtos.sort((a, b) -> {
                    String expA = a.getCandidate().getExperience() != null ? a.getCandidate().getExperience() : "";
                    String expB = b.getCandidate().getExperience() != null ? b.getCandidate().getExperience() : "";
                    return expB.compareToIgnoreCase(expA); // Descending
                });
            }
        }

        return dtos;
    }

    @Override
    public JobApplicationDto updateApplicationStatus(Long applicationId, String status, String recruiterEmail) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", applicationId));

        if (!application.getJob().getCompany().getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new ValidationException("You don't have permission to update this application.");
        }

        try {
            ApplicationStatus newStatus = ApplicationStatus.valueOf(status.toUpperCase());
            application.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status provided.");
        }

        JobApplication updatedApplication = applicationRepository.save(application);
        return mapToDto(updatedApplication);
    }

    @Override
    public void withdrawApplication(Long applicationId, String candidateEmail) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", applicationId));

        if (!application.getCandidate().getEmail().equals(candidateEmail)) {
            throw new ValidationException("You don't have permission to withdraw this application.");
        }

        applicationRepository.delete(application);
    }

    private JobApplicationDto mapToDto(JobApplication application) {
        JobApplicationDto dto = new JobApplicationDto();
        dto.setId(application.getId());
        dto.setStatus(application.getStatus().name());
        dto.setAppliedAt(application.getAppliedAt());

        JobDto jobDto = new JobDto();
        jobDto.setId(application.getJob().getId());
        jobDto.setTitle(application.getJob().getTitle());
        dto.setJob(jobDto);

        UserDto userDto = userService.getUserProfile(application.getCandidate().getEmail());
        dto.setCandidate(userDto);

        ResumeDto resumeDto = new ResumeDto();
        resumeDto.setId(application.getResume().getId());
        resumeDto.setFileUrl(application.getResume().getFileUrl());
        resumeDto.setFileName(application.getResume().getFileName());
        dto.setResume(resumeDto);

        int match = SmartMatchUtil.calculateMatchPercentage(
                application.getJob().getSkillsRequired(),
                application.getCandidate().getSkills());
        dto.setMatchPercentage(match);

        return dto;
    }
}
