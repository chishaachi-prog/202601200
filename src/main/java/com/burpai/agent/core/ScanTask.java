package com.burpai.agent.core;

import burp.api.montoya.http.message.HttpRequest;
import com.burpai.agent.ui.DashboardPanel;

/**
 * Scan Task
 * 
 * Represents a single scanning task to be executed by the Agent Engine.
 */
public class ScanTask {
    
    private final int taskId;
    private final HttpRequest request;
    private final String scanType;
    private final String customPrompt;
    private final DashboardPanel dashboard;
    private final long createdAt;
    
    public ScanTask(int taskId, HttpRequest request, String scanType, 
                   String customPrompt, DashboardPanel dashboard) {
        this.taskId = taskId;
        this.request = request;
        this.scanType = scanType != null ? scanType : "ALL";
        this.customPrompt = customPrompt;
        this.dashboard = dashboard;
        this.createdAt = System.currentTimeMillis();
        
        // Initialize task in dashboard
        dashboard.addTask(taskId, request.method(), request.url(), scanType);
    }
    
    // Getters
    public int getTaskId() { return taskId; }
    public HttpRequest getRequest() { return request; }
    public String getScanType() { return scanType; }
    public String getCustomPrompt() { return customPrompt; }
    public DashboardPanel getDashboard() { return dashboard; }
    public long getCreatedAt() { return createdAt; }
}
