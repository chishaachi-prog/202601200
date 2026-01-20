# Changelog

All notable changes to BurpAI Agent will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0-SNAPSHOT] - 2025-01-20 (Current Development)

### Added
- Complete plugin framework implementation
- Burp Suite extension integration with Montoya API
- Configuration management system (Model, Scope, Scan Policy)
- LLM adapter architecture with OpenAI implementation
- ReAct loop engine for AI-driven vulnerability testing
- Comprehensive file upload payload generator (60+ payload types)
- Dashboard UI with real-time thought chain visualization
- Configuration UI with three tabs (Model, Scope, Policy)
- Repeater context menu integration
- HTTP client manager with request modification
- Concurrent task execution with thread pool
- Support for multiple vulnerability types:
  - SQL Injection
  - XSS (Reflected/Stored)
  - Broken Access Control (IDOR)
  - SSRF
  - File Upload Vulnerabilities (with 60+ payloads)
  - Remote Code Execution (optional)
  - Business Logic Flaws (optional)

### File Upload Payload Generator Features
- **Web Shells**: PHP, JSP, ASPX, ASP with multiple obfuscation techniques
- **Bypass Techniques**:
  - Double extensions (php.jpg, php5.jpg, etc.)
  - Magic header injection (GIF89a, JPEG, PNG)
  - Null byte injection
  - Polyglot files
  - MIME type spoofing
  - Special character bypasses
  - Unicode bypasses
- **Config File Attacks**:
  - .htaccess hijacking
  - web.config injection
  - .user.ini injection
- **Advanced Vectors**:
  - XXE via XML upload
  - SSRF via upload
  - Archive exploits (ZIP, TAR, RAR)
  - Large file DoS testing

### Technical Details
- JDK 8 compatible (uses only Java 8 APIs)
- Maven build system with dependency management
- Gson for JSON parsing
- OkHttp for HTTP client operations
- Swing UI components
- Comprehensive error handling
- Token optimization for LLM API usage
- Configurable agent parameters (max iterations, confidence level)

### Documentation
- Updated README with installation and usage instructions
- FILE_UPLOAD_PAYLOADS.md - Complete payload documentation
- FILE_UPLOAD_QUICK_START.md - Quick start guide for file upload testing
- IMPLEMENTATION_SUMMARY.md - Implementation overview

### Notes
This is a development snapshot. The core MVP features are implemented and functional.
File upload testing is a highlight feature with professional-grade payload generation.
Supports OpenAI-compatible APIs including local models (Ollama, LocalAI).

---

## [Unreleased]

### Planned Features for Future Releases
- [ ] Phase 2: HTTP client manager enhancements
- [ ] Phase 2: Anthropic Claude support
- [ ] Phase 3: Proxy passive listening mode
- [ ] Phase 3: Traffic filtering and deduplication
- [ ] Phase 4: Report export (JSON, HTML, Markdown)
- [ ] Phase 4: Custom prompt templates
- [ ] Phase 4: Plugin-based vulnerability detectors

---

## Version History Legend

### Types of Changes
- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security vulnerability fixes

---

## Future Versions (Roadmap)

### [0.2.0] - Phase 1 Milestone (MVP) - COMPLETED
- [x] Basic plugin framework
- [x] Configuration UI
- [x] LLM adapter (OpenAI)
- [x] Repeater integration
- [x] Single-round analysis
- [x] File upload payload generator (60+ payloads)

### [0.3.0] - Phase 2 Milestone (Agent Alpha) - IN PROGRESS
- [x] ReAct loop implementation
- [x] Multi-round iteration
- [x] HTTP request modification
- [x] Vulnerability detection (SQL Injection, XSS, IDOR, File Upload)
- [x] Thought chain visualization
- [ ] HTTP client manager enhancements
- [ ] Anthropic Claude support

### [0.4.0] - Phase 3 Milestone (Scope & Polish) - PLANNED
- [ ] Scope configuration
- [ ] Proxy passive listening
- [ ] Traffic filtering
- [ ] Token optimization
- [ ] Performance improvements

### [1.0.0] - Stable Release - PLANNED
- [ ] Complete testing
- [ ] Security audit
- [ ] Documentation finalization
- [ ] BApp Store submission
- [ ] Public announcement

---

## Contributing

See [CONTRIBUTING.md](./CONTRIBUTING.md) for contribution guidelines.

## License

This project is licensed under the MIT License - see [LICENSE](./LICENSE) for details.
