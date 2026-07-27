package com.kmarine.fishing.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

// 공용 첨부파일 저장소 — 앱 어디서든(선박 이미지, 선단 이미지, 향후 다른 첨부) 재사용
// 지금은 로컬 디스크에 저장. 추후 S3 등으로 교체해도 이 클래스의 계약(저장→URL 반환)만 유지하면 됨
@Slf4j
@Service
public class FileStorageService {

    // 인라인으로 보여줄 수 있는 이미지 확장자 (svg는 인라인 스크립트 실행 위험이 있어 제외)
    public static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp");

    // 다운로드만 허용하는 일반 문서 확장자 (html/js 등 실행 가능한 확장자는 제외)
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "hwp", "zip", "txt", "csv");

    private static final Set<String> ALLOWED_EXTENSIONS;
    static {
        ALLOWED_EXTENSIONS = new java.util.HashSet<>(IMAGE_EXTENSIONS);
        ALLOWED_EXTENSIONS.addAll(DOCUMENT_EXTENSIONS);
    }

    // 저장 파일명에 담을 원본 파일명 중 안전하게 허용할 문자만 남김 (경로 조작/특수문자 방지)
    private static final Pattern UNSAFE_NAME_CHARS =
            Pattern.compile("[^a-zA-Z0-9가-힣._\\- ]");

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080}")
    private String baseUrl;

    public FileResponseDto store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = getExtension(originalName).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException(
                "허용되지 않는 파일 형식입니다 (이미지: jpg/png/gif/webp, 문서: pdf/doc/xls/ppt/hwp/zip/txt/csv 등).");
        }

        boolean isImage = IMAGE_EXTENSIONS.contains(ext);

        // 저장 파일명 = UUID + 원본명(안전문자만) — 다운로드 시 원본 파일명을 그대로 보여주기 위함
        String safeOriginalName = UNSAFE_NAME_CHARS.matcher(originalName)
                .replaceAll("_");
        if (safeOriginalName.length() > 100) {
            safeOriginalName = safeOriginalName.substring(safeOriginalName.length() - 100);
        }
        String savedName = UUID.randomUUID() + "__" + safeOriginalName;

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            Path target = dir.resolve(savedName).normalize();
            if (!target.getParent().equals(dir)) {
                throw new IllegalArgumentException("잘못된 파일 경로입니다.");
            }

            file.transferTo(target);
        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new IllegalStateException("파일 저장에 실패했습니다.");
        }

        String url = baseUrl + "/uploads/" + savedName;
        log.info("파일 업로드 완료: {}", url);

        return FileResponseDto.builder()
                .url(url)
                .originalName(originalName)
                .size(file.getSize())
                .isImage(isImage)
                .build();
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다.");
        }
        return filename.substring(dot + 1);
    }
}
