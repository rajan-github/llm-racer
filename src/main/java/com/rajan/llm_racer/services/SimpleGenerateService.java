package com.rajan.llm_racer.services;

import com.rajan.llm_racer.models.GenerateRequest;
import com.rajan.llm_racer.provider.LLMProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;

@Service
public class SimpleGenerateService implements GenerateService {
    private final LLMProvider llmProviderA, llmProviderB, llmProviderC;
    private static final ScopedValue<String> PROMPT = ScopedValue.newInstance();
    private static final ScopedValue<String> ORG_ID = ScopedValue.newInstance();

    @Autowired
    public SimpleGenerateService(@Qualifier("providerA") LLMProvider llmProviderA, @Qualifier("providerB") LLMProvider llmProviderB, @Qualifier("providerC") LLMProvider llmProviderC) {
        this.llmProviderA = llmProviderA;
        this.llmProviderB = llmProviderB;
        this.llmProviderC = llmProviderC;
    }

    @Override
    public String generate(GenerateRequest req) {
        try {
            return ScopedValue.where(PROMPT, req.prompt()).where(ORG_ID, req.orgId()).call(() -> {
                try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
                    scope.fork(() -> llmProviderA.generate(PROMPT.get(), ORG_ID.get()));
                    scope.fork(() -> llmProviderB.generate(PROMPT.get(), ORG_ID.get()));
                    scope.fork(() -> llmProviderC.generate(PROMPT.get(), ORG_ID.get()));
                    var deadline = Instant.now().plusSeconds(req.timeoutSeconds());
                    scope.joinUntil(deadline);
                    return scope.result();
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                } catch (TimeoutException e) {
                    throw new RuntimeException("Timeout exceeded", e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
