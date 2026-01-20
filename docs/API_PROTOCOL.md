# API 协议规范

本文档定义了 BurpAI Agent 与大语言模型（LLM）之间的交互协议和数据格式标准。

## 1. 协议概述

### 1.1 设计原则
- **结构化输出**：强制 LLM 返回标准 JSON 格式
- **最小化 Token**：仅传递关键信息给 LLM
- **可扩展性**：支持新增漏洞类型和动作类型
- **容错性**：处理 LLM 输出格式错误的情况

### 1.2 交互流程
```
插件 → [System Prompt + HTTP Request] → LLM
       ↓
LLM → [JSON Response] → 插件解析
       ↓
插件 → [修改后的请求] → 目标服务器
       ↓
目标服务器 → [HTTP Response] → 插件
       ↓
插件 → [Observation + 历史上下文] → LLM
       ↓
(循环直到 LLM 返回 "finish" 或达到最大迭代次数)
```

## 2. System Prompt 模板

### 2.1 通用模板

```text
You are an expert penetration tester with permission to test the target system.
Your task is to analyze HTTP requests and identify security vulnerabilities.

**Target Information:**
- URL: {url}
- Method: {method}
- Content-Type: {content_type}

**Scan Types Enabled:**
{scan_types_list}

**Instructions:**
1. Analyze the HTTP request and response carefully.
2. Identify potentially vulnerable parameters or injection points.
3. If you want to perform a test, output a JSON object with "action": "send_request".
4. If you believe you have found a vulnerability, output "action": "finish" with evidence.
5. If the target appears secure after your analysis, output "action": "finish" with vulnerability_found: false.
6. Do NOT provide explanations outside the JSON structure.
7. Be precise and technical in your analysis.

**Output Format (MUST be valid JSON):**
{
  "thought": "Your reasoning process",
  "action": "send_request" | "finish",
  "request_modification": { ... },  // Only if action is "send_request"
  "vulnerability_found": true | false,  // Only if action is "finish"
  "vulnerability_type": "...",  // Only if vulnerability found
  "severity": "Low|Medium|High|Critical",  // Only if vulnerability found
  "evidence": "..."  // Only if vulnerability found
}

**Original Request:**
{http_request}

**Previous Observations:**
{observations}
```

### 2.2 漏洞类型特定提示

#### SQL Injection
```text
Focus on these indicators:
- Error messages containing database keywords (MySQL, PostgreSQL, MSSQL)
- Time-based delays in response
- Boolean-based logic differences
- Union-based injection possibilities
```

#### XSS
```text
Focus on these indicators:
- Reflected input in HTML context
- JavaScript execution context
- Event handlers (onerror, onload, etc.)
- DOM manipulation possibilities
```

#### IDOR (Broken Access Control)
```text
Focus on these indicators:
- Sequential IDs in URLs or parameters
- Predictable resource identifiers
- Missing authorization checks
- Response contains data of other users
```

#### SSRF
```text
Focus on these indicators:
- URL parameters accepting external addresses
- Internal IP addresses in responses
- Cloud metadata endpoints accessibility
- DNS resolution behavior changes
```

## 3. LLM 输出协议

### 3.1 动作类型

#### 3.1.1 发送请求 (send_request)

用于指示插件发送修改后的 HTTP 请求。

**JSON Schema:**
```json
{
  "thought": "string (required) - AI 的推理过程",
  "action": "send_request",
  "request_modification": {
    "parameter": "string (required) - 要修改的参数名",
    "type": "url_query | url_path | post_body | header | cookie",
    "value": "string (required) - 新的参数值（Payload）",
    "encoding": "none | url_encode | base64 | html_entity (optional)"
  }
}
```

**示例 1: SQL 注入测试**
```json
{
  "thought": "Parameter 'id' appears to be numeric and directly used in SQL query. Testing for error-based injection.",
  "action": "send_request",
  "request_modification": {
    "parameter": "id",
    "type": "url_query",
    "value": "1' OR '1'='1",
    "encoding": "url_encode"
  }
}
```

**示例 2: XSS 测试**
```json
{
  "thought": "Parameter 'search' is reflected in the HTML response without proper encoding. Testing for XSS.",
  "action": "send_request",
  "request_modification": {
    "parameter": "search",
    "type": "url_query",
    "value": "<script>alert(1)</script>",
    "encoding": "none"
  }
}
```

**示例 3: Header 注入**
```json
{
  "thought": "Testing for Host header injection to check SSRF potential.",
  "action": "send_request",
  "request_modification": {
    "parameter": "Host",
    "type": "header",
    "value": "internal.server.local",
    "encoding": "none"
  }
}
```

#### 3.1.2 结束分析 (finish)

用于指示分析完成，输出最终结论。

**JSON Schema:**
```json
{
  "thought": "string (required) - 最终分析结论",
  "action": "finish",
  "vulnerability_found": "boolean (required)",
  "vulnerability_type": "string (required if vulnerability_found=true)",
  "severity": "Low | Medium | High | Critical (required if vulnerability_found=true)",
  "evidence": "string (required if vulnerability_found=true) - 漏洞证据",
  "remediation": "string (optional) - 修复建议"
}
```

