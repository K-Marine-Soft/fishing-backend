package com.kmarine.fishing.file;

import com.kmarine.fishing.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// 공용 첨부파일 업로드 — 로그인한 사용자면 어디서든(선박/선단 이미지 등) 사용 가능
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileResponseDto>> upload(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                ApiResponse.ok(fileStorageService.store(file)));
    }
}
