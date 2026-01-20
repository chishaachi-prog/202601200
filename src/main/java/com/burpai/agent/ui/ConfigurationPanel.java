package com.burpai.agent.ui;

import burp.api.montoya.MontoyaApi;
import com.burpai.agent.config.ConfigManager;
import com.burpai.agent.llm.LLMAdapter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Configuration Panel
 * 
 * Provides UI for configuring the BurpAI Agent extension.
 */
public class ConfigurationPanel extends JPanel {
    
    private final MontoyaApi api;
    private final ConfigManager configManager;
    private final LLMAdapter llmAdapter;
    
    // Model Config components
    private JComboBox<String> providerCombo;
    private JTextField apiKeyField;
    private JTextField modelField;
    private JTextField baseUrlField;
    private JTextField timeoutField;
    
    // Scope Config components
    private JRadioButton useBurpScopeRadio;
    private JRadioButton customScopeRadio;
    private JTextArea includeHostsArea;
    private JTextArea excludeHostsArea;
    private JTextArea excludeExtensionsArea;
    
    // Scan Policy components
    private JCheckBox sqlInjectionCheck;
    private JCheckBox xssCheck;
    private JCheckBox idorCheck;
    private JCheckBox ssrfCheck;
    private JCheckBox fileUploadCheck;
    private JCheckBox rceCheck;
    private JCheckBox businessLogicCheck;
    private JSpinner maxIterationsSpinner;
    private JComboBox<String> confidenceLevelCombo;
    
