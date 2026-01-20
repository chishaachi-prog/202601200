package com.burpai.agent.llm;

import com.burpai.agent.llm.model.LLMMessage;
import com.burpai.agent.llm.model.LLMResponse;

import java.util.List;

/**
 * LLM Adapter Interface
 * 
 * Defines the contract for interacting with different LLM providers.
 */
public interface LLMAdapter {
    
    /**
     * Send a chat request to the LLM
     * 
     * @param messages List of messages in the conversation
     * @return LLM response
     */
    LLMResponse chat(List<LLMMessage> messages);
    
    /**
     * Test the connection to the LLM API
     * 
     * @return true if connection is successful, false otherwise
     */
    boolean testConnection();
    
    /**
     * Get the provider name
     * 
     * @return Provider name (e.g., "OpenAI", "Anthropic")
     */
    String getProviderName();
}
