package com.rajan.llm_racer.models;

public record GenerateRequest(String prompt, String orgId, long timeoutSeconds) {
}
