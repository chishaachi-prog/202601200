package com.burpai.agent.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard Panel
 * 
 * Displays scan tasks and their execution progress with thought chain visualization.
 */
public class DashboardPanel extends JPanel {
    
    private JPanel taskListPanel;
    private JEditorPane chatView;
    private Map<Integer, TaskInfo> tasks;
    private int selectedTaskId = -1;
    
    public DashboardPanel() {
        this.tasks = new HashMap<>();
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.4);
        
        // Left panel - Task list
        JPanel leftPanel = createTaskListPanel();
        splitPane.setLeftComponent(leftPanel);
        
        // Right panel - Chat view
        JPanel rightPanel = createChatViewPanel();
        splitPane.setRightComponent(rightPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Bottom panel - Controls
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTaskListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Scan Tasks"));
        
        // Task list
        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createChatViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("AI Analysis Log"));
        
        // Chat view
        chatView = new JEditorPane();
        chatView.setContentType("text/html");
        chatView.setEditable(false);
        chatView.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(chatView);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton clearButton = new JButton("Clear All Tasks");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllTasks();
            }
        });
        panel.add(clearButton);
        
        JButton exportButton = new JButton("Export Report");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportReport();
            }
        });
        panel.add(exportButton);
        
        return panel;
    }
    
    /**
     * Add a new task to the dashboard
     */
    public void addTask(int taskId, String method, String url, String scanType) {
        TaskInfo taskInfo = new TaskInfo(taskId, method, url, scanType);
        tasks.put(taskId, taskInfo);
        
        // Create task panel
        JPanel taskPanel = new JPanel(new BorderLayout(5, 5));
        taskPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        taskPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // Task info
        JLabel infoLabel = new JLabel(String.format("#%d | %s %s | %s",
                taskId, method, url.length() > 40 ? url.substring(0, 40) + "..." : url, scanType));
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        taskPanel.add(infoLabel, BorderLayout.NORTH);
        
        // Status label
        JLabel statusLabel = new JLabel("Status: Pending");
        statusLabel.setForeground(Color.GRAY);
        taskInfo.setStatusLabel(statusLabel);
        taskPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Click listener
        final int selectedId = taskId;
        taskPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectTask(selectedId);
            }
        });
        
        taskPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        taskInfo.setPanel(taskPanel);
        
        taskListPanel.add(taskPanel);
        taskListPanel.revalidate();
        taskListPanel.repaint();
        
        selectTask(taskId);
    }
    
    /**
     * Select a task and show its messages
     */
    private void selectTask(int taskId) {
        this.selectedTaskId = taskId;
        TaskInfo taskInfo = tasks.get(taskId);
        if (taskInfo != null) {
            // Highlight selected task
            for (TaskInfo ti : tasks.values()) {
                ti.getPanel().setBackground(Color.WHITE);
            }
            taskInfo.getPanel().setBackground(new Color(220, 230, 255));
            
            // Update chat view
            updateChatView(taskInfo);
        }
    }
    
    /**
     * Update task status
     */
    public void updateTaskStatus(int taskId, String status) {
        TaskInfo taskInfo = tasks.get(taskId);
        if (taskInfo != null) {
            taskInfo.setStatus(status);
            JLabel statusLabel = taskInfo.getStatusLabel();
            statusLabel.setText("Status: " + status);
            
            if (status.equals("Running")) {
                statusLabel.setForeground(new Color(0, 100, 200));
            } else if (status.equals("Vulnerability Found")) {
                statusLabel.setForeground(Color.RED);
            } else if (status.equals("Finished")) {
                statusLabel.setForeground(new Color(0, 150, 0));
            } else if (status.equals("Error")) {
                statusLabel.setForeground(new Color(200, 0, 0));
            }
            
            if (selectedTaskId == taskId) {
                updateChatView(taskInfo);
            }
        }
    }
    
    /**
     * Add a message to a task
     */
    public void addTaskMessage(int taskId, MessageType type, String message) {
        TaskInfo taskInfo = tasks.get(taskId);
        if (taskInfo != null) {
            taskInfo.addMessage(type, message);
            
            if (selectedTaskId == taskId) {
                updateChatView(taskInfo);
            }
        }
    }
    
    /**
     * Update the chat view for a task
     */
    private void updateChatView(TaskInfo taskInfo) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: monospace; font-size: 12px;'>");
        
        for (TaskMessage msg : taskInfo.getMessages()) {
            html.append("<div style='margin: 5px 0; padding: 5px; border-radius: 3px;");
            
            switch (msg.getType()) {
                case AI_THOUGHT:
                    html.append("background-color: #e8f5e9;'>");
                    html.append("<b>🟢 AI Thought:</b><br/>");
                    break;
                case SYSTEM_ACTION:
                    html.append("background-color: #e3f2fd;'>");
                    html.append("<b>🔵 System Action:</b><br/>");
                    break;
                case OBSERVATION:
                    html.append("background-color: #fff3e0;'>");
                    html.append("<b>🟠 Observation:</b><br/>");
                    break;
                case FINAL_RESULT:
                    html.append("background-color: #ffebee;'>");
                    html.append("<b>🔴 Result:</b><br/>");
                    break;
                case ERROR:
                    html.append("background-color: #ffcdd2;'>");
                    html.append("<b>❌ Error:</b><br/>");
                    break;
                case INFO:
                    html.append("background-color: #f5f5f5;'>");
                    html.append("<b>ℹ️ Info:</b><br/>");
                    break;
                default:
                    html.append("background-color: #ffffff;'>");
            }
            
            html.append(escapeHtml(msg.getMessage()));
            html.append("</div>");
        }
        
        html.append("</body></html>");
        
        chatView.setText(html.toString());
        chatView.setCaretPosition(0);
    }
    
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\n", "<br/>");
    }
    
    private void clearAllTasks() {
        taskListPanel.removeAll();
        tasks.clear();
        selectedTaskId = -1;
        chatView.setText("");
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }
    
    private void exportReport() {
        // TODO: Implement export functionality
        JOptionPane.showMessageDialog(this, 
                "Export functionality will be implemented in a future version.",
                "Export Report",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    public int getTaskCount() {
        return tasks.size();
    }
    
    /**
     * Message Type Enum
     */
    public enum MessageType {
        AI_THOUGHT,
        SYSTEM_ACTION,
        OBSERVATION,
        FINAL_RESULT,
        ERROR,
        INFO
    }
    
    /**
     * Task Message Model
     */
    private static class TaskMessage {
        private final MessageType type;
        private final String message;
        private final long timestamp;
        
        public TaskMessage(MessageType type, String message) {
            this.type = type;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public MessageType getType() { return type; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Task Info Model
     */
    private static class TaskInfo {
        private final int taskId;
        private final String method;
        private final String url;
        private final String scanType;
        private String status;
        private final java.util.List<TaskMessage> messages;
        private JPanel panel;
        private JLabel statusLabel;
        
        public TaskInfo(int taskId, String method, String url, String scanType) {
            this.taskId = taskId;
            this.method = method;
            this.url = url;
            this.scanType = scanType;
            this.status = "Pending";
            this.messages = new java.util.ArrayList<>();
        }
        
        public void addMessage(MessageType type, String message) {
            messages.add(new TaskMessage(type, message));
        }
        
        // Getters and Setters
        public int getTaskId() { return taskId; }
        public String getMethod() { return method; }
        public String getUrl() { return url; }
        public String getScanType() { return scanType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public java.util.List<TaskMessage> getMessages() { return messages; }
        public JPanel getPanel() { return panel; }
        public void setPanel(JPanel panel) { this.panel = panel; }
        public JLabel getStatusLabel() { return statusLabel; }
        public void setStatusLabel(JLabel statusLabel) { this.statusLabel = statusLabel; }
    }
}
