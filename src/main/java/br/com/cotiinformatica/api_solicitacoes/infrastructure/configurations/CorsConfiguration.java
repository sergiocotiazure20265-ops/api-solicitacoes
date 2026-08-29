package br.com.cotiinformatica.api_solicitacoes.infrastructure.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CorsConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry
                .addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("POST", "PUT", "DELETE", "GET", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }
}