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

        // Use allowedOriginPatterns instead of allowedOrigins("*")
        config.setAllowedOriginPatterns(List.of("*"));

        // Allow common HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow common headers
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
