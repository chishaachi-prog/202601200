# 系统架构设计

## 1. 整体架构

BurpAI Agent 采用模块化设计，主要分为以下几个层次：

```
┌─────────────────────────────────────────────────────┐
│              Burp Suite Extension API               │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│                  BurpAI Agent Core                  │
│  ┌────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │ UI Layer   │  │ Agent Engine│  │ LLM Adapter │  │
│  └────────────┘  └─────────────┘  └─────────────┘  │
│         ↓               ↓                 ↓         │
│  ┌────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │ Config     │  │ Task Queue  │  │ HTTP Client │  │
│  │ Manager    │  │ & Executor  │  │ Manager     │  │
│  └────────────┘  └─────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────┘
```

## 2. 核心模块

### 2.1 UI Layer (界面层)

**职责**：提供用户交互界面，展示配置和扫描结果。

**组件**：
- `ConfigurationPanel`: 配置中心主面板
  - `ModelConfigTab`: AI 引擎设置
  - `ScopeConfigTab`: 范围与过滤设置
  - `PolicyConfigTab`: 扫描策略设置
- `DashboardPanel`: 任务仪表盘
  - `TaskListView`: 任务列表视图
  - `ChatView`: 思维链展示视图

**技术栈**：Java Swing

### 2.2 Agent Engine (代理引擎)

**职责**：实现 ReAct 循环逻辑，协调 LLM 推理和 HTTP 请求执行。

**核心类**：
```java
public class AgentEngine {
    private LLMAdapter llmAdapter;
    private HTTPClientManager httpManager;
    private ConfigManager config;
    
    public AgentResult runAgent(HTTPRequest initialRequest, ScanPolicy policy) {
        // 实现 ReAct 循环
    }
}
```

**工作流程**：
1. 接收初始 HTTP 请求和扫描策略
2. 构建 System Prompt
3. 进入 ReAct 循环：
   - 调用 LLM 分析
   - 解析 LLM 响应（JSON）
   - 执行动作（发送修改后的请求）
   - 收集观察结果
   - 判断是否继续或结束
4. 返回最终结果

### 2.3 LLM Adapter (LLM 适配器)

**职责**：封装与不同 LLM 提供商的交互逻辑。

**接口设计**：
```java
public interface LLMAdapter {
    LLMResponse chat(List<Message> messages);
    boolean testConnection();
}

public class OpenAIAdapter implements LLMAdapter { }
public class AnthropicAdapter implements LLMAdapter { }
public class AzureAdapter implements LLMAdapter { }
public class LocalAdapter implements LLMAdapter { }
```

**功能**：
- 统一的消息格式转换
- API 密钥管理
- 错误处理和重试机制
- Token 使用统计

### 2.4 HTTP Client Manager (HTTP 客户端管理器)

**职责**：与 Burp Suite 的 HTTP 引擎交互，发送请求并捕获响应。

**核心功能**：
```java
public class HTTPClientManager {
    private IBurpExtenderCallbacks callbacks;
    
    public HTTPResponse sendRequest(HTTPRequest modifiedRequest) {
        // 使用 Burp API 发送请求
    }
    
    public HTTPRequest modifyParameter(HTTPRequest original, 
                                       String param, 
                                       String value) {
        // 修改请求参数
    }
}
```

**数据优化**：
- 响应体截断（默认 2KB）
- 请求头清洗
- 二进制数据过滤

### 2.5 Task Queue & Executor (任务队列与执行器)

**职责**：管理并发任务，防止阻塞 Burp 主线程。

**实现**：
```java
public class TaskExecutor {
    private ExecutorService threadPool;
    private BlockingQueue<ScanTask> taskQueue;
    
    public Future<AgentResult> submitTask(ScanTask task) {
        return threadPool.submit(() -> {
            return agentEngine.runAgent(task.getRequest(), task.getPolicy());
        });
    }
}
```

**并发控制**：
- 线程池大小：1-3（可配置）
- 任务优先级队列
- 超时控制：单个任务最大执行时间 5 分钟

