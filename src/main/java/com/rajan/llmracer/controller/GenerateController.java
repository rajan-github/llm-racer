package com.rajan.llmracer.controller;

import com.rajan.llmracer.models.GenerateRequest;
import com.rajan.llmracer.models.GenerateResponse;
import com.rajan.llmracer.service.GenerateService;
import io.javalin.Javalin;

public class GenerateController {
    private final GenerateService generateService;

    public GenerateController(final GenerateService service) {
        this.generateService = service;
    }

    public void register(Javalin app) {
        app.post("/generate", ctx -> {
            var request = ctx.bodyAsClass(GenerateRequest.class);
            var result = this.generateService.generate(request);
            ctx.json(new GenerateResponse(result, request.orgId()));
        });
    }
}
