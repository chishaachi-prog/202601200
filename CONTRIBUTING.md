# 贡献指南 (Contributing Guide)

感谢您对 BurpAI Agent 项目的关注！我们欢迎各种形式的贡献。

## 如何贡献

### 报告 Bug

如果您发现了 Bug，请通过 GitHub Issues 提交报告，并包含以下信息：

1. **环境信息**
   - Burp Suite 版本
   - 操作系统和版本
   - Java 版本
   - 使用的 LLM 提供商和模型

2. **复现步骤**
   - 详细的操作步骤
   - 预期行为
   - 实际行为

3. **日志和截图**
   - 相关的错误日志
   - 界面截图（如果适用）

### 提交功能建议

我们欢迎新功能的建议！请通过 GitHub Issues 提交，并说明：

1. **功能描述**: 清晰描述您希望添加的功能
2. **使用场景**: 这个功能解决什么问题
3. **预期效果**: 功能实现后的预期效果

### 提交代码

#### 开发流程

1. **Fork 本项目**到您的 GitHub 账号

2. **克隆仓库**
   ```bash
   git clone https://github.com/YOUR_USERNAME/burpai-agent.git
   cd burpai-agent
   ```

3. **创建功能分支**
   ```bash
   git checkout -b feature/your-feature-name
   ```

4. **进行开发**
   - 遵循项目的代码规范
   - 编写必要的测试
   - 更新相关文档

5. **提交更改**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

6. **推送到您的 Fork**
   ```bash
   git push origin feature/your-feature-name
   ```

7. **创建 Pull Request**
   - 在 GitHub 上创建 PR
   - 填写 PR 模板
   - 等待代码审查

#### 代码规范

- **Java 代码风格**: 遵循 Google Java Style Guide
- **命名规范**:
  - 类名: PascalCase (如 `AgentEngine`)
  - 方法名: camelCase (如 `sendRequest`)
  - 常量: UPPER_SNAKE_CASE (如 `MAX_ITERATIONS`)
- **注释**: 关键逻辑和复杂算法需要添加注释
- **文档**: 公共 API 需要 Javadoc 注释

#### 提交信息规范

使用 Conventional Commits 格式：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型 (type)**:
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式调整（不影响功能）
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建、依赖等

**示例**:
```
feat(agent): implement multi-round ReAct loop

- Add iteration control logic
- Implement observation feedback mechanism
- Support max iteration limit

Closes #123
```

### 测试

在提交 PR 之前，请确保：

- [ ] 所有现有测试通过
- [ ] 新功能包含相应的测试
- [ ] 手动测试了变更功能
- [ ] 没有引入新的警告或错误

运行测试：
```bash
./gradlew test
```

### 文档

如果您的更改涉及：
- 新功能：更新相应的文档（README.md, docs/）
- API 变更：更新 API_PROTOCOL.md
- 配置变更：更新配置示例

## 开发环境设置

### 前置要求

- JDK 17 或更高版本
- Burp Suite Professional
- Gradle 8.x
- Git

### 构建项目

```bash
# 克隆项目
git clone https://github.com/YOUR_USERNAME/burpai-agent.git
cd burpai-agent

# 构建
./gradlew build

# 生成 JAR 文件
./gradlew jar
```

### 调试

1. 在 Burp Suite 中加载插件
2. 查看 Extender > Errors 标签页的日志
3. 使用 IDE 的远程调试功能（可选）

## 社区准则

- **尊重**: 尊重所有贡献者和用户
- **友好**: 保持友好和专业的沟通
- **建设性**: 提供建设性的反馈和建议
- **包容**: 欢迎不同背景和经验的贡献者

## 许可证

通过提交代码，您同意将您的贡献以 MIT 许可证发布。

## 联系方式

如有疑问，请通过以下方式联系：

- GitHub Issues: https://github.com/YOUR_USERNAME/burpai-agent/issues
- Email: (待补充)

---

再次感谢您的贡献！🎉
