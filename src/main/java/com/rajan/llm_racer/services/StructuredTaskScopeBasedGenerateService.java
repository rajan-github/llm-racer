package com.rajan.llm_racer.services;

import com.rajan.llm_racer.config.ChaosConfig;
import com.rajan.llm_racer.chaos.ChaosLLMProvider;
import com.rajan.llm_racer.config.ApplicationProperties;
import com.rajan.llm_racer.executors.ResilienceExecutor;
import com.rajan.llm_racer.models.GenerateRequest;
import com.rajan.llm_racer.provider.LLMProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

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

                    scope.fork(() -> {
                        Thread.sleep(Duration.ofMillis(400));
                        return resilienceExecutor.execute(() -> llmProviderB.generate(PROMPT.get(), ORG_ID.get()), llmProviderB.getName());
                    });

                    scope.fork(() -> {
                        Thread.sleep(Duration.ofMillis(900));
                        return resilienceExecutor.execute(() -> llmProviderC.generate(PROMPT.get(), ORG_ID.get()), llmProviderC.getName());
                    });
                    var deadline = Instant.now().plusSeconds(req.timeoutSeconds());
                    scope.joinUntil(deadline);
                    return scope.result();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
