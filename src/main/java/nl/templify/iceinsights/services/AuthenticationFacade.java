package nl.templify.iceinsights.services;


import nl.templify.iceinsights.exceptions.NotAuthenticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {
    
    public String getCurrentUsername()  {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotAuthenticatedException("User not authenticated");
        }
        return authentication.getName();
    }
}