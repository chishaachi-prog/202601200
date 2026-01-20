# 产品需求文档 (PRD): BurpAI Agent - 智能交互式漏洞扫描插件

## 1. 项目简介 (Project Overview)

**项目名称**：BurpAI Agent

**项目目标**：开发一款基于 Burp Suite 的扩展插件，集成大语言模型（LLM）的推理能力。它不只是静态分析，而是作为一个 **AI Agent（智能体）**，具备"观察-思考-行动-验证"的闭环能力。

**核心价值**：通过多轮对话迭代,模拟人类渗透测试专家的逻辑，自动生成 Payload、分析响应、绕过防御，实现深度漏洞挖掘。

---

## 2. 系统架构与界面设计 (UI/UX)

插件界面主要分为两个部分：**配置中心 (Configuration)** 和 **任务仪表盘 (Dashboard)**。

### 2.1 配置中心 (Configuration Tab)

界面采用 Tab 分页布局，包含以下三个子页面：

#### Tab 1: AI 引擎设置 (Model & Engine)

负责管理与 LLM 的连接。

* **Provider (下拉框)**: 支持 OpenAI, Anthropic, Azure OpenAI, Local (Ollama/LocalAI)。
* **API Key (输入框)**: 掩码显示，提供"验证连接"按钮。
* **Model Name (输入框/下拉)**: 如 `gpt-4o`, `claude-3-5-sonnet`。
* **Base URL (输入框)**: 用于自定义代理地址或本地 API 地址 (如 `http://localhost:11434/v1`)。

#### Tab 2: 范围与过滤 (Target & Scope)

负责流量清洗，防止误扫和 Token 浪费。

* **Scope Mode**:
  * 🔘 **Use Burp Suite Scope**: 严格跟随 Burp 全局 Target 设置。
  * 🔘 **Custom Scope**: 使用插件独立配置。

* **Host Filter (仅 Custom 模式有效)**:
  * **Include Hosts (白名单)**: 支持通配符 (e.g., `*.test.com`)。
  * **Exclude Hosts (黑名单)**: 优先级最高 (e.g., `logout.test.com`, `analytics.google.com`)。

* **Extension Filter (后缀黑名单)**:
  * **默认开启**: `jpg, png, gif, css, js, woff, svg, ico, pdf`。
  * **自定义**: 允许用户添加/删除后缀。逻辑：以此结尾的 URL **绝不** 发送给 AI。

#### Tab 3: 扫描策略 (Scan Policy)

负责控制 Agent 的行为逻辑。

* **漏洞检测开关 (Checkboxes)**:
  * [x] SQL Injection
  * [x] XSS (Reflected/Stored)
  * [x] Broken Access Control (越权)
  * [x] SSRF
  * [ ] RCE (默认关闭，高危)
  * [ ] Business Logic (业务逻辑)

* **Agent 行为参数**:
  * **Max Iterations (最大迭代次数)**: 整数 (1-10)，默认 5。限制 AI 自主尝试的轮数。
  * **Confidence Level**: Low / Medium / High (影响 AI 判定漏洞的严格程度)。

### 2.2 任务仪表盘 (Dashboard Tab)

用于展示扫描进度和 AI 的思维过程。

* **左侧：任务列表 (Task List)**
  * 显示 `ID`, `Method`, `URL`, `Status` (Running/Finished), `Vuln Found` (Yes/No)。

* **右侧：交互详情 (Chat View)**
  * 类似 ChatGPT 的对话界面。
  * **思维链展示**:
    * 🟢 **AI Thought**: "参数 `id` 疑似数字型注入，准备尝试单引号报错。"
    * 🔵 **System Action**: "Sending Request: GET /api?id=1'"
    * 🟠 **Observation**: "收到 500 错误，包含 MySQL 关键字。"
    * 🔴 **Result**: "确认为 SQL 注入漏洞。"

---

## 3. 核心功能流程 (Functional Logic)

### 3.1 流量入口与触发机制

#### A. Repeater 模块 (主动触发)

* **操作**: 用户在 Request 编辑区 -> 右键菜单 -> `Extensions` -> `BurpAI Agent`。
* **子菜单**:
  * `Auto Analysis (All Types)`: 根据配置开启所有选中的漏洞类型进行检测。
  * `Specific: SQL Injection`: 强制仅检测 SQL 注入。
  * `Specific: IDOR`: 强制仅检测越权。
  * `Custom Prompt`: 弹出输入框，用户输入特定指令 (如 "帮我看看这个加密参数")。

#### B. Proxy 模块 (被动监听)

* **逻辑**: 插件作为 `IHttpListener` 挂载。
* **流式处理**:
  1. 捕获请求 -> 2. 检查后缀过滤 (Pass/Drop) -> 3. 检查 Host Scope (Pass/Drop) -> 4. 放入分析队列。

* **防抖动**: 同一 URL + 参数组合，在 N 分钟内不重复扫描。

### 3.2 AI Agent 核心循环 (The ReAct Loop)

