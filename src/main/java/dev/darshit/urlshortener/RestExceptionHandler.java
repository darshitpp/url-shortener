package dev.darshit.urlshortener;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ShortenResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        ShortenResponse shortenResponse = new ShortenResponse.Builder()
                .withError(ex.getMessage())
                .build();
        return new ResponseEntity<>(shortenResponse, HttpStatus.BAD_REQUEST);
    }

}