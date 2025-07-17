package com.chitchat.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse allowed origins from comma-separated string
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        
        // Parse allowed methods from comma-separated string
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);
        
        // Set allowed headers
        if ("*".equals(allowedHeaders)) {
            configuration.setAllowedHeaders(Arrays.asList("*"));
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }
        
        // Set allow credentials
        configuration.setAllowCredentials(allowCredentials);
        
        // Allow specific headers that are commonly used
        configuration.addAllowedHeader("Authorization");
        configuration.addAllowedHeader("Content-Type");
        configuration.addAllowedHeader("Accept");
        configuration.addAllowedHeader("X-Requested-With");
        configuration.addAllowedHeader("Cache-Control");
        
        // Expose headers that might be needed by the frontend
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("Content-Type");
        
        // Apply this configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
} 