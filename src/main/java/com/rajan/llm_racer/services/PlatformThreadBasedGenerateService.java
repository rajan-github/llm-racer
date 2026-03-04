package com.rajan.llm_racer.services;

import com.rajan.llm_racer.models.GenerateRequest;
import com.rajan.llm_racer.provider.LLMProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Service
public class PlatformThreadBasedGenerateService implements GenerateService {
    private final LLMProvider llmProviderA, llmProviderB, llmProviderC;
    private final ExecutorService executors;

    public PlatformThreadBasedGenerateService(@Qualifier("providerA") LLMProvider llmProviderA, @Qualifier("providerB") LLMProvider llmProviderB, @Qualifier("providerC") LLMProvider llmProviderC) {
        this.llmProviderA = llmProviderA;
        this.llmProviderB = llmProviderB;
        this.llmProviderC = llmProviderC;
        this.executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }


    @Override
    public String generate(GenerateRequest req) {
        log.info("Generating using platform threads for request {}", req);
        final List<Callable<String>> tasks = List.of(() -> llmProviderA.generate(req.prompt(), req.orgId())
                ,
                () -> llmProviderB.generate(req.prompt(), req.orgId()),
                () -> llmProviderC.generate(req.prompt(), req.orgId())
        );
        try {
            return executors.invokeAny(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
