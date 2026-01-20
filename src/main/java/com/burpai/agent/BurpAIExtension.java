package com.burpai.agent;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.Extension;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.menu.Menu;
import burp.api.montoya.ui.menu.MenuItem;
import burp.api.montoya.http.message.HttpRequest;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;

import com.burpai.agent.config.ConfigManager;
import com.burpai.agent.ui.ConfigurationPanel;
import com.burpai.agent.ui.DashboardPanel;
import com.burpai.agent.core.AgentEngine;
import com.burpai.agent.core.TaskExecutor;
import com.burpai.agent.http.HTTPClientManager;
import com.burpai.agent.llm.LLMAdapterFactory;
import com.burpai.agent.llm.LLMAdapter;
import com.burpai.agent.utils.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BurpAI Agent - Main Extension Entry Point
 * 
 * Implements the Burp Suite extension interface and initializes all core components.
 */
public class BurpAIExtension implements BurpExtension {
    
    private MontoyaApi api;
    private ConfigManager configManager;
    private AgentEngine agentEngine;
    private TaskExecutor taskExecutor;
    private HTTPClientManager httpClientManager;
    private LLMAdapter llmAdapter;
    private DashboardPanel dashboardPanel;
    
    private static final String EXTENSION_NAME = "BurpAI Agent";
    private static final String EXTENSION_VERSION = "1.0.0-SNAPSHOT";
    
    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        
        // Initialize extension metadata
        Extension extension = api.extension();
        extension.setName(EXTENSION_NAME);
        
        // Initialize logger
        Logger.init(api);
        Logger.info("Initializing " + EXTENSION_NAME + " v" + EXTENSION_VERSION);
        
        // Initialize configuration manager
        configManager = new ConfigManager(api);
        
        // Initialize HTTP client manager
        httpClientManager = new HTTPClientManager(api);
        
        // Initialize LLM adapter
        llmAdapter = LLMAdapterFactory.createAdapter(configManager.getModelConfig());
        
        // Initialize agent engine
        agentEngine = new AgentEngine(llmAdapter, httpClientManager, configManager);
        
        // Initialize task executor
        taskExecutor = new TaskExecutor(agentEngine);
        
        // Initialize UI components
        initializeUI();
        
        // Register context menu
        registerContextMenu();
        
        Logger.info(EXTENSION_NAME + " loaded successfully!");
        Logger.info("JDK Version: " + System.getProperty("java.version"));
    }
    
    /**
     * Initialize UI components (Configuration Tab and Dashboard Tab)
     */
    private void initializeUI() {
        UserInterface ui = api.userInterface();
        
        // Create and register configuration panel
        ConfigurationPanel configPanel = new ConfigurationPanel(api, configManager, llmAdapter);
        ui.registerSuiteTab("BurpAI Config", configPanel);
        
        // Create and register dashboard panel
        dashboardPanel = new DashboardPanel();
        ui.registerSuiteTab("BurpAI Dashboard", dashboardPanel);
        
        Logger.info("UI components registered");
    }
    
    /**
     * Register context menu in Repeater
     */
    private void registerContextMenu() {
        UserInterface ui = api.userInterface();
        
        Menu menu = api.userInterface().menu();
        
        // Main menu item
        MenuItem mainMenuItem = menu.registerMenu("BurpAI Agent");
        
        // Auto Analysis sub-menu
        MenuItem autoAnalysisItem = mainMenuItem.registerMenu("Auto Analysis");
        autoAnalysisItem.registerAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HttpRequest request = api.userInterface().currentHttpRequest();
                if (request != null) {
                    startScan(request, "ALL");
                } else {
                    Logger.warn("No HTTP request selected");
                }
            }
        });
        
        // SQL Injection sub-menu
        MenuItem sqlInjectionItem = mainMenuItem.registerMenu("SQL Injection Only");
        sqlInjectionItem.registerAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HttpRequest request = api.userInterface().currentHttpRequest();
                if (request != null) {
                    startScan(request, "SQL_INJECTION");
                } else {
                    Logger.warn("No HTTP request selected");
                }
            }
        });
        
        // XSS sub-menu
        MenuItem xssItem = mainMenuItem.registerMenu("XSS Only");
        xssItem.registerAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HttpRequest request = api.userInterface().currentHttpRequest();
                if (request != null) {
                    startScan(request, "XSS");
                } else {
                    Logger.warn("No HTTP request selected");
                }
            }
        });
        
        // File Upload sub-menu (NEW)
        MenuItem fileUploadItem = mainMenuItem.registerMenu("File Upload Analysis");
        fileUploadItem.registerAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HttpRequest request = api.userInterface().currentHttpRequest();
                if (request != null) {
                    startScan(request, "FILE_UPLOAD");
                } else {
                    Logger.warn("No HTTP request selected");
                }
            }
        });
        
        // IDOR sub-menu
        MenuItem idorItem = mainMenuItem.registerMenu("IDOR Only");
        idorItem.registerAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HttpRequest request = api.userInterface().currentHttpRequest();
                if (request != null) {
                    startScan(request, "IDOR");
                } else {
                    Logger.warn("No HTTP request selected");
                }
            }
        });
        
        // Custom Prompt sub-menu
        MenuItem customPromptItem = mainMenuItem.registerMenu("Custom Prompt...");
        customPromptItem.registerAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HttpRequest request = api.userInterface().currentHttpRequest();
                if (request != null) {
                    String customPrompt = JOptionPane.showInputDialog(
                            null,
                            "Enter custom analysis prompt:",
                            "Custom Analysis",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (customPrompt != null && !customPrompt.trim().isEmpty()) {
                        startScan(request, "CUSTOM", customPrompt);
                    }
                } else {
                    Logger.warn("No HTTP request selected");
                }
            }
        });
        
        Logger.info("Context menu registered");
    }
    
    /**
     * Start a new scan task
     */
    private void startScan(HttpRequest request, String scanType) {
        startScan(request, scanType, null);
    }
    
    /**
     * Start a new scan task with custom prompt
     */
    private void startScan(HttpRequest request, String scanType, String customPrompt) {
        try {
            // Validate configuration
            if (!configManager.isConfigured()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Please configure API settings in the Configuration tab first.",
                        "Configuration Required",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            
            // Create task
            final AtomicInteger taskId = new AtomicInteger(dashboardPanel.getTaskCount() + 1);
            
            // Submit to executor
            taskExecutor.submitTask(
                    new com.burpai.agent.core.ScanTask(
                            taskId.get(),
                            request,
                            scanType,
                            customPrompt,
                            dashboardPanel
                    )
            );
            
            Logger.info("Started scan task #" + taskId.get() + ": " + request.url());
            
        } catch (Exception ex) {
            Logger.error("Failed to start scan: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
