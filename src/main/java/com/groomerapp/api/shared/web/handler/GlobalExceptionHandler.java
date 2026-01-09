package com.groomerapp.api.shared.web.handler;

import com.groomerapp.api.shared.exceptions.BusinessRuleException;
import com.groomerapp.api.shared.exceptions.ErrorCode;
import com.groomerapp.api.shared.exceptions.NotFoundException;
import com.groomerapp.api.shared.web.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ✅ Reglas de negocio (nuestras)
     * - Overlap => 409
     * - Integridad/estado inválido => 409
     * - Validación/inputs => 400
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(BusinessRuleException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (ex.getCode() != null) {
            status = switch (ex.getCode()) {
                case OVERLAP_CONFIRMATION_REQUIRED -> HttpStatus.CONFLICT; // 409
                case DATA_INTEGRITY_VIOLATION -> HttpStatus.CONFLICT;      // 409

                case VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;          // 400
                case APPOINTMENT_TIME_INVALID -> HttpStatus.BAD_REQUEST;
                case APPOINTMENT_REASON_REQUIRED -> HttpStatus.BAD_REQUEST;
                case APPOINTMENT_CANCEL_CHARGE_INVALID -> HttpStatus.BAD_REQUEST;

                default -> HttpStatus.BAD_REQUEST;
            };
        }

        String code = ex.getCode() == null ? ErrorCode.VALIDATION_FAILED.name() : ex.getCode().name();

        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getMessage(), code));
    }

    /**
     * ✅ 404s de dominio
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), ErrorCode.NOT_FOUND.name()));
    }

    /**
     * ✅ Validación Bean Validation (@Valid)
     * Devuelve un mensaje legible (primero encontrado) para MVP.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validación fallida");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(msg, ErrorCode.VALIDATION_FAILED.name()));
    }

    /**
     * ✅ Violaciones DB (unique, FK, etc.)
     * Se devuelve 409 para mantener consistencia.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        "Violación de integridad de datos",
                        ErrorCode.DATA_INTEGRITY_VIOLATION.name()
                ));
    }

    /**
     * ✅ Fallback: cualquier otra excepción no controlada => 500
     * No exponemos detalles internos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "Error inesperado del servidor",
                        ErrorCode.INTERNAL_ERROR.name()
                ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Endpoint no encontrado", ErrorCode.NOT_FOUND.name()));
    }


}
