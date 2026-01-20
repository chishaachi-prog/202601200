# BurpAI Agent - 智能交互式漏洞扫描插件

一款基于 Burp Suite 的扩展插件，集成大语言模型（LLM）的推理能力，作为 AI Agent（智能体）具备"观察-思考-行动-验证"的闭环能力。

## 项目概述

**项目目标**：通过多轮对话迭代，模拟人类渗透测试专家的逻辑，自动生成 Payload、分析响应、绕过防御，实现深度漏洞挖掘。

**核心特性**：
- 🤖 **AI Agent 架构**：基于 ReAct (Reasoning + Acting) 循环，自主决策测试策略
- 🔄 **多轮迭代**：支持动态调整 Payload，智能绕过 WAF 和防护机制
- 🎯 **精准扫描**：支持 SQL 注入、XSS、IDOR、SSRF 等多种漏洞类型
- 🌐 **多模型支持**：兼容 OpenAI、Anthropic、Azure OpenAI 及本地模型
- 📊 **可视化思维链**：实时展示 AI 的分析和决策过程

## 文档导航

- [产品需求文档 (PRD)](./docs/PRD.md) - 完整的产品需求和设计规范
- [系统架构设计](./docs/ARCHITECTURE.md) - 技术架构和模块设计
- [API 协议规范](./docs/API_PROTOCOL.md) - LLM 交互协议和数据格式
- [开发路线图](./docs/ROADMAP.md) - 分阶段开发计划

## 快速开始

本项目当前处于 **需求设计阶段**。完整的开发计划分为三个阶段：

### 阶段一 (MVP)
- 实现配置界面（API Key 和 Model 选择）
- 实现 Repeater 右键菜单集成
- 实现单轮对话：发送请求 -> AI 分析 -> 显示结果

### 阶段二 (Agent Alpha)
- 实现自动发包功能
- 实现 JSON 解析与多轮迭代逻辑
- 实现 Dashboard 界面展示思维链

### 阶段三 (Scope & Polish)
- 完善过滤逻辑（Host/Suffix）
- 增加漏洞类型开关
- 流式 Proxy 监听支持

## 技术栈

- **插件开发**：Java (Burp Suite Extension API)
- **AI 模型**：OpenAI GPT-4, Anthropic Claude, 或本地 LLM
- **界面框架**：Java Swing
- **并发处理**：Java 线程池

## 贡献指南

本项目欢迎贡献！请查看 [CONTRIBUTING.md](./CONTRIBUTING.md) 了解详情。

## 许可证

[MIT License](./LICENSE)

## 联系方式

如有问题或建议，请提交 Issue 或 Pull Request。
