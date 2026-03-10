package com.rajan.llm_racer.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ChaosConfig {
    @Value(value = "${application.chaos.failureRate}")
    private double failureRate = 0.05;

    @Value(value = "${application.chaos.timeoutRate}")
    private double timeoutRate = 0.02;
    @Value(value = "${application.chaos.latencySpikeRate}")
    private double latencySpikeRate = 0.10;
    @Value(value = "${application.chaos.latencyMs}")
    private long latencyMs = 1000;
}
