package com.groomerapp.api.shared.uploads.web;

import com.groomerapp.api.shared.uploads.service.UploadService;
import com.groomerapp.api.shared.uploads.web.dto.UploadResponse;
import com.groomerapp.api.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/uploads")
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(value = "/pets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponse> uploadPetImage(@RequestPart("file") MultipartFile file) {
        var stored = uploadService.savePetImage(file);

        return ApiResponse.ok(
                UploadResponse.builder()
                        .url(stored.url())
                        .fileName(stored.fileName())
                        .build()
        );
    }
}
