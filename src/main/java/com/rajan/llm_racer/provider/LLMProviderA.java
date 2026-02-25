package com.rajan.llm_racer.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component(value="providerA")
@Slf4j
public class LLMProviderA implements LLMProvider {

    @Override
    public String generate(String prompt, String orgId) {
        log.info("ProviderA: generating response for prompt: {} and orgId: {}", prompt, orgId);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return """
                %s
                ========
                Provider A generated response-
                \n\n
                What is the main difference between Expected and Unexpected adverse events?
                Your Answer: b) Expected events are mentioned in the drug's labeling, unexpected events are not
                Correct Answer: b) Expected events are mentioned in the drug's labeling, unexpected events are not
                Result: Correct
                
                Which of the following is a common cause of duplicate reporting in Pharmacovigilance?
                Your Answer: c,d) Both b and c
                Correct Answer: d) Both b and c
                Result: Correct
                
                What does ICSR stand for, and what is its primary purpose?
                Your Answer: a) Individual Case Safety Report; to collect and report adverse event data for a single patient
                Correct Answer: a) Individual Case Safety Report; to collect and report adverse event data for a single patient
                Result: Correct
                
                Which of the following would most likely be considered a Serious Adverse Event (SAE)?
                Your Answer: b) Loss of vision that results in permanent disability
                Correct Answer: b) Loss of vision that results in permanent disability
                Result: Correct
                
                What does the acronym PV stand for in pharmacovigilance?
                Your Answer: b) Pharmacovigilance
                Correct Answer: b) Pharmacovigilance
                Result: Correct
                =======
                %s
                """.formatted(prompt, orgId);
    }
}
