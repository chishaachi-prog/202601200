package com.burpai.agent.core;

import burp.api.montoya.http.message.HttpRequest;
import com.burpai.agent.config.ConfigManager;
import com.burpai.agent.http.HTTPClientManager;
import com.burpai.agent.http.HTTPClientManager.HTTPResponse;
import com.burpai.agent.llm.LLMAdapter;
import com.burpai.agent.llm.model.LLMMessage;
import com.burpai.agent.llm.model.LLMMessage.Role;
import com.burpai.agent.llm.model.LLMResponse;
import com.burpai.agent.ui.DashboardPanel;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent Engine
 * 
 * Implements the ReAct (Reasoning + Acting) loop for AI-powered vulnerability testing.
 */
public class AgentEngine {
    
    private final LLMAdapter llmAdapter;
    private final HTTPClientManager httpClientManager;
    private final ConfigManager configManager;
    private final PromptBuilder promptBuilder;
    private final RequestModifier requestModifier;
    
    public AgentEngine(LLMAdapter llmAdapter, 
                      HTTPClientManager httpClientManager, 
                      ConfigManager configManager) {
        this.llmAdapter = llmAdapter;
        this.httpClientManager = httpClientManager;
        this.configManager = configManager;
        this.promptBuilder = new PromptBuilder(httpClientManager, configManager);
        this.requestModifier = new RequestModifier(httpClientManager);
    }
    
    /**
     * Run the ReAct loop for a given scan task
     */
    public AgentResult runAgent(HttpRequest initialRequest, 
                               String scanType, 
                               String customPrompt,
                               DashboardPanel dashboard,
                               int taskId) {
        
        int maxIterations = configManager.getScanPolicy().getMaxIterations();
        List<String> conversationHistory = new ArrayList<>();
        StringBuilder thoughts = new StringBuilder();
        
        dashboard.addTaskMessage(taskId, DashboardPanel.MessageType.AI_THOUGHT,
                "Starting analysis for scan type: " + scanType);
        
        // Build initial system prompt
        String systemPrompt = promptBuilder.buildSystemPrompt(
                initialRequest, scanType, customPrompt);
        
        // Create initial messages
        List<LLMMessage> messages = new ArrayList<>();
        messages.add(new LLMMessage(Role.SYSTEM, systemPrompt));
        
        int iteration = 0;
        
        // ReAct Loop
        while (iteration < maxIterations) {
            iteration++;
            
            dashboard.addTaskMessage(taskId, DashboardPanel.MessageType.INFO,
                    "Iteration " + iteration + "/" + maxIterations);
            
            // Call LLM
            LLMResponse llmResponse = llmAdapter.chat(messages);
            
            if (!llmResponse.isSuccessful()) {
                return AgentResult.error("LLM API call failed: " + llmResponse.getErrorMessage());
            }
            
            String responseContent = llmResponse.getContent();
            
            // Try to parse JSON response
            JsonObject jsonResponse;
            try {
                jsonResponse = JsonParser.parseString(responseContent).getAsJsonObject();
            } catch (Exception e) {
                // Try to extract JSON from response
                jsonResponse = extractJSON(responseContent);
                if (jsonResponse == null) {
                    return AgentResult.error("Failed to parse LLM response as JSON: " + responseContent);
                }
            }
            
            // Get thought
            String thought = jsonResponse.has("thought") ? 
                    jsonResponse.get("thought").getAsString() : "No thought provided";
            
            thoughts.append(thought).append("\n\n");
            
            dashboard.addTaskMessage(taskId, DashboardPanel.MessageType.AI_THOUGHT, thought);
            
            // Get action
            String action = jsonResponse.has("action") ? 
                    jsonResponse.get("action").getAsString() : "";
            
            if ("finish".equals(action)) {
                // Parse final result
                return parseFinishResult(jsonResponse, thoughts.toString());
            }
            
            if (!"send_request".equals(action)) {
                return AgentResult.error("Unknown action: " + action);
            }
            
            // Parse request modification
            if (!jsonResponse.has("request_modification")) {
                return AgentResult.error("Missing request_modification in response");
            }
            
            JsonObject modification = jsonResponse.getAsJsonObject("request_modification");
            
            // Modify and send request
            HttpRequest modifiedRequest;
            if (iteration == 1) {
                modifiedRequest = initialRequest;
            } else {
                modifiedRequest = requestModifier.modifyRequest(
                        initialRequest, modification);
            }
            
            String modificationDesc = requestModifier.getModificationDescription(modification);
            dashboard.addTaskMessage(taskId, DashboardPanel.MessageType.SYSTEM_ACTION,
                    "Sending modified request: " + modificationDesc);
            
            HTTPResponse httpResponse = httpClientManager.sendRequest(modifiedRequest);
            
            if (!httpResponse.isSuccessful()) {
                return AgentResult.error("HTTP request failed: " + httpResponse.getErrorMessage());
            }
            
            // Format observation
            String observation = httpResponse.formatForLLM();
            dashboard.addTaskMessage(taskId, DashboardPanel.MessageType.OBSERVATION,
                    String.format("Status: %d, Time: %dms, Length: %d",
                            httpResponse.getStatusCode(),
                            httpResponse.getResponseTime(),
                            httpResponse.getContentLength()));
            
            // Build observation prompt
            String observationPrompt = promptBuilder.buildObservationPrompt(
                    thoughts.toString(), observation, iteration);
            
            // Add to messages for next iteration
            messages.add(new LLMMessage(Role.ASSISTANT, responseContent));
            messages.add(new LLMMessage(Role.USER, observationPrompt));
        }
        
        // Max iterations reached
        return AgentResult.noVulnerabilityFound(
                "Reached maximum iterations (" + maxIterations + 
                ") without conclusive evidence. Analysis: " + thoughts.toString());
    }
    
