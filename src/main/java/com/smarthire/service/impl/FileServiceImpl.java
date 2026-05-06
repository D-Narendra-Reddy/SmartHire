package com.smarthire.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smarthire.dto.ResumeDto;
import com.smarthire.entity.Resume;
import com.smarthire.entity.User;
import com.smarthire.exception.FileUploadException;
import com.smarthire.exception.ResourceNotFoundException;
import com.smarthire.repository.ResumeRepository;
import com.smarthire.repository.UserRepository;
import com.smarthire.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {

    private final Cloudinary cloudinary;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    @Autowired
    public FileServiceImpl(Cloudinary cloudinary, ResumeRepository resumeRepository, UserRepository userRepository) {
        this.cloudinary = cloudinary;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ResumeDto uploadResume(MultipartFile file, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        try {
            // Check if user already has a resume
            Resume resume = resumeRepository.findByCandidateId(userId).orElse(new Resume());

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                        "resource_type", "raw",
                        "use_filename", true,
                        "unique_filename", true
                    ));
            String fileUrl = uploadResult.get("secure_url").toString();

            resume.setFileUrl(fileUrl);
            resume.setFileName(file.getOriginalFilename());
            resume.setCandidate(user);

            Resume savedResume = resumeRepository.save(resume);

            ResumeDto dto = new ResumeDto();
            dto.setId(savedResume.getId());
            dto.setFileUrl(savedResume.getFileUrl());
            dto.setFileName(savedResume.getFileName());
            dto.setUploadedAt(savedResume.getUploadedAt());

            return dto;

        } catch (IOException e) {
            throw new FileUploadException("Could not upload file: " + e.getMessage());
        }
    }
}
