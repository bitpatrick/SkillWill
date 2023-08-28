package com.sinnerschrader.skillwill.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI customOpenAPI() {
    	
        return new OpenAPI()
                .info(new Info()
                        .title("API del mio servizio")
                        .version("1.0")
                        .description("Descrizione dell'API del mio servizio Spring Boot")
                        .contact(new Contact().name("Nome Autore").email("email@dominio.it")));
       
    }
}

