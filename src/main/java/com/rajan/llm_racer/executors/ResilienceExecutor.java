package com.rajan.llm_racer.executors;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
public class ResilienceExecutor {
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public <T> ResilienceExecutor() {
        retryRegistry = RetryRegistry.ofDefaults();
        bulkheadRegistry = BulkheadRegistry.ofDefaults();
        circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    }

    public <T> T execute(Callable<T> task, String name) throws Exception {
        Retry retry = retryRegistry.retry(name);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead(name);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        Callable<T> decorated = Retry.decorateCallable(retry, Bulkhead.decorateCallable(bulkhead, CircuitBreaker.decorateCallable(circuitBreaker, task)));
        return decorated.call();
    }
}
