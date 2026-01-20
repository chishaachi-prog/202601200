package com.burpai.agent.core;

import burp.api.montoya.http.message.HttpRequest;
import com.burpai.agent.config.ConfigManager;
import com.burpai.agent.http.HTTPClientManager;
import com.burpai.agent.payloads.FileUploadPayloadGenerator;

import java.util.List;

/**
 * System Prompt Builder
 * 
 * Constructs comprehensive system prompts for the LLM with vulnerability-specific instructions.
 */
public class PromptBuilder {
    
    private final HTTPClientManager httpClientManager;
    private final ConfigManager configManager;
    
    public PromptBuilder(HTTPClientManager httpClientManager, ConfigManager configManager) {
        this.httpClientManager = httpClientManager;
        this.configManager = configManager;
    }
    
    /**
     * Build the initial system prompt for analysis
     */
    public String buildSystemPrompt(HttpRequest request, String scanType, String customPrompt) {
        StringBuilder prompt = new StringBuilder();
        
        // Basic system prompt
        prompt.append("You are an expert penetration tester with authorization to test the target system.\n");
        prompt.append("Your task is to analyze HTTP requests and identify security vulnerabilities.\n\n");
        
        // Target information
        prompt.append("**Target Information:**\n");
        prompt.append("- URL: ").append(request.url()).append("\n");
        prompt.append("- Method: ").append(request.method()).append("\n");
        prompt.append("- Scan Type: ").append(scanType).append("\n\n");
        
        // Scan types enabled
        prompt.append("**Scan Types Enabled:**\n");
        ConfigManager.ScanPolicy policy = configManager.getScanPolicy();
        if (policy.isSqlInjectionEnabled()) {
            prompt.append("- SQL Injection\n");
        }
        if (policy.isXssEnabled()) {
            prompt.append("- XSS (Reflected/Stored)\n");
        }
        if (policy.isIdorEnabled()) {
            prompt.append("- Broken Access Control (IDOR)\n");
        }
        if (policy.isSsrfEnabled()) {
            prompt.append("- SSRF\n");
        }
        if (policy.isFileUploadEnabled()) {
            prompt.append("- File Upload Vulnerabilities\n");
        }
        if (policy.isRceEnabled()) {
            prompt.append("- Remote Code Execution (RCE)\n");
        }
        if (policy.isBusinessLogicEnabled()) {
            prompt.append("- Business Logic Flaws\n");
        }
        prompt.append("\n");
        
        // Agent parameters
        prompt.append("**Agent Parameters:**\n");
        prompt.append("- Max Iterations: ").append(policy.getMaxIterations()).append("\n");
        prompt.append("- Confidence Level: ").append(policy.getConfidenceLevel()).append("\n\n");
        
        // Custom prompt if provided
        if (customPrompt != null && !customPrompt.isEmpty()) {
            prompt.append("**Custom Instruction:**\n");
            prompt.append(customPrompt).append("\n\n");
        }
        
        // Instructions
        prompt.append("**Instructions:**\n");
        prompt.append("1. Carefully analyze the HTTP request structure and identify potential injection points.\n");
        prompt.append("2. Look for suspicious parameters, file uploads, headers, or data patterns.\n");
        prompt.append("3. If you want to perform a test, output a JSON object with \"action\": \"send_request\".\n");
        prompt.append("4. If you believe you have found a vulnerability, output \"action\": \"finish\" with evidence.\n");
        prompt.append("5. If the target appears secure after analysis, output \"action\": \"finish\" with vulnerability_found: false.\n");
        prompt.append("6. Do NOT provide explanations outside the JSON structure.\n");
        prompt.append("7. Be precise and technical in your analysis.\n\n");
        
        // Output format
        prompt.append("**Output Format (MUST be valid JSON):**\n");
        prompt.append("{\n");
        prompt.append("  \"thought\": \"Your reasoning process\",\n");
        prompt.append("  \"action\": \"send_request\" | \"finish\",\n");
        prompt.append("  \"request_modification\": { ... },  // Only if action is \"send_request\"\n");
        prompt.append("  \"vulnerability_found\": true | false,  // Only if action is \"finish\"\n");
        prompt.append("  \"vulnerability_type\": \"...\",  // Only if vulnerability found\n");
        prompt.append("  \"severity\": \"Low|Medium|High|Critical\",  // Only if vulnerability found\n");
        prompt.append("  \"evidence\": \"...\"  // Only if vulnerability found\n");
        prompt.append("}\n\n");
        
        // Add scan type specific instructions
        prompt.append(getScanTypeSpecificInstructions(scanType));
        
        // Original request
        prompt.append("\n**Original Request:**\n");
        prompt.append(httpClientManager.formatRequestForLLM(request));
        
        return prompt.toString();
    }
    
