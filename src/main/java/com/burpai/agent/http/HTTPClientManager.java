package com.burpai.agent.http;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequest;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.requests.HttpRequestBody;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP Client Manager
 * 
 * Handles HTTP request modification, sending, and response capture.
 */
public class HTTPClientManager {
    
    private static final int MAX_BODY_LENGTH = 2048; // 2KB
    
    private final MontoyaApi api;
    
    public HTTPClientManager(MontoyaApi api) {
        this.api = api;
    }
    
    /**
     * Send an HTTP request and capture the response
     */
    public HTTPResponse sendRequest(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            burp.api.montoya.http.message.HttpResponse burpResponse = 
                    api.http().sendRequest(request);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return new HTTPResponse(
                    burpResponse.statusCode(),
                    responseTime,
                    burpResponse.headers().toString(),
                    truncateBody(burpResponse.bodyToString()),
                    burpResponse.body().length()
            );
            
        } catch (Exception e) {
            return HTTPResponse.error("Failed to send request: " + e.getMessage());
        }
    }
    
    /**
     * Modify a URL query parameter
     */
    public HttpRequest modifyQueryParam(HttpRequest originalRequest, String paramName, String newValue) {
        List<HttpParameter> params = originalRequest.parameters();
        
        // Check if parameter exists
        for (HttpParameter param : params) {
            if (param.type() == HttpParameterType.QUERY 
                    && param.name().equals(paramName)) {
                // Update existing parameter
                return originalRequest.withParameter(param.withValue(newValue));
            }
        }
        
        // Add new parameter if not exists
        HttpParameter newParam = HttpParameter.parameter(
                paramName, 
                newValue, 
                HttpParameterType.QUERY
        );
        return originalRequest.withAddedParameter(newParam);
    }
    
    /**
     * Modify a form body parameter
     */
    public HttpRequest modifyFormParam(HttpRequest originalRequest, String paramName, String newValue) {
        List<HttpParameter> params = originalRequest.parameters();
        
        // Check if parameter exists
        for (HttpParameter param : params) {
            if (param.type() == HttpParameterType.BODY 
                    && param.name().equals(paramName)) {
                // Update existing parameter
                return originalRequest.withParameter(param.withValue(newValue));
            }
        }
        
        // Add new parameter if not exists
        HttpParameter newParam = HttpParameter.parameter(
                paramName, 
                newValue, 
                HttpParameterType.BODY
        );
        return originalRequest.withAddedParameter(newParam);
    }
    
    /**
     * Modify a JSON body parameter
     */
    public HttpRequest modifyJsonParam(HttpRequest originalRequest, String paramName, String newValue) {
        String body = originalRequest.bodyToString();
        
        // Simple JSON modification using regex
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(paramName) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(body);
        
        if (matcher.find()) {
            String newBody = matcher.replaceFirst("\"" + paramName + "\": \"" + escapeJson(newValue) + "\"");
            return originalRequest.withBody(HttpRequestBody.body(newBody));
        }
        
        // Handle numeric values
        pattern = Pattern.compile("\"" + Pattern.quote(paramName) + "\"\\s*:\\s*(\\d+)");
        matcher = pattern.matcher(body);
        
        if (matcher.find()) {
            String newBody = matcher.replaceFirst("\"" + paramName + "\": " + newValue);
            return originalRequest.withBody(HttpRequestBody.body(newBody));
        }
        
        return originalRequest;
    }
    
    /**
     * Modify a header value
     */
    public HttpRequest modifyHeader(HttpRequest originalRequest, String headerName, String newValue) {
        return originalRequest.withHeader(headerName, newValue);
    }
    
    /**
     * Replace the entire body with new content (for multipart file uploads)
     */
    public HttpRequest modifyBody(HttpRequest originalRequest, String newBody) {
        return originalRequest.withBody(HttpRequestBody.body(newBody));
    }
    
    /**
     * Get formatted request text for LLM analysis
     */
    public String formatRequestForLLM(HttpRequest request) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Method: ").append(request.method()).append("\n");
        sb.append("URL: ").append(request.url()).append("\n\n");
        
        sb.append("Headers:\n");
        sb.append(request.headers()).append("\n\n");
        
        String body = request.bodyToString();
        if (body != null && !body.isEmpty()) {
            sb.append("Body:\n");
            sb.append(truncateBody(body)).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Truncate body to max length
     */
    private String truncateBody(String body) {
        if (body == null || body.length() <= MAX_BODY_LENGTH) {
            return body;
        }
        return body.substring(0, MAX_BODY_LENGTH) + "... [TRUNCATED]";
    }
    
    /**
     * Escape special characters in JSON string
     */
    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    /**
     * HTTP Response Model
     */
    public static class HTTPResponse {
        private final int statusCode;
        private final long responseTime;
        private final String headers;
        private final String body;
        private final int contentLength;
        private final String errorMessage;
        private final boolean successful;
        
        private HTTPResponse(int statusCode, long responseTime, String headers, String body, int contentLength) {
            this.statusCode = statusCode;
            this.responseTime = responseTime;
            this.headers = headers;
            this.body = body;
            this.contentLength = contentLength;
            this.errorMessage = null;
            this.successful = true;
        }
        
        private HTTPResponse(String errorMessage) {
            this.statusCode = 0;
            this.responseTime = 0;
            this.headers = "";
            this.body = "";
            this.contentLength = 0;
            this.errorMessage = errorMessage;
            this.successful = false;
        }
        
        public static HTTPResponse error(String errorMessage) {
            return new HTTPResponse(errorMessage);
        }
        
        // Getters
        public int getStatusCode() { return statusCode; }
        public long getResponseTime() { return responseTime; }
        public String getHeaders() { return headers; }
        public String getBody() { return body; }
        public int getContentLength() { return contentLength; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isSuccessful() { return successful; }
        
        /**
         * Format response for LLM analysis
         */
        public String formatForLLM() {
            StringBuilder sb = new StringBuilder();
            
            sb.append("Status Code: ").append(statusCode).append("\n");
            sb.append("Response Time: ").append(responseTime).append("ms\n");
            sb.append("Content-Length: ").append(contentLength).append("\n\n");
            
            sb.append("Response Headers:\n");
            sb.append(headers).append("\n\n");
            
            sb.append("Response Body:\n");
            sb.append(body);
            
            return sb.toString();
        }
    }
}
