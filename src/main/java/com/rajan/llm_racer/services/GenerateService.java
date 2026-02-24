package com.rajan.llm_racer.services;

import com.rajan.llm_racer.models.GenerateRequest;

public interface GenerateService {
    String generate(GenerateRequest req);
}
