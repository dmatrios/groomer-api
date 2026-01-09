package com.groomerapp.api.shared.exceptions;

public class BusinessRuleException extends ApiException {
    public BusinessRuleException(ErrorCode code, String message) {
        super(code, message);
    }
}