package com.rajan.llm_racer.provider;

public interface LLMProvider {
    String generate(String prompt, String orgId);
}
