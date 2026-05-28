# 🤝 Contributing to DZEconomy

Thank you for your interest in contributing to DZEconomy! This document provides guidelines and information about contributing.

---

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Coding Guidelines](#coding-guidelines)
- [Submitting Changes](#submitting-changes)
- [Reporting Bugs](#reporting-bugs)
- [Requesting Features](#requesting-features)

---

## 📜 Code of Conduct

This project and everyone participating in it is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

---

## 💡 How Can I Contribute?

### 🐛 Reporting Bugs

Found a bug? Please [open an issue](https://github.com/DemonZ-Development/DZEconomy/issues/new?template=bug_report.yml) with:

- Server software and version (e.g., Paper 1.21.4)
- DZEconomy version
- Steps to reproduce
- Expected vs. actual behavior
- Relevant console logs / stack traces

### ✨ Suggesting Features

Have an idea? [Open a feature request](https://github.com/DemonZ-Development/DZEconomy/issues/new?template=feature_request.yml) with:

- A clear description of the feature
- Why it would be useful
- Any implementation ideas (optional)

### 🔧 Code Contributions

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/my-feature`
3. **Commit** your changes: `git commit -m "Add my feature"`
4. **Push** to the branch: `git push origin feature/my-feature`
5. **Open** a Pull Request

---

## 🛠️ Development Setup

### Prerequisites

- Java 21 (JDK) — [Adoptium](https://adoptium.net/)
- Git
- An IDE (IntelliJ IDEA recommended)

### Building

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/DZEconomy.git
cd DZEconomy

# Build with Gradle
./gradlew shadowJar

# Run tests
./gradlew test

# Build without tests (faster)
./gradlew shadowJar -x test
```

The JAR will be in `build/libs/DZEconomy-2.1.0.jar`.

### Project Structure

```
DZEconomy/
├── src/main/java/online/demonzdevelopment/dzeconomy/
│   ├── DZEconomy.java          # Main plugin class
│   ├── api/                    # Public API
│   ├── command/                # Command handlers
│   ├── config/                 # Configuration management
│   ├── currency/               # Currency system
│   ├── data/                   # Data models
│   ├── gui/                    # GUI managers
│   ├── integration/            # PlaceholderAPI, LuckPerms
│   ├── listener/               # Event listeners
│   ├── manager/                # Business logic managers
│   ├── rank/                   # Rank system
│   ├── storage/                # Storage backends
│   ├── task/                   # Scheduled tasks
│   ├── update/                 # Update checker
│   └── util/                   # Utilities
├── src/main/resources/         # Plugin configs (plugin.yml, config.yml, etc.)
├── src/test/                   # Unit tests
├── wiki/                       # GitHub Wiki pages
├── listings/                   # Marketplace listing descriptions
└── assets/                     # Images and screenshots
```

---

## 📝 Coding Guidelines

### Style

- Follow the existing code style and formatting
- Use 4-space indentation (no tabs)
- Maximum line length: 120 characters
- Use descriptive variable and method names

### Documentation

- Add JavaDoc comments to **all** public API methods
- Include `@param`, `@return`, and `@throws` tags where applicable
- Update wiki documentation if you change user-facing behavior

### Testing

- Write unit tests for new functionality
- Ensure all existing tests pass: `./gradlew test`
- Test on a real server if modifying gameplay features

### Commits

- Write clear, descriptive commit messages
- Use present tense: "Add feature" not "Added feature"
- Reference issues where applicable: "Fix #42: resolve balance overflow"

---

## 📬 Submitting Changes

### Pull Request Process

1. Ensure your code builds cleanly: `./gradlew shadowJar`
2. Run all tests: `./gradlew test`
3. Update documentation if needed
4. Fill out the PR template completely
5. Request review from a maintainer

### PR Guidelines

- **Keep PRs focused** — one feature or fix per PR
- **Don't mix** refactoring with feature changes
- **Update tests** for any changed behavior
- **Don't break** backward compatibility without discussion

---

## 📜 License

By contributing to DZEconomy, you agree that your contributions will be licensed under the [GNU General Public License v3.0](LICENSE).

---

<p align="center">
  Thank you for helping make DZEconomy better! ❤️
</p>
