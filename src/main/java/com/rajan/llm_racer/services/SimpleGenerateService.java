package com.rajan.llm_racer.services;

import com.rajan.llm_racer.models.GenerateRequest;
import org.springframework.stereotype.Service;

@Service
public class SimpleGenerateService implements GenerateService {
    @Override
    public String generate(GenerateRequest req) {
        return "";
    }
}
