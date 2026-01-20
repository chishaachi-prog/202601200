package com.burpai.agent.llm.model;

/**
 * LLM Message Model
 * 
 * Represents a message in the conversation with the LLM.
 */
public class LLMMessage {
    
    public enum Role {
        SYSTEM,
        USER,
        ASSISTANT
    }
    
    private Role role;
    private String content;
    
    public LLMMessage(Role role, String content) {
        this.role = role;
        this.content = content;
    }
    
    // Getters and Setters
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}
