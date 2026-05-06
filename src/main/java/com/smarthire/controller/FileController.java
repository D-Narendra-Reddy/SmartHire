package com.smarthire.controller;

import com.smarthire.dto.ApiResponse;
import com.smarthire.dto.ResumeDto;
import com.smarthire.entity.User;
import com.smarthire.repository.UserRepository;
import com.smarthire.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final UserRepository userRepository;

    @Autowired
    public FileController(FileService fileService, UserRepository userRepository) {
        this.fileService = fileService;
        this.userRepository = userRepository;
    }

    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<ResumeDto>> uploadResume(@RequestParam("file") MultipartFile file, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        ResumeDto resumeDto = fileService.uploadResume(file, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Resume uploaded successfully", resumeDto));
    }

    /**
     * Public proxy endpoint: fetches a file from Cloudinary and serves it
     * with Content-Disposition: inline so the browser displays it instead of downloading.
     */
    @GetMapping("/preview")
    public void previewFile(@RequestParam String url, HttpServletResponse response) throws IOException {
        // Basic validation — only allow Cloudinary URLs
        if (url == null || !url.startsWith("https://res.cloudinary.com/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL");
            return;
        }

        URL fileUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"resume.pdf\"");

        try (InputStream inputStream = connection.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
        } finally {
            connection.disconnect();
        }
    }
}
