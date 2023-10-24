package dk.lundogbendsen.springbootcourse.urlshortener.controller;

import dk.lundogbendsen.springbootcourse.urlshortener.service.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ControllerAdvicerServiceLayer {

    @ExceptionHandler({TokenAlreadyExistsException.class, UserExistsException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflict(Exception exception) {
        if (exception instanceof TokenAlreadyExistsException) {
            return Map.of("message", "The token already exists");
        } else {
            return Map.of("message", "The user already exists");
        }
    }

    @ExceptionHandler({TokenNotFoundException.class, UserNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(TokenNotFoundException exception) {
        if (exception instanceof TokenNotFoundException) {
            return Map.of("message", "The token not found");
        } else {
            return Map.of("message", "The user not found");
        }
    }

    @ExceptionHandler({IllegalTargetUrlException.class, IllegalTokenNameException.class, InvalidTargetUrlException.class, TokenTargetUrlIsNullException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleValidation(Exception exception) {
        return Map.of("message", "The token did not validate", "validation-type", exception.getClass().getSimpleName());
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleSecurity(AccessDeniedException exception, HttpServletRequest request) {
        return Map.of("message", "The operation is not allowed", "path", request.getRequestURI());
    }
}
