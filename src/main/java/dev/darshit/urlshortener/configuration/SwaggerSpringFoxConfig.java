package dev.darshit.urlshortener.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;

@Configuration
public class SwaggerSpringFoxConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("dev.darshit.urlshortener.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "URL Shortener",
                "Various implementations of URL shortening strategies",
                "API TOS",
                "Terms of service",
                new Contact("Darshit Patel", "https://darshit.dev", "darshit12@gmail.com"),
                "Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0.txt", Collections.emptyList());
    }
}