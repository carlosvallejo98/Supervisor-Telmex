package com.supervisor.supervisor_backend.config;

import com.supervisor.supervisor_backend.security.SupervisorAuthFilter;
import com.supervisor.supervisor_backend.security.TokenRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig {

    // Origen único (back-compat)
    @Value("${cors.origin:http://localhost:3002}")
    private String corsOrigin;

    // Orígenes múltiples (CSV, opcional)
    @Value("${cors.origins:}")
    private String corsOriginsCsv;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        // Construye la lista de orígenes válidos
        String[] origins = (corsOriginsCsv != null && !corsOriginsCsv.isBlank())
                ? Arrays.stream(corsOriginsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new)
                : new String[]{ corsOrigin };

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(origins)        // <- cada origen por separado, NO en una sola cadena
                        .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    public FilterRegistrationBean<SupervisorAuthFilter> supervisorAuthFilter(TokenRegistry tokenRegistry) {
        FilterRegistrationBean<SupervisorAuthFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new SupervisorAuthFilter(tokenRegistry));
        reg.addUrlPatterns("/*");
        reg.setOrder(1);
        return reg;
    }
}
