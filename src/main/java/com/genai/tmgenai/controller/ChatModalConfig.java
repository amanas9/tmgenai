package com.genai.tmgenai.controller;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;

@Configuration
public class ChatModalConfig {

    @Value("${key.opnenapikey}")
    private String OPENAI_API_KEY;
    @Bean
    public ConversationalChain getConversationChain() {
        ConversationalChain chain = ConversationalChain.builder()
                .chatLanguageModel(OpenAiChatModel.builder()
                        .modelName(GPT_3_5_TURBO)
                        .temperature(0.0)
                        .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                        .build())
                .chatMemory(MessageWindowChatMemory.builder()
                        .systemMessage("You are a helpful assistant.")
                        .capacityInMessages(3)
                        .build())
                .build();
        return chain;
    }

}
