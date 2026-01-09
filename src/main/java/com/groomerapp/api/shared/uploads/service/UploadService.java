package com.groomerapp.api.shared.uploads.service;

import com.groomerapp.api.shared.exceptions.BusinessRuleException;
import com.groomerapp.api.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png");

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Transactional
    public StoredFile savePetImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException(ErrorCode.UPLOAD_FILE_REQUIRED, "El archivo es obligatorio");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType)) {
            throw new BusinessRuleException(ErrorCode.UPLOAD_INVALID_TYPE, "Solo se permiten imágenes JPG o PNG");
        }

        String ext = contentType.equals("image/png") ? ".png" : ".jpg";
        String safeName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path dir = Path.of(uploadDir, "pets").toAbsolutePath().normalize();
        Path target = dir.resolve(safeName);

        try {
            Files.createDirectories(dir);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new BusinessRuleException(ErrorCode.UPLOAD_FAILED, "No se pudo guardar el archivo");
        }

        String url = "/uploads/pets/" + safeName;
        return new StoredFile(url, safeName);
    }

    public record StoredFile(String url, String fileName) {}
}
