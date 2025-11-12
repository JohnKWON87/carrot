package com.carrot.controller;

import com.carrot.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/api/file")
public class FileUploadController {

    @Value("${file.upload.dir:uploads/images}")
    private String uploadDir;

    /**
     * 이미지 업로드 API
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 로그인 체크
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 파일 유효성 검사
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일을 선택해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 파일 크기 체크 (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "파일 크기가 10MB를 초과할 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미지 파일 형식 체크
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 허용된 확장자 체크
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                response.put("success", false);
                response.put("message", "잘못된 파일입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!isAllowedExtension(extension)) {
                response.put("success", false);
                response.put("message", "지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif만 가능)");
                return ResponseEntity.badRequest().body(response);
            }

            // 업로드 디렉토리 생성
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            // 고유한 파일명 생성
            String uniqueFilename = generateUniqueFilename(originalFilename);
            Path filePath = Paths.get(uploadDir, uniqueFilename);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 웹 접근 가능한 URL 생성
            String fileUrl = "/images/" + uniqueFilename;

            response.put("success", true);
            response.put("message", "파일이 성공적으로 업로드되었습니다.");
            response.put("filename", uniqueFilename);
            response.put("url", fileUrl);
            response.put("originalName", originalFilename);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 파일 삭제 API
     */
    @DeleteMapping("/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteFile(
            @RequestParam("filename") String filename,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 로그인 체크
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 파일 경로 생성
            Path filePath = Paths.get(uploadDir, filename);
            File file = filePath.toFile();

            if (file.exists()) {
                if (file.delete()) {
                    response.put("success", true);
                    response.put("message", "파일이 성공적으로 삭제되었습니다.");
                } else {
                    response.put("success", false);
                    response.put("message", "파일 삭제에 실패했습니다.");
                }
            } else {
                response.put("success", false);
                response.put("message", "파일을 찾을 수 없습니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 허용된 확장자인지 체크
     */
    private boolean isAllowedExtension(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("png") || extension.equals("gif");
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + extension;
    }
}