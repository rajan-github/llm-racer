package com.rajan.llm_racer.controllers;

import com.rajan.llm_racer.models.GenerateRequest;
import com.rajan.llm_racer.models.GenerateResponse;
import com.rajan.llm_racer.services.GenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenerateController {
    private final GenerateService generateService;

    @Autowired
    public GenerateController(GenerateService generateService) {
        this.generateService = generateService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> handleRequest(@RequestBody GenerateRequest req) {
        var response = generateService.generate(req);
        return new ResponseEntity<>(new GenerateResponse(response, req.orgId()), HttpStatus.OK);
    }
}
