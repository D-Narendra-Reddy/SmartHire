package com.smarthire.service;

import org.springframework.web.multipart.MultipartFile;
import com.smarthire.dto.ResumeDto;

public interface FileService {
    ResumeDto uploadResume(MultipartFile file, Long userId);
}
