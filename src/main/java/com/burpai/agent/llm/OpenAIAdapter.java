package com.burpai.agent.llm;

import com.burpai.agent.config.ConfigManager.ModelConfig;
import com.burpai.agent.llm.model.LLMMessage;
import com.burpai.agent.llm.model.LLMMessage.Role;
import com.burpai.agent.llm.model.LLMResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI LLM Adapter Implementation
 * 
 * Handles communication with OpenAI-compatible APIs (including Azure and Local models).
 */
public class OpenAIAdapter implements LLMAdapter {
    
    private static final String CHAT_ENDPOINT = "/chat/completions";
    
    private final ModelConfig config;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final MediaType JSON_MEDIA_TYPE;
    
    public OpenAIAdapter(ModelConfig config) {
        this.config = config;
        this.gson = new Gson();
        this.JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
        
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS);
        
        this.httpClient = builder.build();
    }
    
    @Override
    public LLMResponse chat(List<LLMMessage> messages) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Build request body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", config.getModel());
            
            JsonArray messagesArray = new JsonArray();
            for (LLMMessage message : messages) {
                JsonObject msg = new JsonObject();
                msg.addProperty("role", mapRole(message.getRole()));
                msg.addProperty("content", message.getContent());
                messagesArray.add(msg);
            }
            requestBody.add("messages", messagesArray);
            
            // Set temperature for more deterministic results
            requestBody.addProperty("temperature", 0.3);
            
            // Build HTTP request
            String url = config.getBaseUrl();
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += CHAT_ENDPOINT;
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(JSON_MEDIA_TYPE, gson.toJson(requestBody)))
                    .build();
            
            // Execute request
            Response response = httpClient.newCall(request).execute();
            
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                return LLMResponse.error("API request failed with status " + response.code() + ": " + errorBody);
            }
            
            // Parse response
            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");
                String content = message.get("content").getAsString();
                
                LLMResponse llmResponse = LLMResponse.success(content);
                llmResponse.setResponseTime(System.currentTimeMillis() - startTime);
                
                // Extract token usage if available
                if (jsonResponse.has("usage")) {
                    JsonObject usage = jsonResponse.getAsJsonObject("usage");
                    llmResponse.setTokensUsed(usage.get("total_tokens").getAsInt());
                }
                
                return llmResponse;
            } else {
                return LLMResponse.error("Invalid response format: no choices found");
            }
            
        } catch (IOException e) {
            return LLMResponse.error("Network error: " + e.getMessage());
        } catch (Exception e) {
            return LLMResponse.error("Error: " + e.getMessage());
        }
    }
    
    @Override
    public boolean testConnection() {
        List<LLMMessage> messages = new ArrayList<>();
        messages.add(new LLMMessage(Role.USER, "Hello! Can you respond with just 'OK'?"));
        
        LLMResponse response = chat(messages);
        return response.isSuccessful();
    }
    
    @Override
    public String getProviderName() {
        return config.getProvider();
    }
    
    /**
     * Map internal role to OpenAI role format
     */
    private String mapRole(Role role) {
        switch (role) {
            case SYSTEM:
                return "system";
            case USER:
                return "user";
            case ASSISTANT:
                return "assistant";
            default:
                return "user";
        }
    }
}
