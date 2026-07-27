package com.kmarine.fishing.file;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileResponseDto {
    private String url;
    private String originalName;
    private long   size;
    private boolean isImage;
}
