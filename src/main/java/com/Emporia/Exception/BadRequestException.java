package com.Emporia.Exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Data
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@EqualsAndHashCode(callSuper = true)
public class BadRequestException extends RuntimeException {

    private final String localMessage;
    private final String message;
    private final HttpStatus status;

    public BadRequestException(String message , String localMessage) {
        super(message) ;
        this.localMessage = localMessage;
        this.message = message;
        this.status = HttpStatus.BAD_REQUEST;
    }

}
