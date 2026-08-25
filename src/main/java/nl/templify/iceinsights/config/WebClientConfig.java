package nl.templify.iceinsights.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .baseUrl("https://practice-api.speedhive.com")
            .defaultHeaders(headers -> {
                headers.set("Host", "practice-api.speedhive.com");
                headers.set("Origin", "https://sporthive.com");
                headers.set("Cookie", "ai_session=lx8/dg63CnVGw8aE6WGpaw|1729847176437|1729848795986; " +
                          "ai_user=8XSUDiJIoix3QSuWx5gS3T|2024-10-25T09:06:15.656Z");
            })
            .build();
    }
}