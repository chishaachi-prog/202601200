# 快速开始 (Quick Start Guide)

本指南将帮助您在 5 分钟内完成 BurpAI Agent 的安装和配置。

> **注意**: 当前文档面向未来的正式版本。项目目前处于 **需求设计阶段**，实际代码开发尚未开始。

---

## 前置要求

在开始之前，请确保您已准备好：

- ✅ **Burp Suite Professional** (2023.x 或更高版本)
- ✅ **Java 17+** 运行环境
- ✅ **LLM API 访问权限** (以下任一):
  - OpenAI API Key (推荐 GPT-4)
  - Anthropic API Key (Claude)
  - Azure OpenAI 账户
  - 本地 LLM (Ollama/LocalAI)
- ✅ **网络连接** (访问 API 或本地服务)

---

## 步骤 1: 安装插件

### 方法 A: 通过 BApp Store 安装 (推荐)

1. 打开 Burp Suite
2. 进入 **Extender** 标签页
3. 点击 **BApp Store**
4. 搜索 **"BurpAI Agent"**
5. 点击 **Install**

### 方法 B: 手动安装

1. 从 [GitHub Releases](https://github.com/YOUR_USERNAME/burpai-agent/releases) 下载最新的 `burpai-agent.jar`
2. 在 Burp Suite 中打开 **Extender** -> **Extensions**
3. 点击 **Add**
4. Extension Type 选择 **Java**
5. 选择下载的 JAR 文件
6. 点击 **Next**

### 验证安装

成功安装后，您应该看到：
- Extender 列表中出现 **BurpAI Agent**
- 新增了两个标签页: **Configuration** 和 **Dashboard**

---

## 步骤 2: 配置 AI 引擎

### 2.1 打开配置界面

1. 点击顶部的 **BurpAI Agent** 标签
2. 进入 **Configuration** 子标签

### 2.2 选择 LLM 提供商

#### 选项 1: OpenAI (推荐)

```
┌─────────────────────────────────────┐
│ Provider: [OpenAI ▼]                │
│ API Key: [sk-proj-xxxxx...] 🔒      │
│ Model Name: [gpt-4o]                │
│ Base URL: [https://api.openai.com/v1] │
│                                     │
│ [Test Connection]                   │
└─────────────────────────────────────┘
```

**配置说明**:
- **API Key**: 从 [OpenAI Platform](https://platform.openai.com/api-keys) 获取
- **Model Name**: 
  - `gpt-4o` - 最强性能 (推荐)
  - `gpt-4o-mini` - 经济实惠
  - `gpt-3.5-turbo` - 快速响应
- **Base URL**: 保持默认，除非使用代理

#### 选项 2: Anthropic Claude

```
Provider: [Anthropic ▼]
API Key: [sk-ant-xxxxx...]
Model Name: [claude-3-5-sonnet-20241022]
Base URL: [https://api.anthropic.com]
```

#### 选项 3: 本地模型 (Ollama)

```
Provider: [Local ▼]
API Key: [留空或填 "not_required"]
Model Name: [llama3.1]
Base URL: [http://localhost:11434/v1]
```

**本地模型配置步骤**:
1. 安装 Ollama: https://ollama.ai
2. 下载模型: `ollama pull llama3.1`
3. 启动服务: `ollama serve`
4. 在插件中填写上述配置

### 2.3 测试连接

1. 填写完配置后，点击 **Test Connection**
2. 等待 2-5 秒
3. 看到 ✅ **"Connection Successful!"** 表示配置正确

**常见错误**:
- ❌ "Invalid API Key" → 检查 API Key 是否正确
- ❌ "Network Error" → 检查网络连接和防火墙
- ❌ "Model Not Found" → 确认 Model Name 拼写正确

### 2.4 保存配置

点击右下角的 **Save** 按钮，配置会自动保存到 Burp 的持久化存储中。

---

## 步骤 3: 配置扫描范围 (可选)

### 3.1 选择 Scope 模式

进入 **Configuration** -> **Target & Scope** 子标签:

```
🔘 Use Burp Suite Scope (推荐)
   跟随 Burp 全局 Target 设置

🔘 Custom Scope
   使用独立的过滤规则
```

**建议**: 首次使用选择 **Use Burp Suite Scope**，这样只扫描您已经添加到 Burp Target 的域名。

### 3.2 配置扩展名过滤 (避免扫描静态资源)

默认已排除常见静态文件，您可以自定义:

```
Extension Filter (后缀黑名单):
[jpg, png, gif, css, js, woff, svg, ico, pdf]

[+ Add]  [- Remove]
```

**说明**: 以这些后缀结尾的 URL 不会发送给 AI 分析，节省 Token 和时间。

---

## 步骤 4: 配置扫描策略

进入 **Configuration** -> **Scan Policy** 子标签:

### 4.1 选择漏洞类型

```
漏洞检测开关:
☑ SQL Injection
☑ XSS (Reflected/Stored)
☑ Broken Access Control (IDOR)
☑ SSRF
☐ RCE (Remote Code Execution) ⚠️ 高危
☐ Business Logic
```

**建议**: 首次使用保持默认设置（前 4 项开启）。

### 4.2 调整 Agent 参数

```
Max Iterations: [5] (1-10)
   AI 最多尝试的轮数

Confidence Level: ◉ Medium ○ Low ○ High
   Low:  更激进，可能误报
   Medium: 平衡 (推荐)
   High:  保守，只报高置信度漏洞
```

### 4.3 保存设置

点击 **Save** 完成配置。

---

## 步骤 5: 开始第一次扫描

### 方法 A: 从 Repeater 触发 (主动扫描)

1. 在 Burp Suite 中切换到 **Repeater** 标签
2. 准备一个 HTTP 请求 (或从 Proxy 历史中发送到 Repeater)
3. 在请求区域 **右键点击**
4. 选择 **Extensions** -> **BurpAI Agent** -> **Auto Analysis (All Types)**
5. 等待分析完成

**示例流程**:
```
Repeater 中的请求:
GET /api/user?id=1001 HTTP/1.1
Host: example.com

↓ (右键 -> BurpAI Agent -> Auto Analysis)

插件开始分析...
→ AI 正在思考...
→ 发送测试请求...
→ 分析响应...

结果显示在 Dashboard 中
```

### 方法 B: 从 Proxy 被动监听 (自动扫描)

1. 确保 Burp Proxy 正在拦截流量
2. 浏览目标网站 (确保在 Scope 范围内)
3. 插件会自动捕获并分析符合条件的请求
4. 查看 **BurpAI Agent** -> **Dashboard** 标签查看任务进度

---

## 步骤 6: 查看扫描结果

### 6.1 打开 Dashboard

点击顶部的 **BurpAI Agent** -> **Dashboard** 标签。

### 6.2 任务列表

左侧显示所有扫描任务:

```
┌────┬────────┬─────────────────────┬──────────┬───────────┐
│ ID │ Method │ URL                 │ Status   │ Vuln Found│
├────┼────────┼─────────────────────┼──────────┼───────────┤
│ 1  │ GET    │ /api/user?id=1001   │ Finished │ ✅ Yes    │
│ 2  │ POST   │ /login              │ Running  │ ⏳ ...    │
│ 3  │ GET    │ /search?q=test      │ Finished │ ❌ No     │
└────┴────────┴─────────────────────┴──────────┴───────────┘
```

### 6.3 查看详细思维链

点击任意任务，右侧显示完整的 AI 分析过程:

```
🟢 AI Thought
"参数 'id' 疑似数字型注入，准备尝试单引号报错。"

🔵 System Action
Sending Request: GET /api/user?id=1001'

🟠 Observation
收到 500 错误，包含 MySQL 关键字。

🔴 Result
✅ 确认为 SQL 注入漏洞
Severity: High
Evidence: MySQL syntax error in response
```

### 6.4 导出报告

右键点击任务 -> **Export Report** -> 选择格式:
- **JSON**: 机器可读格式
- **HTML**: 漂亮的网页报告
- **Markdown**: 适合文档

---

## 下一步

### 进阶使用

- 📖 阅读 [使用示例](./EXAMPLES.md) 了解实际案例
- 🔧 查看 [API 协议规范](./API_PROTOCOL.md) 自定义 Prompt
- 🏗️ 了解 [系统架构](./ARCHITECTURE.md) 深入理解原理

### 优化建议

1. **调整 Max Iterations**: 如果经常超时，尝试降低到 3
2. **使用更快的模型**: `gpt-3.5-turbo` 比 `gpt-4` 快 3-5 倍
3. **精细化 Scope**: 避免扫描不必要的接口，节省成本

### 获取帮助

- 💬 [GitHub Discussions](https://github.com/YOUR_USERNAME/burpai-agent/discussions)
- 🐛 [报告 Bug](https://github.com/YOUR_USERNAME/burpai-agent/issues)
- 📧 Email: (待补充)

---

## 常见问题 (FAQ)

### Q1: Token 消耗大概是多少?

**A**: 平均单次扫描:
- 简单请求 (GET): ~2,000 tokens
- 复杂请求 (POST JSON): ~5,000 tokens
- 完整 ReAct 循环 (5 轮): ~10,000 tokens

成本估算 (GPT-4):
- 单次扫描: ~$0.01 - $0.05
- 每天 100 次扫描: ~$1 - $5

### Q2: 支持哪些语言?

**A**: 
- 界面: 中文/English (可切换)
- AI 分析: 主要使用英文 (Prompt)，但结果可配置为中文

### Q3: 会不会产生破坏性操作?

**A**: 
- 默认情况下，插件只发送 **读取型** 请求 (GET, POST 查询)
- **RCE 检测默认关闭**，需手动启用
- 建议先在 **测试环境** 使用

### Q4: 如何停止正在运行的扫描?

**A**: 
在 Dashboard 中右键点击任务 -> **Cancel Task**

### Q5: 本地模型效果如何?

**A**: 
- Llama 3.1 (8B): 基础漏洞可检测，复杂场景较弱
- Llama 3.1 (70B): 接近 GPT-3.5 水平
- 建议至少使用 13B 参数以上的模型

---

恭喜！您已完成 BurpAI Agent 的配置 🎉

开始您的智能漏洞挖掘之旅吧！
