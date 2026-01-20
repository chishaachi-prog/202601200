package com.burpai.agent.utils;

import burp.api.montoya.MontoyaApi;

/**
 * Logger
 * 
 * Provides logging functionality for the extension.
 */
public class Logger {
    
    private static MontoyaApi api;
    private static final String PREFIX = "[BurpAI] ";
    
    public static void init(MontoyaApi api) {
        Logger.api = api;
    }
    
    public static void info(String message) {
        log("INFO", message);
    }
    
    public static void warn(String message) {
        log("WARN", message);
    }
    
    public static void error(String message) {
        log("ERROR", message);
    }
    
    public static void debug(String message) {
        log("DEBUG", message);
    }
    
    private static void log(String level, String message) {
        if (api != null) {
            api.logging().logToOutput(PREFIX + "[" + level + "] " + message);
        }
        // Also print to stdout for debugging
        System.out.println(PREFIX + "[" + level + "] " + message);
    }
}
