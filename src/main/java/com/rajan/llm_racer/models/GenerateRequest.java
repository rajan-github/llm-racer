package com.rajan.llm_racer.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerateRequest(@JsonProperty("prompt") String prompt, @JsonProperty("orgId") String orgId,
                              @JsonProperty("timeoutSeconds") long timeoutSeconds) {
}