    /**
     * Get scan type specific instructions
     */
    private String getScanTypeSpecificInstructions(String scanType) {
        StringBuilder sb = new StringBuilder();
        
        if (scanType.equals("FILE_UPLOAD") || scanType.equals("ALL")) {
            sb.append("\n**File Upload Testing Guidelines:**\n");
            sb.append("When testing file upload functionality, pay attention to:\n");
            sb.append("- File extension validation bypass techniques\n");
            sb.append("- MIME type spoofing\n");
            sb.append("- Double extensions (e.g., .php.jpg)\n");
            sb.append("- Null byte injection\n");
            sb.append("- Magic header manipulation (GIF89a, etc.)\n");
            sb.append("- .htaccess or web.config upload\n");
            sb.append("- Polyglot files (valid image + malicious code)\n");
            sb.append("- Special character bypasses\n");
            sb.append("- Unicode bypass techniques\n");
            sb.append("- Archive upload exploits (zip, tar, rar)\n");
            sb.append("\n");
            sb.append("**Test these file upload payloads systematically:**\n");
            List<FileUploadPayloadGenerator.FileUploadPayload> payloads = 
                    FileUploadPayloadGenerator.generateAllPayloads();
            sb.append("You should test ").append(payloads.size()).append(" different payload types:\n");
            
            // Group by type
            sb.append("\n1. Web Shells: PHP, JSP, ASPX, ASP\n");
            sb.append("2. Image-Embedded: GIF/JPEG/PNG headers with code\n");
            sb.append("3. Double Extensions: .php.jpg, .shell.php5, etc.\n");
            sb.append("4. Null Byte Injection: file.php%00.jpg\n");
            sb.append("5. .htaccess Attacks: Force execution of images as code\n");
            sb.append("6. Config Injection: web.config, .user.ini\n");
            sb.append("7. MIME Spoofing: PHP code with image MIME type\n");
            sb.append("8. Special Characters: Trailing dots, spaces, ::DATA\n");
            sb.append("9. Unicode Bypass: Non-standard characters\n");
            sb.append("10. XXE via XML: SVG with external entity\n");
            sb.append("11. SSRF via Upload: SVG with internal requests\n");
            sb.append("12. Archive Exploits: ZIP/TAR with embedded shell\n");
        }
        
        if (scanType.equals("SQL_INJECTION") || scanType.equals("ALL")) {
            sb.append("\n**SQL Injection Testing Guidelines:**\n");
            sb.append("Focus on these indicators:\n");
            sb.append("- Error messages containing database keywords (MySQL, PostgreSQL, MSSQL, Oracle)\n");
            sb.append("- Time-based delays in response (SLEEP(), WAITFOR DELAY)\n");
            sb.append("- Boolean-based logic differences (AND 1=1 vs AND 1=2)\n");
            sb.append("- Union-based injection possibilities\n");
            sb.append("- Stacked queries\n");
            sb.append("- Blind injection techniques\n");
        }
        
        if (scanType.equals("XSS") || scanType.equals("ALL")) {
            sb.append("\n**XSS Testing Guidelines:**\n");
            sb.append("Focus on these indicators:\n");
            sb.append("- Reflected input in HTML context without encoding\n");
            sb.append("- JavaScript execution context\n");
            sb.append("- Event handlers (onerror, onload, onmouseover, etc.)\n");
            sb.append("- DOM manipulation possibilities\n");
            sb.append("- Tag-based injections (<script>, <img>, <svg>, <body>, etc.)\n");
            sb.append("- Attribute-based injections (onload=, onerror=)\n");
        }
        
        if (scanType.equals("IDOR") || scanType.equals("ALL")) {
            sb.append("\n**IDOR Testing Guidelines:**\n");
            sb.append("Focus on these indicators:\n");
            sb.append("- Sequential IDs in URLs or parameters\n");
            sb.append("- Predictable resource identifiers (UUIDs, emails, usernames)\n");
            sb.append("- Missing authorization checks\n");
            sb.append("- Response contains data of other users\n");
            sb.append("- Access control bypass attempts\n");
        }
        
        if (scanType.equals("SSRF") || scanType.equals("ALL")) {
            sb.append("\n**SSRF Testing Guidelines:**\n");
            sb.append("Focus on these indicators:\n");
            sb.append("- URL parameters accepting external addresses\n");
            sb.append("- Internal IP addresses in responses\n");
            sb.append("- Cloud metadata endpoints accessibility\n");
            sb.append("- DNS resolution behavior changes\n");
            sb.append("- File scheme access (file://)\n");
            sb.append("- Internal service discovery\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Build observation prompt for subsequent iterations
     */
    public String buildObservationPrompt(String previousThoughts, 
                                         String observation, 
                                         int iteration) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("**Previous Thoughts:**\n");
        sb.append(previousThoughts).append("\n\n");
        
        sb.append("**Observation #").append(iteration).append(":**\n");
        sb.append(observation).append("\n\n");
        
        sb.append("**Analysis Hint:**\n");
        sb.append("Compare this response with the original baseline response.\n");
        sb.append("Look for:\n");
        sb.append("- Error messages or stack traces\n");
        sb.append("- Timing differences (for blind injection)\n");
        sb.append("- Content length changes (for boolean-based attacks)\n");
        sb.append("- Reflected payloads (for XSS/injection)\n");
        sb.append("- File upload acceptance/rejection patterns\n");
        sb.append("- Status code changes\n\n");
        
        sb.append("Continue your analysis or provide a final conclusion.");
        
        return sb.toString();
    }
}
