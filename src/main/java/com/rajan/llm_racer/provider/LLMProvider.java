package com.rajan.llm_racer.provider;

import java.util.concurrent.TimeoutException;

public interface LLMProvider {
    String generate(String prompt, String orgId) throws TimeoutException, InterruptedException;
    String getName();
}
