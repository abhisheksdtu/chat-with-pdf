//package com.chatdoc.chatwithpdf.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.openai.api.OpenAiApi;
//import org.springframework.ai.openai.OpenAiEmbeddingModel;
//
//@Configuration
//public class EmbeddingConfig {
//
//    @Bean
//    public EmbeddingModel embeddingModel() {
//        // Get API key from environment variable
//        String apiKey = System.getenv("OPENAI_API_KEY");
//
//        // Alternatively: hardcode for local dev (NOT recommended for prod)
//        // String apiKey = "sk-...";
//
//        OpenAiApi api = new OpenAiApi(apiKey);
//        return new OpenAiEmbeddingModel(api);
//    }
//}