这是插件的大脑，必须严格执行以下循环：

1. **初始化 (Init)**: 将原始 HTTP 请求包 + 用户配置的漏洞类型 + System Prompt 发送给 LLM。

2. **第一轮分析 (Analysis)**:
   * LLM 分析请求结构，判断是否存在可疑参数。
   * 如果认为安全 -> 输出 "No Vulnerability" -> **结束**。
   * 如果认为可疑 -> 生成测试计划 -> 输出 **Action JSON**。

3. **动作执行 (Execution)**:
   * 插件解析 JSON，提取 `payload` 和 `injection_point`。
   * 调用 Burp API (`makeHttpRequest`) 发送修改后的请求。
   * 捕获新的响应数据。

4. **反思与修正 (Reflection)**:
   * 将 **新响应** (截断后的 Body + Status Code) 喂回给 LLM。
   * LLM 判断攻击是否成功。
   * **未成功但有希望**: 生成绕过 Payload (如 WAF 绕过) -> **回到步骤 3**。
   * **已成功**: 输出漏洞证据 -> **结束**。
   * **完全失败**: 尝试次数 > Max Iterations -> **结束**。

---

## 4. 数据协议与交互标准

### 4.1 System Prompt 模板 (简化版)

```text
You are a penetration testing expert. You have permission to test this target.
Target URL: {url}
Scan Types: {scan_types}

Rules:
1. Analyze the request/response.
2. If you need to test, output a JSON object with "action": "scan".
3. Do NOT provide general advice, only technical actions.
4. Response Format must be strictly JSON.
```

### 4.2 LLM 输出协议 (JSON Schema)

强制 LLM 返回标准 JSON，以便插件代码解析。

**请求行动 (Action):**

```json
{
  "thought": "The 'id' parameter looks susceptible to SQLi. I will try a boolean inference.",
  "action": "send_request",
  "request_modification": {
    "parameter": "id",
    "type": "url_query",
    "value": "1' AND 1=1--"
  }
}
```

**最终结论 (Final Result):**

```json
{
  "thought": "The server responded with different content lengths for true/false payloads.",
  "action": "finish",
  "vulnerability_found": true,
  "vulnerability_type": "SQL Injection",
  "severity": "High",
  "evidence": "Content-Length difference: 500 vs 200"
}
```

---

## 5. 技术约束与性能要求

### 5.1 Token 优化

* **Body 截断**: 对于 HTTP Response Body，仅保留前 2KB 或 5KB 数据，防止 Token 溢出。
* **Header 清洗**: 移除 `Cookie` (部分)、`User-Agent` 等对分析无关的字段，除非是测试目标。

### 5.2 并发控制

* 设置独立的线程池 (Thread Pool) 处理 AI 任务，防止阻塞 Burp 主界面。
* 并发数限制：建议默认为 1-3 个并发任务。

### 5.3 安全性

* **敏感数据**: API Key 必须保存到 Burp 的 `loadExtensionSetting` 中，尽量不落盘明文。
* **死循环熔断**: 即使 AI 要求继续，一旦达到 Max Iterations，插件强制终止任务。

---

## 6. 开发路线图 (Roadmap)

### 阶段一 (MVP)
* 实现配置界面 (仅 API Key 和 Model)。
* 实现 Repeater 右键菜单。
* 实现单轮对话：发送请求 -> AI 分析 -> 显示结果 (无自动发包)。

### 阶段二 (Agent Alpha)
* 实现自动发包功能 (`makeHttpRequest`)。
* 实现 JSON 解析与多轮迭代逻辑。
* 实现 Dashboard 界面展示思维链。

### 阶段三 (Scope & Polish)
* 完善过滤逻辑 (Host/Suffix)。
* 增加漏洞类型开关。
* 流式 Proxy 监听支持。

---

## 附录

### A. 支持的漏洞类型

| 漏洞类型 | 默认启用 | 风险等级 | 说明 |
|---------|---------|---------|------|
| SQL Injection | ✅ | High | 数据库注入攻击 |
| XSS (Reflected/Stored) | ✅ | Medium-High | 跨站脚本攻击 |
| Broken Access Control (IDOR) | ✅ | Medium-High | 越权访问 |
| SSRF | ✅ | High | 服务器端请求伪造 |
| RCE | ❌ | Critical | 远程代码执行（高危，默认关闭）|
| Business Logic | ❌ | Varies | 业务逻辑漏洞 |

### B. 系统要求

* Burp Suite Professional 2023.x 或更高版本
* Java 17 或更高版本
* 网络连接（用于访问 LLM API）
* 建议内存：至少 4GB RAM

### C. 配置示例

#### OpenAI 配置
```json
{
  "provider": "OpenAI",
  "api_key": "sk-xxx",
  "model": "gpt-4o",
  "base_url": "https://api.openai.com/v1"
}
```

#### 本地模型配置 (Ollama)
```json
{
  "provider": "Local",
  "api_key": "not_required",
  "model": "llama3",
  "base_url": "http://localhost:11434/v1"
}
```
