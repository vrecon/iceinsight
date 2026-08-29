package nl.templify.iceinsights.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SeasonNotFoundException extends RuntimeException {
    public SeasonNotFoundException(String message) {
        super(message);
    }
}
