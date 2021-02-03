package nl.rcomanne.gameservice.shared;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler(Exception.class)
    public void handleException(final Exception ex) {
        log.error("Exception: {}", ex.getMessage());
        ex.printStackTrace();
    }

    @ExceptionHandler(NoSuchElementException.class)
    private ResponseEntity<Map<String, String>> handleNoSuchElementException(final NoSuchElementException ex) {
        log.error("NoSuchElementException: " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}