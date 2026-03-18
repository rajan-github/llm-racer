package com.rajan.llm_racer.services;

import com.rajan.llm_racer.chaos.ChaosLLMProvider;
import com.rajan.llm_racer.config.ApplicationProperties;
import com.rajan.llm_racer.config.ChaosConfig;
import com.rajan.llm_racer.executors.ResilienceExecutor;
import com.rajan.llm_racer.models.GenerateRequest;
import com.rajan.llm_racer.provider.LLMProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;


@Slf4j
@Service(value = "platform")
public class PlatformThreadBasedGenerateService implements GenerateService {
    private final LLMProvider llmProviderA, llmProviderB, llmProviderC;
    private final ScheduledExecutorService scheduler;
    private final ResilienceExecutor resilienceExecutor;

    public PlatformThreadBasedGenerateService(@Qualifier("providerA") LLMProvider llmProviderA,
                                              @Qualifier("providerB") LLMProvider llmProviderB,
                                              @Qualifier("providerC") LLMProvider llmProviderC,
                                              final ApplicationProperties applicationProperties,
                                              final ChaosConfig chaosConfig, ResilienceExecutor resilienceExecutor) {
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
        this.scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdownNow));
    }


    @Override
    public String generate(GenerateRequest req) {
        log.info("Generating using platform threads for request {}", req);
        Callable<String> taskA = () -> llmProviderA.generate(req.prompt(), req.orgId());
        Callable<String> taskB = () -> llmProviderB.generate(req.prompt(), req.orgId());
        Callable<String> taskC = () -> llmProviderC.generate(req.prompt(), req.orgId());

        CompletableFuture<String> result = new CompletableFuture<>();

        List<Future<?>> futures = new CopyOnWriteArrayList<>();
        Future<?> a = this.scheduler.submit(() -> {
            try {
                tryComplete(result, resilienceExecutor.execute(taskA, llmProviderA.getName()), futures);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        futures.add(a);
        scheduler.schedule(() -> {
            if (!result.isDone()) {
                Future<?> b = scheduler.submit(() -> {
                    try {
                        tryComplete(result, resilienceExecutor.execute(taskB, llmProviderB.getName()), futures);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                futures.add(b);
            }
        }, 100, TimeUnit.MILLISECONDS);


        scheduler.schedule(() -> {
            if (!result.isDone()) {
                Future<?> c = scheduler.submit(() -> {
                    try {
                        tryComplete(result, resilienceExecutor.execute(taskC, llmProviderC.getName()), futures);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                futures.add(c);
            }
        }, 250, TimeUnit.MILLISECONDS);
        try {
            return result.get(req.timeoutSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void tryComplete(CompletableFuture<String> result, String value, List<Future<?>> tasks) {
        if (result.complete(value)) {
            for (var task : tasks) {
                task.cancel(true);
            }
        }
    }
}
