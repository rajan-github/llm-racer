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

    @PostMapping("/generate-virtual")
    public ResponseEntity<GenerateResponse> handleRequestWithVirtualThreads(@RequestBody GenerateRequest req) {
        var response = generateService.generate(req);
        return new ResponseEntity<>(new GenerateResponse(response, req.orgId()), HttpStatus.OK);
    }

    @PostMapping("/generate-platform")
    public ResponseEntity<GenerateResponse> handleRequestWithPlatformThreads(@RequestBody GenerateRequest req) {
        var response = generateService.generate(req);
        return new ResponseEntity<>(new GenerateResponse(response, req.orgId()), HttpStatus.OK);
    }
}
