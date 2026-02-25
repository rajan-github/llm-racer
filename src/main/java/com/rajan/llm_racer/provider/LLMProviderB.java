package com.rajan.llm_racer.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component(value="providerB")
@Slf4j
public class LLMProviderB implements LLMProvider {
    @Override
    public String generate(String prompt, String orgId) {
        log.info("ProviderA: generating response for prompt: {} and orgId: {}", prompt, orgId);
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return """
                %s
                ========
                LLMProviderB produced--
                \n
                Addressing the Israeli Parliament, the Knesset, PM Benjamin Netanyahu hailed PM Modi and called him a great leader on the world stage.
                               “India is a gigantic power of almost one and a half billion people. Israel is somewhat smaller, but Israel is gigantic too. I want to say that the alliance between us is an enormous multiplier of our individual powers, an enormous multiplier...You stood with us,” Mr. Netanyahu.
                =======
                %s
                """.formatted(prompt, orgId);
    }
}
