# BurpAI Agent

BurpAI Agent - 智能交互式漏洞扫描插件

基于 Burp Suite 的扩展插件，集成大语言模型（LLM）的推理能力，实现自动化漏洞检测。

## 项目特点

- ✅ **AI 驱动的漏洞检测**：利用 LLM 进行智能分析和推理
- ✅ **ReAct 循环架构**：支持"观察-思考-行动-验证"的闭环能力
- ✅ **多漏洞类型支持**：SQL 注入、XSS、IDOR、SSRF、文件上传等
- ✅ **专业文件上传测试**：包含 60+ 种专业 payload，覆盖绕过技术
- ✅ **JDK 8 兼容**：完全兼容 Java 8
- ✅ **可视化思维链**：实时展示 AI 的推理过程
- ✅ **多 LLM 支持**：OpenAI、Anthropic、Azure、本地模型（Ollama）

## 文件上传测试能力

本插件特别加强了文件上传漏洞测试能力，包含以下专业 payload 类型：

### Web Shells
- PHP Web Shells（基础、混淆、Base64、变量函数等多种变体）
- JSP Web Shells
- ASPX Web Shells
- ASP Classic Web Shells

### 绕过技术
- **双扩展名绕过**：.php.jpg, .shell.php5, .file.php.jpg.php
- **Magic Header 注入**：GIF89a, JPEG, PNG 头部嵌入恶意代码
- **Null 字节注入**：file.php%00.jpg
- **Polyglot 文件**：有效图片 + 恶意代码的组合
- **MIME 类型欺骗**：PHP 代码伪装成图片 MIME 类型
- **特殊字符绕过**：file.php. (trailing dot), file.php::$DATA 等
- **Unicode 绕过**：非标准字符绕过过滤器

### 配置文件攻击
- **.htaccess 攻击**：强制执行 .jpg 为 PHP
- **web.config 攻击**：IIS 配置注入
- **.user.ini 攻击**：PHP-FPM 自动包含文件

### 其他攻击向量
- **XXE via XML**：SVG 外部实体注入
- **SSRF via Upload**：SVG 内部服务请求
- **Archive Exploits**：ZIP/TAR/RAR 归档文件利用
- **大文件 DoS**：超大文件上传测试

## 系统要求

- Burp Suite Professional 2023.x 或更高版本
- Java 8 或更高版本
- 网络连接（用于访问 LLM API）
- 建议内存：至少 4GB RAM

## 构建项目

### 使用 Maven 构建

```bash
# 克隆仓库
git clone https://github.com/yourusername/burpai-agent.git
cd burpai-agent

# 编译项目
mvn clean package

# 生成的 JAR 文件位于 target/burpai-agent-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

## 安装与配置

### 安装插件

1. 打开 Burp Suite
2. 导航到 `Extender` -> `Extensions` -> `Add`
3. 选择生成的 JAR 文件
4. 点击 `Next` 完成安装

### 配置 LLM

在 `BurpAI Config` 标签页中配置：

#### OpenAI 配置
- Provider: OpenAI
- API Key: 你的 OpenAI API Key
- Model: gpt-4o, gpt-4, gpt-3.5-turbo
- Base URL: https://api.openai.com/v1

#### 本地模型配置（Ollama）
- Provider: Local
- API Key: (留空)
- Model: llama3, mistral, 或其他模型
- Base URL: http://localhost:11434/v1

## 使用方法

### 主动扫描（Repeater 模块）

1. 在 Repeater 中选择一个 HTTP 请求
2. 右键点击请求区域
3. 选择 `BurpAI Agent` 菜单
4. 选择扫描类型：
   - `Auto Analysis` - 检测所有启用的漏洞类型
   - `SQL Injection Only` - 专门检测 SQL 注入
   - `XSS Only` - 专门检测 XSS
   - `File Upload Analysis` - 专门检测文件上传漏洞
   - `IDOR Only` - 专门检测越权访问
   - `Custom Prompt` - 自定义分析指令
5. 在 `BurpAI Dashboard` 标签页中查看分析结果

### 被动监听（Proxy 模块）

配置好 `Target & Scope` 后，所有符合条件的流量将自动触发扫描。

## 项目结构

```
burpai-agent/
├── pom.xml
├── src/
│   └── main/
│       └── java/
│           └── com/burpai/agent/
│               ├── BurpAIExtension.java          # 主扩展入口
│               ├── config/
│               │   └── ConfigManager.java      # 配置管理
│               ├── core/
│               │   ├── AgentEngine.java         # ReAct 引擎
│               │   ├── PromptBuilder.java       # Prompt 构建器
│               │   ├── RequestModifier.java     # 请求修改器
│               │   ├── ScanTask.java           # 扫描任务
│               │   └── TaskExecutor.java       # 任务执行器
│               ├── http/
│               │   └── HTTPClientManager.java   # HTTP 客户端管理
│               ├── llm/
│               │   ├── LLMAdapter.java         # LLM 适配器接口
│               │   ├── LLMAdapterFactory.java   # LLM 适配器工厂
│               │   ├── OpenAIAdapter.java      # OpenAI 适配器
│               │   └── model/
│               │       ├── LLMMessage.java     # LLM 消息模型
│               │       └── LLMResponse.java    # LLM 响应模型
│               ├── payloads/
│               │   └── FileUploadPayloadGenerator.java  # 文件上传 Payload 生成器
│               ├── ui/
│               │   ├── ConfigurationPanel.java  # 配置面板
│               │   └── DashboardPanel.java      # 仪表盘面板
│               └── utils/
│                   └── Logger.java             # 日志工具
└── docs/
    ├── PRD.md                   # 产品需求文档
    ├── ARCHITECTURE.md         # 系统架构设计
    ├── API_PROTOCOL.md          # API 协议规范
    ├── ROADMAP.md              # 开发路线图
    ├── EXAMPLES.md             # 使用示例
    └── ...
```

## 开发路线图

当前版本：v1.0.0-SNAPSHOT (Phase 1 - MVP)

### 已实现功能
- ✅ 项目基础框架
- ✅ LLM 适配器（OpenAI 兼容）
- ✅ Repeater 右键菜单集成
- ✅ 配置管理界面
- ✅ Dashboard 可视化
- ✅ ReAct 循环引擎
- ✅ 专业文件上传 Payload 生成器（60+ 种变体）

### 未来计划
- [ ] HTTP 客户端管理器完善
- [ ] 更多 LLM 提供商支持（Anthropic, Claude）
- [ ] Proxy 被动监听模式
- [ ] 流量过滤和去重
- [ ] 报告导出功能
- [ ] 自定义 Prompt 模板
- [ ] 插件式漏洞检测器

## 安全说明

本工具仅用于授权的安全测试。使用本工具进行未授权测试是非法的。

## 许可证

MIT License - 详见 LICENSE 文件

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

- GitHub: https://github.com/yourusername/burpai-agent
- 文档: [文档目录](docs/)

## 致谢

- Burp Suite Extender API
- OpenAI GPT-4
- 所有贡献者
