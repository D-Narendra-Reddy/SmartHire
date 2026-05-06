package com.smarthire.service.impl;

import com.smarthire.dto.CompanyDto;
import com.smarthire.dto.JobDto;
import com.smarthire.entity.Company;
import com.smarthire.entity.EmploymentType;
import com.smarthire.entity.Job;
import com.smarthire.entity.User;
import com.smarthire.exception.ResourceNotFoundException;
import com.smarthire.exception.ValidationException;
import com.smarthire.repository.CompanyRepository;
import com.smarthire.repository.JobRepository;
import com.smarthire.repository.UserRepository;
import com.smarthire.service.JobService;
import com.smarthire.util.SmartMatchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Autowired
    public JobServiceImpl(JobRepository jobRepository, CompanyRepository companyRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    @Override
    public JobDto createJob(JobDto jobDto, String recruiterEmail) {
        User user = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", recruiterEmail));

        Company company = companyRepository.findByRecruiterId(user.getId())
                .orElseThrow(() -> new ValidationException("Please create a company profile first before posting jobs."));

        Job job = mapToEntity(jobDto);
        job.setCompany(company);

        Job savedJob = jobRepository.save(job);
        return mapToDto(savedJob);
    }

    @Override
    public JobDto getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));
        return mapToDto(job);
    }

    @Override
    public Page<JobDto> getAllJobs(int pageNo, int pageSize, String sortBy, String sortDir, String keyword, String location) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Job> jobs;
        if ((keyword != null && !keyword.isEmpty()) || (location != null && !location.isEmpty())) {
            String kw = (keyword != null) ? keyword : "";
            String loc = (location != null) ? location : "";
            jobs = jobRepository.searchJobs(kw, loc, pageable);
        } else {
            jobs = jobRepository.findAll(pageable);
        }

        return jobs.map(this::mapToDto);
    }

    @Override
    public List<JobDto> getJobsByRecruiter(String recruiterEmail) {
        User user = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", recruiterEmail));

        Company company = companyRepository.findByRecruiterId(user.getId())
                .orElseThrow(() -> new ValidationException("No company profile found."));

        List<Job> jobs = jobRepository.findByCompanyId(company.getId());
        return jobs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public JobDto updateJob(Long id, JobDto jobDto, String recruiterEmail) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));

        // Security check
        if (!job.getCompany().getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new ValidationException("You don't have permission to update this job");
        }

        job.setTitle(jobDto.getTitle());
        job.setDescription(jobDto.getDescription());
        job.setSkillsRequired(jobDto.getSkillsRequired());
        job.setSalary(jobDto.getSalary());
        job.setExperienceRequired(jobDto.getExperienceRequired());
        job.setLocation(jobDto.getLocation());
        job.setEmploymentType(EmploymentType.valueOf(jobDto.getEmploymentType().toUpperCase()));
        job.setApplicationDeadline(jobDto.getApplicationDeadline());

        Job updatedJob = jobRepository.save(job);
        return mapToDto(updatedJob);
    }

    @Override
    public void deleteJob(Long id, String email) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));

        User user = userRepository.findByEmail(email).orElseThrow();
        
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !job.getCompany().getRecruiter().getEmail().equals(email)) {
            throw new ValidationException("You don't have permission to delete this job");
        }

        jobRepository.delete(job);
    }

    @Override
    public List<JobDto> getRecommendedJobs(String candidateEmail) {
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", candidateEmail));

        // Fetch recent 50 jobs to evaluate
        Pageable pageable = PageRequest.of(0, 50, Sort.by("createdAt").descending());
        List<Job> recentJobs = jobRepository.findAll(pageable).getContent();

        if (candidate.getSkills() == null || candidate.getSkills().trim().isEmpty()) {
            return recentJobs.stream().limit(10).map(this::mapToDto).collect(Collectors.toList());
        }

        return recentJobs.stream()
                .map(job -> {
                    JobDto dto = mapToDto(job);
                    int match = SmartMatchUtil.calculateMatchPercentage(job.getSkillsRequired(), candidate.getSkills());
                    dto.setMatchPercentage(match);
                    return dto;
                })
                .sorted((j1, j2) -> Integer.compare(j2.getMatchPercentage(), j1.getMatchPercentage()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private Job mapToEntity(JobDto jobDto) {
        Job job = new Job();
        job.setTitle(jobDto.getTitle());
        job.setDescription(jobDto.getDescription());
        job.setSkillsRequired(jobDto.getSkillsRequired());
        job.setSalary(jobDto.getSalary());
        job.setExperienceRequired(jobDto.getExperienceRequired());
        job.setLocation(jobDto.getLocation());
        job.setEmploymentType(EmploymentType.valueOf(jobDto.getEmploymentType().toUpperCase()));
        job.setApplicationDeadline(jobDto.getApplicationDeadline());
        return job;
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
