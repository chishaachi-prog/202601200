package com.burpai.agent.llm.model;

/**
 * LLM Response Model
 * 
 * Represents the response from the LLM.
 */
public class LLMResponse {
    
    private String content;
    private boolean successful;
    private String errorMessage;
    private int tokensUsed;
    private long responseTime;
    
    public LLMResponse() {
    }
    
    public LLMResponse(String content, boolean successful) {
        this.content = content;
        this.successful = successful;
    }
    
    public static LLMResponse error(String errorMessage) {
        LLMResponse response = new LLMResponse();
        response.setSuccessful(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
    
    public static LLMResponse success(String content) {
        return new LLMResponse(content, true);
    }
    
    // Getters and Setters
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public int getTokensUsed() {
        return tokensUsed;
    }
    
    public void setTokensUsed(int tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
    
    public long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}
