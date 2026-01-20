package com.burpai.agent.core;

import burp.api.montoya.http.message.HttpRequest;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.requests.HttpRequestBody;
import com.burpai.agent.http.HTTPClientManager;
import com.burpai.agent.payloads.FileUploadPayloadGenerator;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Request Modifier
 * 
 * Modifies HTTP requests based on LLM instructions, including special handling
 * for file uploads with professional payload generation.
 */
public class RequestModifier {
    
    private final HTTPClientManager httpClientManager;
    private int currentFileUploadPayloadIndex = 0;
    
    public RequestModifier(HTTPClientManager httpClientManager) {
        this.httpClientManager = httpClientManager;
    }
    
    /**
     * Modify request based on modification specification
     */
    public HttpRequest modifyRequest(HttpRequest originalRequest, JsonObject modification) {
        String parameter = modification.has("parameter") ? 
                modification.get("parameter").getAsString() : null;
        String type = modification.has("type") ? 
                modification.get("type").getAsString() : "";
        String value = modification.has("value") ? 
                modification.get("value").getAsString() : "";
        String encoding = modification.has("encoding") ? 
                modification.get("encoding").getAsString() : "none";
        
        // Apply encoding if needed
        value = applyEncoding(value, encoding);
        
        // Handle file uploads specially
        if (type.equals("file_upload") || type.equals("multipart")) {
            return modifyFileUpload(originalRequest, parameter, value);
        }
        
        // Handle other parameter types
        switch (type) {
            case "url_query":
                return httpClientManager.modifyQueryParam(originalRequest, parameter, value);
                
            case "url_path":
                return modifyPathParam(originalRequest, value);
                
            case "post_body":
                return httpClientManager.modifyFormParam(originalRequest, parameter, value);
                
            case "json":
                return httpClientManager.modifyJsonParam(originalRequest, parameter, value);
                
            case "header":
                return httpClientManager.modifyHeader(originalRequest, parameter, value);
                
            case "cookie":
                return modifyCookie(originalRequest, parameter, value);
                
            case "raw_body":
                return httpClientManager.modifyBody(originalRequest, value);
                
            default:
                return originalRequest;
        }
    }
    
    /**
     * Modify file upload request with professional payloads
     */
    private HttpRequest modifyFileUpload(HttpRequest originalRequest, String paramName, String content) {
        String contentType = originalRequest.headerValue("Content-Type");
        
        // If content is not provided, generate a professional payload
        if (content == null || content.isEmpty() || content.equals("auto")) {
            List<FileUploadPayloadGenerator.FileUploadPayload> allPayloads = 
                    FileUploadPayloadGenerator.generateAllPayloads();
            
            if (currentFileUploadPayloadIndex < allPayloads.size()) {
                FileUploadPayloadGenerator.FileUploadPayload payload = 
                        allPayloads.get(currentFileUploadPayloadIndex);
                
                return constructMultipartRequest(originalRequest, paramName, 
                        payload.getFilename(), payload.getContentType(), 
                        payload.getContent());
            } else {
                // Reset to first payload
                currentFileUploadPayloadIndex = 0;
                FileUploadPayloadGenerator.FileUploadPayload payload = 
                        allPayloads.get(0);
                
                return constructMultipartRequest(originalRequest, paramName, 
                        payload.getFilename(), payload.getContentType(), 
                        payload.getContent());
            }
        } else {
            // Use provided content with a default filename
            String filename = paramName != null && !paramName.isEmpty() ? 
                    paramName + ".php" : "upload.php";
            
            return constructMultipartRequest(originalRequest, paramName, 
                    filename, "application/x-php", content);
        }
    }
    
