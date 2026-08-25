package nl.templify.iceinsights.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Custom Exceptions
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ChipNotFoundException extends RuntimeException {
    public ChipNotFoundException(String message) {
        super(message);
    }
}
