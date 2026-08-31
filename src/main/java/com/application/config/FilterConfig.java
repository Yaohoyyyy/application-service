package com.application.config;

import com.application.service.AuthService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    private final AuthService authService;

    public FilterConfig(AuthService authService) {
        this.authService = authService;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilter() {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthFilter(authService));
        registration.addUrlPatterns("/api/users", "/api/users/*");
        registration.setName("jwtAuthFilter");
        registration.setOrder(1);
        return registration;
    }
}
