package com.rajan.llm_racer.chaos;

import com.rajan.llm_racer.provider.LLMProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

public class ChaosLLMProvider implements LLMProvider {
    private final LLMProvider delegate;
    private final ChaosConfig chaosConfig;

    public ChaosLLMProvider(final LLMProvider delegate, final ChaosConfig chaosConfig) {
        this.delegate = delegate;
        this.chaosConfig = chaosConfig;
    }


    @Override
    public String generate(String prompt, String orgId) throws TimeoutException, InterruptedException {
        double r = ThreadLocalRandom.current().nextDouble();
        if (r < chaosConfig.getTimeoutRate()) {
            Thread.sleep(chaosConfig.getLatencyMs());
            throw new TimeoutException("LLMProvider timeout!");
        } else if (r < chaosConfig.getFailureRate()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable");
        } else if (r < chaosConfig.getLatencySpikeRate()) {
            Thread.sleep(chaosConfig.getLatencyMs());
        }
        return delegate.generate(prompt, orgId);
    }
}
