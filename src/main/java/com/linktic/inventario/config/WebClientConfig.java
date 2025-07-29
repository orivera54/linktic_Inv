package com.linktic.inventario.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Configuración de WebClient para comunicación con servicios externos
 */
@Configuration
public class WebClientConfig {

    @Value("${app.productos-service.timeout:5000}")
    private int timeout;

    @Value("${app.productos-service.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.productos-service.retry.backoff.initial-interval:1000}")
    private long initialInterval;

    @Value("${app.productos-service.retry.backoff.multiplier:2.0}")
    private double multiplier;

    @Value("${app.productos-service.retry.backoff.max-interval:10000}")
    private long maxInterval;

    /**
     * Configuración de WebClient
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)); // 1MB
    }

    /**
     * Configuración del Circuit Breaker
     */
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
    }

    /**
     * Configuración de Retry
     */
    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.ofMillis(initialInterval))
                .retryExceptions(Exception.class)
                .build();
    }
}