package com.flash.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Interview Flash API")
                        .description("刷题系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Flash Team")
                                .email("contact@flash.com")));
    }
}
