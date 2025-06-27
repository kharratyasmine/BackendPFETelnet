package com.workpilot.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getFieldErrors().forEach(error ->
//                errors.put(error.getField(), error.getDefaultMessage()));
//        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
//    }

//    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
//    public ResponseEntity<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
//        String path = request.getRequestURI();
//        if (path.startsWith("/api/")) {
//            return new ResponseEntity<>("Erreur : " + ex.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
//        }
//        // ✅ Laisser passer les autres GET
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }


//    @ExceptionHandler(NoResourceFoundException.class)
//    public ResponseEntity<String> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
//        if (request.getRequestURI().matches(".*\\.(js|css|html|ico)$")) {
//            return new ResponseEntity<>("Fichier non trouvé : " + request.getRequestURI(), HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<>("Ressource non trouvée", HttpStatus.NOT_FOUND);
//    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleGenericException(Exception ex, HttpServletRequest request) {
//        ex.printStackTrace();
//        String path = request.getRequestURI();
//        if (path.equals("/") || path.equals("/index.html") ||
//                path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".html") ||
//                path.startsWith("/assets") || path.startsWith("/static") || path.equals("/favicon.ico")) {
//            return null;
//        }
//        return new ResponseEntity<>("Erreur interne du serveur", HttpStatus.INTERNAL_SERVER_ERROR);
//    }
}
