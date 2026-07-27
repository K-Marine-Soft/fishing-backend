package com.kmarine.fishing.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URLEncoder;

// 업로드 파일 서빙 — 정적 리소스 매핑 대신 컨트롤러를 통해 응답 헤더를 직접 제어함
// (이미지는 인라인으로 보여주고, 그 외 파일은 원본 파일명으로 강제 다운로드시켜
//  업로드된 파일이 브라우저에서 그대로 실행/렌더링되는 것을 방지)
@Slf4j
@RestController
public class FileServeController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serve(@PathVariable("filename") String filename) {
        return respond(filename, false);
    }

    // 이미지를 포함해 항상 다운로드로 강제하고 싶을 때 사용하는 엔드포인트
    @GetMapping("/api/files/download/{filename:.+}")
    public ResponseEntity<Resource> download(@PathVariable("filename") String filename) {
        return respond(filename, true);
    }

    private ResponseEntity<Resource> respond(String filename, boolean forceDownload) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = dir.resolve(filename).normalize();
        if (!target.getParent().equals(dir) || !Files.exists(target)) {
            return ResponseEntity.notFound().build();
        }

        String ext = getExtension(filename).toLowerCase();
        boolean isImage = FileStorageService.IMAGE_EXTENSIONS.contains(ext);
        String originalName = extractOriginalName(filename);

        HttpHeaders headers = new HttpHeaders();
        String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        if (isImage && !forceDownload) {
            headers.setContentType(MediaType.parseMediaType(
                    "image/" + (ext.equals("jpg") ? "jpeg" : ext)));
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename*=UTF-8''" + encodedName);
            headers.setCacheControl("public, max-age=86400");
        } else {
            // 일반 파일(또는 강제 다운로드 요청)은 항상 첨부파일로 취급 —
            // 브라우저가 내용을 추측해 실행하지 못하도록 sniffing도 차단
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encodedName);
            headers.add("X-Content-Type-Options", "nosniff");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(new FileSystemResource(target));
    }

    // 저장 파일명(UUID__원본명) 에서 원본 파일명만 추출. 구형(UUID.ext) 파일은 그대로 사용
    private String extractOriginalName(String savedName) {
        int idx = savedName.indexOf("__");
        return idx >= 0 ? savedName.substring(idx + 2) : savedName;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1);
    }
}
