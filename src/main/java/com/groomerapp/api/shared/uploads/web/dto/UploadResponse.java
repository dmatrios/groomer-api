package com.groomerapp.api.shared.uploads.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadResponse {
    private final String url;
    private final String fileName;
}
