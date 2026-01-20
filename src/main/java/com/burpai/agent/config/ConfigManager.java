package com.burpai.agent.config;

import burp.api.montoya.MontoyaApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration Manager
 * 
 * Manages all plugin configuration including AI settings, scope, and scan policies.
 */
public class ConfigManager {
    
    private static final String CONFIG_PREFIX = "burpai.";
    
    // AI Model Configuration
    private ModelConfig modelConfig;
    
    // Scope Configuration
    private ScopeConfig scopeConfig;
    
    // Scan Policy Configuration
    private ScanPolicy scanPolicy;
    
    private final MontoyaApi api;
    private final Gson gson;
    
    public ConfigManager(MontoyaApi api) {
        this.api = api;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadConfiguration();
    }
    
    /**
     * Load all configuration from Burp extension settings
     */
    private void loadConfiguration() {
        // Load Model Configuration
        String modelConfigJson = api.persistence().extensionData().getString(CONFIG_PREFIX + "model_config");
        if (modelConfigJson != null && !modelConfigJson.isEmpty()) {
            this.modelConfig = gson.fromJson(modelConfigJson, ModelConfig.class);
        } else {
            this.modelConfig = new ModelConfig();
        }
        
        // Load Scope Configuration
        String scopeConfigJson = api.persistence().extensionData().getString(CONFIG_PREFIX + "scope_config");
        if (scopeConfigJson != null && !scopeConfigJson.isEmpty()) {
            this.scopeConfig = gson.fromJson(scopeConfigJson, ScopeConfig.class);
        } else {
            this.scopeConfig = new ScopeConfig();
        }
        
        // Load Scan Policy
        String scanPolicyJson = api.persistence().extensionData().getString(CONFIG_PREFIX + "scan_policy");
        if (scanPolicyJson != null && !scanPolicyJson.isEmpty()) {
            this.scanPolicy = gson.fromJson(scanPolicyJson, ScanPolicy.class);
        } else {
            this.scanPolicy = new ScanPolicy();
        }
    }
    
    /**
     * Save all configuration to Burp extension settings
     */
    public void saveConfiguration() {
        api.persistence().extensionData().setString(
                CONFIG_PREFIX + "model_config",
                gson.toJson(modelConfig)
        );
        
        api.persistence().extensionData().setString(
                CONFIG_PREFIX + "scope_config",
                gson.toJson(scopeConfig)
        );
        
        api.persistence().extensionData().setString(
                CONFIG_PREFIX + "scan_policy",
                gson.toJson(scanPolicy)
        );
    }
    
    /**
     * Check if the plugin is properly configured
     */
    public boolean isConfigured() {
        return modelConfig != null 
                && modelConfig.getProvider() != null 
                && !modelConfig.getProvider().isEmpty()
                && modelConfig.getApiKey() != null 
                && !modelConfig.getApiKey().isEmpty()
                && modelConfig.getModel() != null 
                && !modelConfig.getModel().isEmpty();
    }
    
    // Getters and Setters
    public ModelConfig getModelConfig() {
        return modelConfig;
    }
    
    public void setModelConfig(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
    }
    
    public ScopeConfig getScopeConfig() {
        return scopeConfig;
    }
    
    public void setScopeConfig(ScopeConfig scopeConfig) {
        this.scopeConfig = scopeConfig;
    }
    
    public ScanPolicy getScanPolicy() {
        return scanPolicy;
    }
    
    public void setScanPolicy(ScanPolicy scanPolicy) {
        this.scanPolicy = scanPolicy;
    }
    
    /**
     * Model Configuration Class
     */
    public static class ModelConfig {
        private String provider = "OpenAI"; // OpenAI, Anthropic, Azure, Local
        private String apiKey = "";
        private String model = "gpt-4o";
        private String baseUrl = "https://api.openai.com/v1";
        private int timeout = 30; // seconds
        
        public ModelConfig() {
            // Default values
        }
        
        // Getters and Setters
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }
    
    /**
     * Scope Configuration Class
     */
    public static class ScopeConfig {
        private boolean useBurpScope = true;
        private Set<String> includeHosts = new HashSet<String>();
        private Set<String> excludeHosts = new HashSet<String>();
        private Set<String> excludeExtensions = new HashSet<String>(
                Arrays.asList("jpg", "jpeg", "png", "gif", "css", "js", "woff", "woff2", "svg", "ico", "pdf", "ttf", "eot")
        );
        
        public ScopeConfig() {
            // Default values
        }
        
        // Getters and Setters
        public boolean isUseBurpScope() { return useBurpScope; }
        public void setUseBurpScope(boolean useBurpScope) { this.useBurpScope = useBurpScope; }
        
        public Set<String> getIncludeHosts() { return includeHosts; }
        public void setIncludeHosts(Set<String> includeHosts) { this.includeHosts = includeHosts; }
        
        public Set<String> getExcludeHosts() { return excludeHosts; }
        public void setExcludeHosts(Set<String> excludeHosts) { this.excludeHosts = excludeHosts; }
        
        public Set<String> getExcludeExtensions() { return excludeExtensions; }
        public void setExcludeExtensions(Set<String> excludeExtensions) { this.excludeExtensions = excludeExtensions; }
    }
    
    /**
     * Scan Policy Configuration Class
     */
    public static class ScanPolicy {
        // Vulnerability type toggles
        private boolean sqlInjectionEnabled = true;
        private boolean xssEnabled = true;
        private boolean idorEnabled = true;
        private boolean ssrfEnabled = true;
        private boolean fileUploadEnabled = true;
        private boolean rceEnabled = false;
        private boolean businessLogicEnabled = false;
        
        // Agent behavior parameters
        private int maxIterations = 5;
        private String confidenceLevel = "Medium"; // Low, Medium, High
        
        public ScanPolicy() {
            // Default values
        }
        
        // Getters and Setters
        public boolean isSqlInjectionEnabled() { return sqlInjectionEnabled; }
        public void setSqlInjectionEnabled(boolean enabled) { this.sqlInjectionEnabled = enabled; }
        
        public boolean isXssEnabled() { return xssEnabled; }
        public void setXssEnabled(boolean enabled) { this.xssEnabled = enabled; }
        
        public boolean isIdorEnabled() { return idorEnabled; }
        public void setIdorEnabled(boolean enabled) { this.idorEnabled = enabled; }
        
        public boolean isSsrfEnabled() { return ssrfEnabled; }
        public void setSsrfEnabled(boolean enabled) { this.ssrfEnabled = enabled; }
        
        public boolean isFileUploadEnabled() { return fileUploadEnabled; }
        public void setFileUploadEnabled(boolean enabled) { this.fileUploadEnabled = enabled; }
        
        public boolean isRceEnabled() { return rceEnabled; }
        public void setRceEnabled(boolean enabled) { this.rceEnabled = enabled; }
        
        public boolean isBusinessLogicEnabled() { return businessLogicEnabled; }
        public void setBusinessLogicEnabled(boolean enabled) { this.businessLogicEnabled = enabled; }
        
        public int getMaxIterations() { return maxIterations; }
        public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }
        
        public String getConfidenceLevel() { return confidenceLevel; }
        public void setConfidenceLevel(String confidenceLevel) { this.confidenceLevel = confidenceLevel; }
    }
}