    /**
     * Construct a multipart/form-data request
     */
    private HttpRequest constructMultipartRequest(HttpRequest originalRequest, 
                                                  String paramName, 
                                                  String filename, 
                                                  String contentType, 
                                                  String content) {
        // Generate boundary
        String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().substring(0, 16);
        
        // Build multipart body
        StringBuilder multipartBody = new StringBuilder();
        
        // Add the file parameter
        multipartBody.append("--").append(boundary).append("\r\n");
        multipartBody.append("Content-Disposition: form-data; name=\"");
        multipartBody.append(paramName != null && !paramName.isEmpty() ? paramName : "file");
        multipartBody.append("\"; filename=\"").append(filename).append("\"\r\n");
        multipartBody.append("Content-Type: ").append(contentType).append("\r\n");
        multipartBody.append("\r\n");
        multipartBody.append(content).append("\r\n");
        
        // Add other form parameters from original request if they exist
        List<HttpParameter> params = originalRequest.parameters();
        for (HttpParameter param : params) {
            if (param.type() == HttpParameterType.BODY) {
                multipartBody.append("--").append(boundary).append("\r\n");
                multipartBody.append("Content-Disposition: form-data; name=\"");
                multipartBody.append(param.name()).append("\"\r\n");
                multipartBody.append("\r\n");
                multipartBody.append(param.value()).append("\r\n");
            }
        }
        
        // End boundary
        multipartBody.append("--").append(boundary).append("--\r\n");
        
        // Update content type header
        String newContentType = "multipart/form-data; boundary=" + boundary;
        
        HttpRequest modified = originalRequest.withHeader("Content-Type", newContentType);
        modified = modified.withBody(HttpRequestBody.body(multipartBody.toString()));
        
        // Update content length
        modified = modified.withHeader("Content-Length", 
                String.valueOf(multipartBody.toString().getBytes(StandardCharsets.UTF_8).length));
        
        return modified;
    }
    
    /**
     * Modify URL path parameter
     */
    private HttpRequest modifyPathParam(HttpRequest originalRequest, String newValue) {
        String url = originalRequest.url();
        // Simple path modification - replace last path segment
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash > 0) {
            String newUrl = url.substring(0, lastSlash + 1) + newValue;
            return originalRequest.withUpdatedUrl(newUrl);
        }
        return originalRequest;
    }
    
    /**
     * Modify cookie parameter
     */
    private HttpRequest modifyCookie(HttpRequest originalRequest, String cookieName, String newValue) {
        String cookieHeader = originalRequest.headerValue("Cookie");
        
        if (cookieHeader != null) {
            // Update existing cookie
            String[] cookies = cookieHeader.split(";\\s*");
            for (int i = 0; i < cookies.length; i++) {
                String[] kv = cookies[i].split("=", 2);
                if (kv.length == 2 && kv[0].trim().equals(cookieName)) {
                    cookies[i] = cookieName + "=" + newValue;
                    break;
                }
            }
            String newCookieHeader = String.join("; ", cookies);
            return originalRequest.withHeader("Cookie", newCookieHeader);
        } else {
            // Add new cookie
            return originalRequest.withAddedHeader("Cookie", cookieName + "=" + newValue);
        }
    }
    
    /**
     * Apply encoding to value
     */
    private String applyEncoding(String value, String encoding) {
        if (value == null || encoding.equals("none")) {
            return value;
        }
        
        switch (encoding) {
            case "url_encode":
                try {
                    return java.net.URLEncoder.encode(value, "UTF-8");
                } catch (Exception e) {
                    return value;
                }
                
            case "url_decode":
                try {
                    return java.net.URLDecoder.decode(value, "UTF-8");
                } catch (Exception e) {
                    return value;
                }
                
            case "base64":
                return java.util.Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
                
            case "base64_decode":
                try {
                    return new String(java.util.Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    return value;
                }
                
            case "html_entity":
                return value.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\"", "&quot;")
                        .replace("'", "&#39;");
                
            default:
                return value;
        }
    }
    
    /**
     * Get human-readable description of modification
     */
    public String getModificationDescription(JsonObject modification) {
        StringBuilder desc = new StringBuilder();
        
        String type = modification.has("type") ? 
                modification.get("type").getAsString() : "";
        String parameter = modification.has("parameter") ? 
                modification.get("parameter").getAsString() : "";
        String value = modification.has("value") ? 
                modification.get("value").getAsString() : "";
        
        desc.append("Type: ").append(type);
        
        if (type.equals("file_upload") || type.equals("multipart")) {
            desc.append(", Parameter: ").append(parameter != null ? parameter : "file");
            if (value.isEmpty() || value.equals("auto")) {
                desc.append(", Payload: Generated #").append(currentFileUploadPayloadIndex + 1);
                currentFileUploadPayloadIndex++;
            } else {
                desc.append(", Custom content");
            }
        } else {
            if (!parameter.isEmpty()) {
                desc.append(", Parameter: ").append(parameter);
            }
            if (!value.isEmpty()) {
                String displayValue = value.length() > 50 ? 
                        value.substring(0, 50) + "..." : value;
                desc.append(", Value: ").append(displayValue);
            }
        }
        
        return desc.toString();
    }
    
    /**
     * Reset file upload payload index
     */
    public void resetFileUploadPayloadIndex() {
        currentFileUploadPayloadIndex = 0;
    }
}
