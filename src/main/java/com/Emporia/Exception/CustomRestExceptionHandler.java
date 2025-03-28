package com.Emporia.Exception;

import com.Emporia.Model.CoreExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class CustomRestExceptionHandler {

    @ExceptionHandler({BadRequestException.class})
    public ResponseEntity<CoreExceptionResponse> handleBadRequestException(
            BadRequestException ex, HttpServletRequest request) {
        logException(ex, request);
        CoreExceptionResponse coreExceptionResponse =
                new CoreExceptionResponse(ex.getStatus(), ex.getMessage(), ex.getLocalMessage());
        return new ResponseEntity<>(coreExceptionResponse, coreExceptionResponse.getStatus());
    }

    private void logException(Throwable exception, HttpServletRequest request) {
        log.error("BadRequestException ", exception);
        log.error("failed for request {} {}", request.getMethod(), request.getRequestURI());
    }
}