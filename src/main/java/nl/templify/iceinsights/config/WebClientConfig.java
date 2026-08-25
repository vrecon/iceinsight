package nl.templify.iceinsights.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final int MAX_IN_MEMORY_SIZE = 16 * 1024 * 1024;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://practice-api.speedhive.com")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .defaultHeaders(headers -> {
                    headers.set("Origin", "https://sporthive.com");
                    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.set(HttpHeaders.REFERER, "https://sporthive.com/");
                })
                .build();
    }
}
