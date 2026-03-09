package com.rajan.llm_racer.chaos;

import lombok.Getter;

@Getter
public class ChaosConfig {
    private final double failureRate=0.05;
    private final double timeoutRate=0.02;
    private final double latencySpikeRate=0.10;
    private final long latencyMs = 1000;
}
