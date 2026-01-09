package com.groomerapp.api.shared.exceptions;

public class OverlapConfirmationRequiredException extends BusinessRuleException {
    public OverlapConfirmationRequiredException(String message) {
        super(ErrorCode.OVERLAP_CONFIRMATION_REQUIRED, message);
    }
}