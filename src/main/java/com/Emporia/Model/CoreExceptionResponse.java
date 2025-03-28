package com.Emporia.Model;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class CoreExceptionResponse implements Serializable {

    private HttpStatus status;
    private HttpStatus externalStatus;
    private String message;
    private List<String> errors;

    public CoreExceptionResponse(HttpStatus status, String message, String error) {
        super();
        this.status = status;
        this.message = message;
        errors = Collections.singletonList(error);
    }

}
