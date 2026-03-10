package com.rajan.llm_racer.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
@Data
@Configuration
public class ApplicationProperties {

    @Value(value="${application.chaos.enabled}")
    private boolean isCaosEnabled;
}
