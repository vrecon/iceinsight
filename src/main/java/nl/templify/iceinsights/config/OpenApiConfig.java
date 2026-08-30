package nl.templify.iceinsights.config;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.InputStream;

/**
 * API-first OpenAPI: serve the contract YAML and load it for /api-docs.
 * Single source: classpath:openapi/openapi.yaml
 */
@Configuration
public class OpenApiConfig implements WebMvcConfigurer {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openAPI() throws IOException {
        try (InputStream in = new ClassPathResource("openapi/openapi.yaml").getInputStream()) {
            return Yaml.mapper().readValue(in, OpenAPI.class);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/openapi.yaml")
                .addResourceLocations("classpath:/openapi/");
    }
}