### 2.6 Config Manager (配置管理器)

**职责**：持久化和管理用户配置。

**存储方式**：
- 使用 Burp API 的 `saveExtensionSetting` / `loadExtensionSetting`
- API Key 加密存储

**配置结构**：
```java
public class Configuration {
    private ModelConfig modelConfig;
    private ScopeConfig scopeConfig;
    private ScanPolicy scanPolicy;
    
    public void save() { }
    public static Configuration load() { }
}
```

## 3. 数据流

### 3.1 主动扫描流程（Repeater 触发）

```
用户右键菜单
    ↓
捕获当前请求
    ↓
创建 ScanTask
    ↓
提交到 TaskExecutor
    ↓
AgentEngine.runAgent()
    ┌─────────────────┐
    │ ReAct 循环开始  │
    └─────────────────┘
    ↓
构建 System Prompt
    ↓
调用 LLM (首次分析)
    ↓
解析 JSON 响应
    ↓
┌───────────────────┐
│ action: send_request? │
└───────────────────┘
    ↓ Yes
修改请求参数
    ↓
发送 HTTP 请求
    ↓
捕获响应
    ↓
截断和清洗数据
    ↓
构建观察结果
    ↓
调用 LLM (反思)
    ↓
┌───────────────────┐
│ 达到 Max Iterations? │
└───────────────────┘
    ↓ No
回到"解析 JSON 响应"
    ↓ Yes
生成最终报告
    ↓
更新 Dashboard UI
```

### 3.2 被动监听流程（Proxy 模块）

```
Proxy 拦截流量
    ↓
IHttpListener.processHttpMessage()
    ↓
检查 URL 后缀 (黑名单)
    ↓ Pass
检查 Host Scope
    ↓ Pass
去重检查 (URL+参数 哈希)
    ↓ 新请求
创建 ScanTask
    ↓
放入任务队列
    ↓
(后续流程同主动扫描)
```

## 4. 安全机制

### 4.1 API Key 保护
- 不在 UI 明文显示
- 使用 Burp 内置存储加密
- 内存中仅保留引用

### 4.2 死循环防护
```java
int currentIteration = 0;
while (currentIteration < config.getMaxIterations()) {
    LLMResponse response = llm.chat(messages);
    if (response.getAction().equals("finish")) {
        break;
    }
    currentIteration++;
}
```

### 4.3 异常处理
- LLM API 调用失败：重试 3 次，超时后标记任务失败
- HTTP 请求超时：10 秒超时，记录错误日志
- JSON 解析失败：记录原始响应，提示 LLM 输出格式错误

## 5. 扩展性设计

### 5.1 插件式漏洞检测
```java
public interface VulnerabilityDetector {
    String getVulnerabilityType();
    String getSystemPrompt();
    boolean validateResult(LLMResponse response);
}
```

### 5.2 自定义 Prompt 模板
- 允许用户编辑 System Prompt
- 支持变量占位符：`{url}`, `{method}`, `{headers}`, `{body}`

### 5.3 报告导出
- 支持导出为 JSON / HTML / Markdown
- 包含完整的思维链记录
- 可集成到 Burp 的 Issue 系统

## 6. 性能优化

### 6.1 Token 节省策略
- 响应体仅取前 N 字节（可配置）
- 移除 HTML 注释和空白字符
- 仅发送关键 Headers

### 6.2 缓存机制
- 相同请求的 LLM 分析结果缓存 1 小时
- 使用 LRU 策略，最大缓存 100 条

### 6.3 批处理优化
- 合并相似请求（如同一接口不同参数值）
- 一次性发送给 LLM，减少 API 调用

## 7. 测试策略

### 7.1 单元测试
- LLM Adapter 的 Mock 测试
- JSON 解析器测试
- 请求参数修改逻辑测试

### 7.2 集成测试
- 使用测试靶场（DVWA, WebGoat）
- 验证各类漏洞检测准确率
- 测试 WAF 绕过能力

### 7.3 性能测试
- 并发任务压力测试
- 内存泄漏检测
- 长时间运行稳定性测试
