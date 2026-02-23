package com.rajan.llmracer.service;

import com.rajan.llmracer.models.GenerateRequest;

public interface GenerateService {
    String generate(GenerateRequest request);
}
