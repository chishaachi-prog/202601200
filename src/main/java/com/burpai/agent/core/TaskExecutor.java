package com.burpai.agent.core;

import burp.api.montoya.http.message.HttpRequest;
import com.burpai.agent.ui.DashboardPanel;

import java.util.concurrent.*;

/**
 * Task Executor
 * 
 * Manages concurrent execution of scanning tasks using a thread pool.
 */
public class TaskExecutor {
    
    private final ExecutorService executorService;
    private final AgentEngine agentEngine;
    
    // Default thread pool size: 1-3 concurrent tasks
    private static final int DEFAULT_THREAD_POOL_SIZE = 2;
    
    public TaskExecutor(AgentEngine agentEngine) {
        this.agentEngine = agentEngine;
        this.executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
    }
    
    /**
     * Submit a scan task for execution
     */
    public Future<AgentResult.AgentResult> submitTask(ScanTask task) {
        return executorService.submit(new Callable<AgentResult.AgentResult>() {
            @Override
            public AgentResult.AgentResult call() throws Exception {
                return executeTask(task);
            }
        });
    }
    
    /**
     * Execute a single scan task
     */
    private AgentResult.AgentResult executeTask(ScanTask task) {
        task.getDashboard().updateTaskStatus(task.getTaskId(), "Running");
        
        try {
            AgentResult.AgentResult result = agentEngine.runAgent(
                    task.getRequest(),
                    task.getScanType(),
                    task.getCustomPrompt(),
                    task.getDashboard(),
                    task.getTaskId()
            );
            
            // Update dashboard with final result
            if (result.isSuccessful()) {
                if (result.isVulnerabilityFound()) {
                    task.getDashboard().updateTaskStatus(task.getTaskId(), "Vulnerability Found");
                    task.getDashboard().addTaskMessage(task.getTaskId(), 
                            DashboardPanel.MessageType.FINAL_RESULT,
                            String.format("VULNERABILITY FOUND: %s (%s)\n%s",
                                    result.getVulnerabilityType(),
                                    result.getSeverity(),
                                    result.getEvidence()));
                } else {
                    task.getDashboard().updateTaskStatus(task.getTaskId(), "Finished");
                    task.getDashboard().addTaskMessage(task.getTaskId(),
                            DashboardPanel.MessageType.FINAL_RESULT,
                            "No vulnerability found after analysis.");
                }
            } else {
                task.getDashboard().updateTaskStatus(task.getTaskId(), "Error");
                task.getDashboard().addTaskMessage(task.getTaskId(),
                        DashboardPanel.MessageType.ERROR,
                        "Error: " + result.getErrorMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            task.getDashboard().updateTaskStatus(task.getTaskId(), "Error");
            task.getDashboard().addTaskMessage(task.getTaskId(),
                    DashboardPanel.MessageType.ERROR,
                    "Exception: " + e.getMessage());
            return AgentResult.AgentResult.error(e.getMessage());
        }
    }
    
    /**
     * Shutdown the executor
     */
    public void shutdown() {
        executorService.shutdown();
    }
    
    /**
     * Shutdown the executor forcefully
     */
    public void shutdownNow() {
        executorService.shutdownNow();
    }
}
