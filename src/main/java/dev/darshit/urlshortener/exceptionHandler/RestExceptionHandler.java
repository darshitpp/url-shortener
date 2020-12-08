package dev.darshit.urlshortener.exceptionHandler;

import dev.darshit.urlshortener.domain.ShortenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ShortenResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        logger.error("Error occurred: {}", ex.getMessage(), ex.getCause());
        ShortenResponse shortenResponse = new ShortenResponse.Builder()
                .withError(ex.getMessage())
                .build();
        return new ResponseEntity<>(shortenResponse, HttpStatus.BAD_REQUEST);
    }

    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error("Error in input data: {}", ex.getMessage(), ex.getCause());
        ShortenResponse shortenResponse = new ShortenResponse.Builder()
                .withError(ex.getMostSpecificCause().getMessage())
                .build();
        return new ResponseEntity<>(shortenResponse, HttpStatus.BAD_REQUEST);
    }

}