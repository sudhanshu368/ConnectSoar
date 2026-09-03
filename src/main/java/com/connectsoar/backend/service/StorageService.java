package com.connectsoar.backend.service;

import com.connectsoar.backend.dto.AvatarUploadRequest;
import com.connectsoar.backend.dto.AvatarUploadResponse;
import com.connectsoar.backend.enums.ErrorCode;
import com.connectsoar.backend.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    @Value("${supabase.url:https://example.supabase.co}")
    private String supabaseUrl;

    public AvatarUploadResponse generateAvatarUploadUrl(String userId, AvatarUploadRequest request) {
        if (!ALLOWED_MIME_TYPES.contains(request.getContentType().toLowerCase())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, 
                    "Invalid file type. Only JPEG, PNG, and WebP images are allowed.", HttpStatus.BAD_REQUEST);
        }

        if (request.getFileSizeBytes() > MAX_FILE_SIZE_BYTES) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, 
                    "File size exceeds 5MB limit.", HttpStatus.BAD_REQUEST);
        }

        String extension = getFileExtension(request.getFileName());
        if (extension.isEmpty() || !Arrays.asList("jpg", "jpeg", "png", "webp").contains(extension.toLowerCase())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Invalid file extension.", HttpStatus.BAD_REQUEST);
        }

        String fileId = UUID.randomUUID().toString().substring(0, 8);
        String storagePath = "profile-images/" + userId + "/avatar_" + fileId + "." + extension;
        String uploadUrl = supabaseUrl + "/storage/v1/object/profile-images/" + storagePath;
        String signedUrl = supabaseUrl + "/storage/v1/object/public/profile-images/" + storagePath;

        return AvatarUploadResponse.builder()
                .path(storagePath)
                .uploadUrl(uploadUrl)
                .signedUrl(signedUrl)
                .expiresIn(3600)
                .build();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
