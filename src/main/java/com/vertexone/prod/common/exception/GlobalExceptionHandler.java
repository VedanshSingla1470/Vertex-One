
package com.vertexone.prod.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return createProblem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        return createProblem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException ex) {

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed"
        );

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        problem.setProperty("validationErrors", errors);

        return problem;
    }

    private ProblemDetail createProblem(
            HttpStatus status,
            String message) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                status,
                message
        );

        problem.setType(URI.create("about:blank"));
        problem.setTitle(status.getReasonPhrase());

        return problem;
    }
}
