package nl.templify.iceinsights.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {


    public static final String BEARER_AUTH = "bearerAuth";
    private static final String TITLE = "API documentation  M3E Bouwkosten";
    private static final String VERSION = "1.0";

    @Bean
    public OpenAPI openAPI() {
        Contact contact = new Contact();
        contact.setEmail("barry@templify.nl");
        contact.setName("Templify BV");

        io.swagger.v3.oas.models.info.Info info = new Info()
                .title("OpenApi documentation")
                .version(VERSION)
                .contact(contact);


        io.swagger.v3.oas.models.security.SecurityScheme bearerAuth = new io.swagger.v3.oas.models.security.SecurityScheme();
        bearerAuth.setType(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP);
        bearerAuth.setScheme("bearer");
        bearerAuth.setBearerFormat("JWT");

        io.swagger.v3.oas.models.security.SecurityRequirement securityItem = new io.swagger.v3.oas.models.security.SecurityRequirement();
        securityItem.addList(BEARER_AUTH);

        return new OpenAPI().info(info).addSecurityItem(securityItem)
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, bearerAuth));
    }

}