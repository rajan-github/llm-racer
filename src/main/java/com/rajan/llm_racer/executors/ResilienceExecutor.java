package com.rajan.llm_racer.executors;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

@Component
public class ResilienceExecutor {
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ResilienceExecutor() {
        RetryConfig retryConfig = RetryConfig.custom().maxAttempts(3).intervalFunction(
                        IntervalFunction.ofExponentialRandomBackoff(
                                50,
                                1.5,
                                0.3
                        ))
                .retryExceptions(
                        IOException.class,
                        TimeoutException.class
                )
                .ignoreExceptions(
                        IllegalArgumentException.class
                ).build();
        retryRegistry = RetryRegistry.of(retryConfig);
        var bulkheadConfig = BulkheadConfig.custom().maxConcurrentCalls(5000).maxWaitDuration(Duration.ofMillis(50)).build();
        bulkheadRegistry = BulkheadRegistry.custom().withBulkheadConfig(bulkheadConfig).build();
        var circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(80)
                .slowCallRateThreshold(90)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(10)
                .minimumNumberOfCalls(20)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(30)
                .recordExceptions(IOException.class, TimeoutException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    public <T> T execute(Callable<T> task, String name) throws Exception {
        Retry retry = retryRegistry.retry(name);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead(name);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        Callable<T> decorated = Bulkhead.decorateCallable(bulkhead,
                CircuitBreaker.decorateCallable(circuitBreaker, Retry.decorateCallable(retry, task)));
        return decorated.call();
    }
}