    public ConfigurationPanel(MontoyaApi api, ConfigManager configManager, LLMAdapter llmAdapter) {
        this.api = api;
        this.configManager = configManager;
        this.llmAdapter = llmAdapter;
        
        initializeUI();
        loadConfiguration();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab 1: Model Configuration
        tabbedPane.addTab("Model & Engine", createModelConfigTab());
        
        // Tab 2: Scope Configuration
        tabbedPane.addTab("Target & Scope", createScopeConfigTab());
        
        // Tab 3: Scan Policy
        tabbedPane.addTab("Scan Policy", createScanPolicyTab());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Save button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Configuration");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveConfiguration();
            }
        });
        buttonPanel.add(saveButton);
        
        JButton testButton = new JButton("Test Connection");
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testConnection();
            }
        });
        buttonPanel.add(testButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createModelConfigTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Provider:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        providerCombo = new JComboBox<>(new String[]{"OpenAI", "Anthropic", "Azure", "Local"});
        panel.add(providerCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(new JLabel("API Key:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        apiKeyField = new JPasswordField(30);
        panel.add(apiKeyField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Model Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        modelField = new JTextField("gpt-4o", 30);
        panel.add(modelField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Base URL:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        baseUrlField = new JTextField("https://api.openai.com/v1", 30);
        panel.add(baseUrlField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Timeout (seconds):"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        timeoutField = new JTextField("30", 10);
        panel.add(timeoutField, gbc);
        
        // Add description
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel descLabel = new JLabel("<html><i>For Local LLMs (e.g., Ollama), use Base URL: http://localhost:11434/v1</i></html>");
        descLabel.setForeground(Color.GRAY);
        panel.add(descLabel, gbc);
        
        return panel;
    }
    
    private JPanel createScopeConfigTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // Scope mode
        JPanel scopeModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scopeModePanel.setBorder(new TitledBorder("Scope Mode"));
        
        useBurpScopeRadio = new JRadioButton("Use Burp Suite Scope", true);
        customScopeRadio = new JRadioButton("Custom Scope");
        
        ButtonGroup scopeGroup = new ButtonGroup();
        scopeGroup.add(useBurpScopeRadio);
        scopeGroup.add(customScopeRadio);
        
        scopeModePanel.add(useBurpScopeRadio);
        scopeModePanel.add(customScopeRadio);
        
        panel.add(scopeModePanel, BorderLayout.NORTH);
        
        // Host filters
        JPanel hostFilterPanel = new JPanel(new GridBagLayout());
        hostFilterPanel.setBorder(new TitledBorder("Host Filters"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        hostFilterPanel.add(new JLabel("Include Hosts (one per line):"), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        includeHostsArea = new JTextArea(5, 30);
        includeHostsArea.setLineWrap(true);
        hostFilterPanel.add(new JScrollPane(includeHostsArea), gbc);
        
        gbc.gridx = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        hostFilterPanel.add(new JLabel("Exclude Hosts:"), gbc);
        
        gbc.gridy = 2;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        excludeHostsArea = new JTextArea(5, 30);
        excludeHostsArea.setLineWrap(true);
        hostFilterPanel.add(new JScrollPane(excludeHostsArea), gbc);
        
        panel.add(hostFilterPanel, BorderLayout.CENTER);
        
        // Extension filter
        JPanel extensionPanel = new JPanel(new BorderLayout(5, 5));
        extensionPanel.setBorder(new TitledBorder("Extension Filter (blacklist)"));
        
        excludeExtensionsArea = new JTextArea(3, 40);
        excludeExtensionsArea.setText("jpg,jpeg,png,gif,css,js,woff,woff2,svg,ico,pdf,ttf,eot");
        extensionPanel.add(new JScrollPane(excludeExtensionsArea), BorderLayout.CENTER);
        
        panel.add(extensionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createScanPolicyTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // Vulnerability types
        JPanel vulnPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        vulnPanel.setBorder(new TitledBorder("Vulnerability Detection"));
        
        sqlInjectionCheck = new JCheckBox("SQL Injection", true);
        xssCheck = new JCheckBox("XSS (Reflected/Stored)", true);
        idorCheck = new JCheckBox("Broken Access Control (IDOR)", true);
        ssrfCheck = new JCheckBox("SSRF", true);
        fileUploadCheck = new JCheckBox("File Upload Vulnerabilities", true);
        rceCheck = new JCheckBox("Remote Code Execution (RCE)", false);
        businessLogicCheck = new JCheckBox("Business Logic", false);
        
        vulnPanel.add(sqlInjectionCheck);
        vulnPanel.add(xssCheck);
        vulnPanel.add(idorCheck);
        vulnPanel.add(ssrfCheck);
        vulnPanel.add(fileUploadCheck);
        vulnPanel.add(rceCheck);
        vulnPanel.add(businessLogicCheck);
        
        panel.add(vulPanel, BorderLayout.NORTH);
        
        // Agent parameters
        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBorder(new TitledBorder("Agent Parameters"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        paramPanel.add(new JLabel("Max Iterations:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(5, 1, 10, 1);
        maxIterationsSpinner = new JSpinner(spinnerModel);
        paramPanel.add(maxIterationsSpinner, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        paramPanel.add(new JLabel("Confidence Level:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        confidenceLevelCombo = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        paramPanel.add(confidenceLevelCombo, gbc);
        
        // Description
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel descLabel = new JLabel("<html><i>Max Iterations: Number of AI attempts before giving up.<br/>Confidence Level: How conservative the AI should be before reporting a vulnerability.</i></html>");
        descLabel.setForeground(Color.GRAY);
        paramPanel.add(descLabel, gbc);
        
        panel.add(paramPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadConfiguration() {
        // Load model config
        ConfigManager.ModelConfig modelConfig = configManager.getModelConfig();
        if (modelConfig != null) {
            providerCombo.setSelectedItem(modelConfig.getProvider());
            apiKeyField.setText(modelConfig.getApiKey());
            modelField.setText(modelConfig.getModel());
            baseUrlField.setText(modelConfig.getBaseUrl());
            timeoutField.setText(String.valueOf(modelConfig.getTimeout()));
        }
        
        // Load scope config
        ConfigManager.ScopeConfig scopeConfig = configManager.getScopeConfig();
        if (scopeConfig != null) {
            useBurpScopeRadio.setSelected(scopeConfig.isUseBurpScope());
            customScopeRadio.setSelected(!scopeConfig.isUseBurpScope());
            
            includeHostsArea.setText(String.join("\n", scopeConfig.getIncludeHosts()));
            excludeHostsArea.setText(String.join("\n", scopeConfig.getExcludeHosts()));
            excludeExtensionsArea.setText(String.join(",", scopeConfig.getExcludeExtensions()));
        }
        
        // Load scan policy
        ConfigManager.ScanPolicy scanPolicy = configManager.getScanPolicy();
        if (scanPolicy != null) {
            sqlInjectionCheck.setSelected(scanPolicy.isSqlInjectionEnabled());
            xssCheck.setSelected(scanPolicy.isXssEnabled());
            idorCheck.setSelected(scanPolicy.isIdorEnabled());
            ssrfCheck.setSelected(scanPolicy.isSsrfEnabled());
            fileUploadCheck.setSelected(scanPolicy.isFileUploadEnabled());
            rceCheck.setSelected(scanPolicy.isRceEnabled());
            businessLogicCheck.setSelected(scanPolicy.isBusinessLogicEnabled());
            
            maxIterationsSpinner.setValue(scanPolicy.getMaxIterations());
            confidenceLevelCombo.setSelectedItem(scanPolicy.getConfidenceLevel());
        }
    }
    
    private void saveConfiguration() {
        try {
            // Save model config
            ConfigManager.ModelConfig modelConfig = new ConfigManager.ModelConfig();
            modelConfig.setProvider((String) providerCombo.getSelectedItem());
            modelConfig.setApiKey(apiKeyField.getText());
            modelConfig.setModel(modelField.getText());
            modelConfig.setBaseUrl(baseUrlField.getText());
            modelConfig.setTimeout(Integer.parseInt(timeoutField.getText()));
            configManager.setModelConfig(modelConfig);
            
            // Save scope config
            ConfigManager.ScopeConfig scopeConfig = new ConfigManager.ScopeConfig();
            scopeConfig.setUseBurpScope(useBurpScopeRadio.isSelected());
            scopeConfig.setIncludeHosts(parseTextArea(includeHostsArea));
            scopeConfig.setExcludeHosts(parseTextArea(excludeHostsArea));
            scopeConfig.setExcludeExtensions(parseExtensions(excludeExtensionsArea.getText()));
            configManager.setScopeConfig(scopeConfig);
            
            // Save scan policy
            ConfigManager.ScanPolicy scanPolicy = new ConfigManager.ScanPolicy();
            scanPolicy.setSqlInjectionEnabled(sqlInjectionCheck.isSelected());
            scanPolicy.setXssEnabled(xssCheck.isSelected());
            scanPolicy.setIdorEnabled(idorCheck.isSelected());
            scanPolicy.setSsrfEnabled(ssrfCheck.isSelected());
            scanPolicy.setFileUploadEnabled(fileUploadCheck.isSelected());
            scanPolicy.setRceEnabled(rceCheck.isSelected());
            scanPolicy.setBusinessLogicEnabled(businessLogicCheck.isSelected());
            scanPolicy.setMaxIterations((Integer) maxIterationsSpinner.getValue());
            scanPolicy.setConfidenceLevel((String) confidenceLevelCombo.getSelectedItem());
            configManager.setScanPolicy(scanPolicy);
            
            // Persist to Burp storage
            configManager.saveConfiguration();
            
            JOptionPane.showMessageDialog(this, 
                    "Configuration saved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                    
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save configuration: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private java.util.Set<String> parseTextArea(JTextArea area) {
        java.util.Set<String> set = new java.util.HashSet<>();
        String[] lines = area.getText().split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                set.add(line);
            }
        }
        return set;
    }
    
    private java.util.Set<String> parseExtensions(String text) {
        java.util.Set<String> set = new java.util.HashSet<>();
        String[] extensions = text.split(",");
        for (String ext : extensions) {
            ext = ext.trim().toLowerCase().replaceFirst("^\\.", "");
            if (!ext.isEmpty()) {
                set.add(ext);
            }
        }
        return set;
    }
    
    private void testConnection() {
        try {
            // Update config first
            ConfigManager.ModelConfig modelConfig = new ConfigManager.ModelConfig();
            modelConfig.setProvider((String) providerCombo.getSelectedItem());
            modelConfig.setApiKey(apiKeyField.getText());
            modelConfig.setModel(modelField.getText());
            modelConfig.setBaseUrl(baseUrlField.getText());
            modelConfig.setTimeout(Integer.parseInt(timeoutField.getText()));
            
            if (llmAdapter instanceof com.burpai.agent.llm.OpenAIAdapter) {
                // Create new adapter with updated config
                com.burpai.agent.llm.OpenAIAdapter newAdapter = 
                        new com.burpai.agent.llm.OpenAIAdapter(modelConfig);
                
                boolean success = newAdapter.testConnection();
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Connection test successful!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Connection test failed. Please check your API key and network connection.",
                            "Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Connection test not implemented for this provider.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Connection test failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
