package nl.templify.iceinsights.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ChipNotLinkedException extends RuntimeException {
    public ChipNotLinkedException(String message) {
        super(message);
    }
}