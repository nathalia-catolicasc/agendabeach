package com.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/** Converte as exceções da API em respostas ProblemDetail com mensagens em português. */
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalid(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail conflict(IllegalStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integrity(DataIntegrityViolationException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Dados em conflito: horário já reservado ou e-mail, CPF ou quadra já cadastrados");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException e) {
        String fields =
                e.getBindingResult().getFieldErrors().stream()
                        .map(ApiExceptionHandler::describe)
                        .sorted()
                        .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                fields.isEmpty() ? "Dados inválidos" : "Dados inválidos: " + fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail unreadable(HttpMessageNotReadableException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou mal formatado");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail typeMismatch(MethodArgumentTypeMismatchException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + e.getName() + "'");
    }

    private static String describe(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }
}
