package com.example.leaderboard_ms_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers, etc.)
        config.setAllowCredentials(true);

        // Allow requests from all origins (or replace "*" with specific patterns for more control)
        config.setAllowedOriginPatterns(List.of("*")); // You can replace "*" with your frontend domain like "http://localhost:3000"

        // Allow common HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow common headers
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
