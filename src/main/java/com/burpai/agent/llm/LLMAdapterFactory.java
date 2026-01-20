package com.burpai.agent.llm;

import com.burpai.agent.config.ConfigManager.ModelConfig;

/**
 * LLM Adapter Factory
 * 
 * Creates appropriate LLM adapter based on provider configuration.
 */
public class LLMAdapterFactory {
    
    public static LLMAdapter createAdapter(ModelConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ModelConfig cannot be null");
        }
        
        String provider = config.getProvider();
        
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("Provider must be specified");
        }
        
        switch (provider.toLowerCase()) {
            case "openai":
            case "azure":
            case "local":
                // All OpenAI-compatible APIs use the same adapter
                return new OpenAIAdapter(config);
            
            case "anthropic":
                // Future implementation
                return new OpenAIAdapter(config); // Temporary fallback
            
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }
}
