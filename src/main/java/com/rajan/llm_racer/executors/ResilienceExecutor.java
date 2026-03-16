package com.rajan.llm_racer.executors;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
                                100,
                                2.0,
                                0.5
                        ))
                .retryExceptions(
                        IOException.class,
                        TimeoutException.class
                )
                .ignoreExceptions(
                        IllegalArgumentException.class
                ).build();
        retryRegistry = RetryRegistry.of(retryConfig);
        bulkheadRegistry = BulkheadRegistry.ofDefaults();
        circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    }

    public <T> T execute(Callable<T> task, String name) throws Exception {
        Retry retry = retryRegistry.retry(name);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead(name);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        Callable<T> decorated = Bulkhead.decorateCallable(bulkhead, Retry.decorateCallable(retry,
                CircuitBreaker.decorateCallable(circuitBreaker, task)));
        return decorated.call();
    }
}
