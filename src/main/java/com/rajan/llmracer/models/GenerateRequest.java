package com.rajan.llmracer.models;

public record GenerateRequest(String prompt, String orgId, long timeoutSeconds) {
}
