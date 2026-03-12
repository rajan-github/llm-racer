package com.rajan.llm_racer.services;

import com.rajan.llm_racer.config.ChaosConfig;
import com.rajan.llm_racer.chaos.ChaosLLMProvider;
import com.rajan.llm_racer.config.ApplicationProperties;
import com.rajan.llm_racer.executors.ResilienceExecutor;
import com.rajan.llm_racer.models.GenerateRequest;
import com.rajan.llm_racer.provider.LLMProvider;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service(value = "virtual")
public class StructuredTaskScopeBasedGenerateService implements GenerateService {
    private final LLMProvider llmProviderA, llmProviderB, llmProviderC;
    private static final ScopedValue<String> PROMPT = ScopedValue.newInstance();
    private static final ScopedValue<String> ORG_ID = ScopedValue.newInstance();
    private final ResilienceExecutor resilienceExecutor;

    @Autowired
    public StructuredTaskScopeBasedGenerateService(@Qualifier("providerA") LLMProvider llmProviderA,
                                                   @Qualifier("providerB") LLMProvider llmProviderB,
                                                   @Qualifier("providerC") LLMProvider llmProviderC,
                                                   final ApplicationProperties applicationProperties,
                                                   final ChaosConfig chaosConfig,
                                                   ResilienceExecutor resilienceExecutor) {
        this.resilienceExecutor = resilienceExecutor;
        if (applicationProperties.isCaosEnabled()) {
            this.llmProviderA = new ChaosLLMProvider(llmProviderA, chaosConfig);
            this.llmProviderB = new ChaosLLMProvider(llmProviderB, chaosConfig);
            this.llmProviderC = new ChaosLLMProvider(llmProviderC, chaosConfig);
        } else {
            this.llmProviderC = llmProviderC;
            this.llmProviderA = llmProviderA;
            this.llmProviderB = llmProviderB;
        }
    }

    @Override
    public String generate(GenerateRequest req) {
        log.info("Generate request: {}", req);
        try {
            return ScopedValue.where(PROMPT, req.prompt()).where(ORG_ID, req.orgId()).call(() -> {
                try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
                    scope.fork(() -> resilienceExecutor.execute(() -> llmProviderA.generate(PROMPT.get(), ORG_ID.get()), llmProviderA.getName()));
                    try {
                        scope.joinUntil(Instant.now().plusMillis(400));
                        return scope.result();
                    } catch (TimeoutException eA) {
                        scope.fork(() -> resilienceExecutor.execute(() -> llmProviderB.generate(PROMPT.get(), ORG_ID.get()), llmProviderB.getName()));
                        try {
                            scope.joinUntil(Instant.now().plusMillis(900));
                            return scope.result();
                        } catch (TimeoutException eB) {
                            scope.fork(() -> resilienceExecutor.execute(() -> llmProviderC.generate(PROMPT.get(), ORG_ID.get()), llmProviderC.getName()));
                            scope.joinUntil(Instant.now().plusSeconds(req.timeoutSeconds()));
                            return scope.result();
                        }
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw ex;
                } catch (TimeoutException e) {
                    throw new RuntimeException("Timeout exceeded", e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