    /**
     * Extract JSON from potentially mixed response
     */
    private JsonObject extractJSON(String response) {
        try {
            // Find JSON object boundaries
            int startIndex = response.indexOf("{");
            int endIndex = response.lastIndexOf("}");
            
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String jsonStr = response.substring(startIndex, endIndex + 1);
                return JsonParser.parseString(jsonStr).getAsJsonObject();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    /**
     * Parse finish result from JSON
     */
    private AgentResult parseFinishResult(JsonObject jsonResponse, String thoughts) {
        if (!jsonResponse.has("vulnerability_found")) {
            return AgentResult.error("Missing vulnerability_found field in finish result");
        }
        
        boolean vulnFound = jsonResponse.get("vulnerability_found").getAsBoolean();
        
        if (vulnFound) {
            String vulnType = jsonResponse.has("vulnerability_type") ?
                    jsonResponse.get("vulnerability_type").getAsString() : "Unknown";
            String severity = jsonResponse.has("severity") ?
                    jsonResponse.get("severity").getAsString() : "Medium";
            String evidence = jsonResponse.has("evidence") ?
                    jsonResponse.get("evidence").getAsString() : "No evidence provided";
            String remediation = jsonResponse.has("remediation") ?
                    jsonResponse.get("remediation").getAsString() : "";
            
            return AgentResult.vulnerabilityFound(
                    vulnType, severity, evidence, remediation, thoughts.toString());
        } else {
            return AgentResult.noVulnerabilityFound(thoughts.toString());
        }
    }
    
    /**
     * Agent Result Model
     */
    public static class AgentResult {
        private final boolean successful;
        private final boolean vulnerabilityFound;
        private final String vulnerabilityType;
        private final String severity;
        private final String evidence;
        private final String remediation;
        private final String thoughtProcess;
        private final String errorMessage;
        
        private AgentResult(boolean successful, boolean vulnerabilityFound, 
                          String vulnerabilityType, String severity, 
                          String evidence, String remediation, 
                          String thoughtProcess, String errorMessage) {
            this.successful = successful;
            this.vulnerabilityFound = vulnerabilityFound;
            this.vulnerabilityType = vulnerabilityType;
            this.severity = severity;
            this.evidence = evidence;
            this.remediation = remediation;
            this.thoughtProcess = thoughtProcess;
            this.errorMessage = errorMessage;
        }
        
        public static AgentResult vulnerabilityFound(String type, String severity, 
                                                    String evidence, String remediation,
                                                    String thoughtProcess) {
            return new AgentResult(true, true, type, severity, 
                    evidence, remediation, thoughtProcess, null);
        }
        
        public static AgentResult noVulnerabilityFound(String thoughtProcess) {
            return new AgentResult(true, false, null, null, 
                    null, null, thoughtProcess, null);
        }
        
        public static AgentResult error(String errorMessage) {
            return new AgentResult(false, false, null, null, 
                    null, null, null, errorMessage);
        }
        
        // Getters
        public boolean isSuccessful() { return successful; }
        public boolean isVulnerabilityFound() { return vulnerabilityFound; }
        public String getVulnerabilityType() { return vulnerabilityType; }
        public String getSeverity() { return severity; }
        public String getEvidence() { return evidence; }
        public String getRemediation() { return remediation; }
        public String getThoughtProcess() { return thoughtProcess; }
        public String getErrorMessage() { return errorMessage; }
    }
}