**示例 1: 发现 SQL 注入**
```json
{
  "thought": "The application returned different response times for true/false conditions, confirming time-based blind SQL injection.",
  "action": "finish",
  "vulnerability_found": true,
  "vulnerability_type": "SQL Injection (Time-based Blind)",
  "severity": "High",
  "evidence": "Response time: 5.2s for payload \"1' AND SLEEP(5)--\", normal time: 0.3s",
  "remediation": "Use parameterized queries (prepared statements) to prevent SQL injection."
}
```

**示例 2: 未发现漏洞**
```json
{
  "thought": "After testing multiple SQL injection payloads, the application properly escapes all inputs. No vulnerability detected.",
  "action": "finish",
  "vulnerability_found": false
}
```

**示例 3: 发现 IDOR**
```json
{
  "thought": "Successfully accessed user ID 1002's data by modifying the 'userId' parameter from 1001.",
  "action": "finish",
  "vulnerability_found": true,
  "vulnerability_type": "IDOR (Insecure Direct Object Reference)",
  "severity": "High",
  "evidence": "Original request returned user 'john@example.com', modified request returned user 'admin@example.com' with userId=1",
  "remediation": "Implement proper authorization checks to ensure users can only access their own data."
}
```

### 3.2 观察结果格式

插件在每次 HTTP 请求后，将响应数据以下格式发送回 LLM。

**格式:**
```text
**Observation #{iteration}:**
- Status Code: {status_code}
- Response Time: {response_time}ms
- Content-Length: {content_length}
- Content-Type: {content_type}

**Response Headers (filtered):**
{filtered_headers}

**Response Body (truncated to 2KB):**
{truncated_body}

**Analysis Hint:**
Compare this response with the original baseline response.
Look for:
- Error messages or stack traces
- Timing differences (for blind injection)
- Content length changes (for boolean-based attacks)
- Reflected payloads (for XSS/injection)
```

**示例:**
```text
**Observation #2:**
- Status Code: 500 Internal Server Error
- Response Time: 234ms
- Content-Length: 1024
- Content-Type: text/html

**Response Headers (filtered):**
Server: Apache/2.4.41
X-Powered-By: PHP/7.4.3

**Response Body (truncated to 2KB):**
<html>
<body>
<h1>Database Error</h1>
<pre>
You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near ''1' OR '1'='1' at line 1
</pre>
</body>
</html>

**Analysis Hint:**
The error message explicitly reveals MySQL syntax error, confirming SQL injection vulnerability.
```

## 4. 错误处理

### 4.1 LLM 输出格式错误

如果 LLM 返回的不是有效 JSON，插件应：

1. 记录原始响应到日志
2. 尝试提取 JSON 片段（正则匹配 `{...}`）
3. 如果仍然失败，重新提示 LLM：

```text
**ERROR:** Your previous response was not valid JSON. Please output ONLY a valid JSON object.

Required format:
{
  "thought": "...",
  "action": "send_request" or "finish",
  ...
}

Original request context:
{original_context}
```

### 4.2 不支持的动作类型

如果 LLM 返回未知的 `action` 值，插件应：

```json
{
  "error": "Unsupported action type",
  "received_action": "{unknown_action}",
  "supported_actions": ["send_request", "finish"]
}
```

### 4.3 必填字段缺失

如果必填字段缺失，插件应提示：

```text
**ERROR:** Missing required field in your response.

Missing field: {field_name}

Please provide a complete JSON response with all required fields.
```

## 5. Token 优化策略

### 5.1 请求体截断
- **GET/HEAD 请求**: 仅发送 URL 和关键 Headers
- **POST/PUT 请求**: 
  - JSON Body: 保留完整结构（最大 2KB）
  - Form Data: 保留所有参数名和值
  - Multipart: 仅保留字段名，不发送文件内容

### 5.2 响应体截断
- **默认**: 前 2KB
- **HTML 响应**: 提取 `<body>` 内容的前 2KB
- **JSON 响应**: 尝试保留完整 JSON 结构，如果超出则截断
- **二进制响应**: 仅发送 Content-Type 和大小

### 5.3 Header 过滤
**发送给 LLM 的 Headers（白名单）:**
- Content-Type
- Content-Length
- Authorization (如果测试对象)
- Cookie (仅参数名，不发送完整值)
- Set-Cookie
- Location
- Server
- X-* (自定义头)

**过滤掉的 Headers:**
- User-Agent (除非是测试目标)
- Accept-Encoding
- Connection
- Cache-Control
- 其他标准浏览器头

## 6. 版本控制

当前协议版本: **v1.0**

未来可能的扩展：
- 支持多步骤测试计划（一次返回多个 Payload）
- 支持自定义验证脚本（如 Python 代码片段）
- 支持链式漏洞组合（如 XSS + CSRF）

## 7. 安全考虑

### 7.1 Payload 验证
- 插件应验证 LLM 生成的 Payload 长度（最大 10KB）
- 检查是否包含明显的破坏性命令（如 `rm -rf`）
- 对于 RCE 类型，默认禁用或需要用户确认

### 7.2 速率限制
- 单个任务最多 10 次迭代
- 单次 LLM 调用超时: 30 秒
- 单个任务总超时: 5 分钟

### 7.3 数据隐私
- 不向 LLM 发送真实的 API Key 或密码（除非用户明确配置）
- 敏感字段（如 `password`, `token`）在发送前进行脱敏处理
