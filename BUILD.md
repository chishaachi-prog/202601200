# BurpAI Agent Build Instructions

## Prerequisites

1. **JDK 8 or higher**
   - Download from: https://adoptium.net/ or use your distribution's package manager
   - Verify installation: `java -version` should show Java 8+

2. **Apache Maven 3.6+**
   - Download from: https://maven.apache.org/download.cgi
   - Or install via package manager:
     - Ubuntu/Debian: `sudo apt-get install maven`
     - macOS: `brew install maven`
   - Verify installation: `mvn -version`

3. **Burp Suite Professional**
   - Download from: https://portswigger.net/burp/pro
   - Required for extension development and testing

## Building the Project

### 1. Clone or Download the Repository

```bash
git clone https://github.com/yourusername/burpai-agent.git
cd burpai-agent
```

### 2. Compile the Project

```bash
mvn clean compile
```

This will:
- Download required dependencies
- Compile all Java source files
- Place compiled classes in `target/classes/`

### 3. Package the Extension

```bash
mvn clean package
```

This will:
- Compile all source files
- Run any tests (if configured)
- Create a "fat JAR" with all dependencies included
- Output: `target/burpai-agent-1.0.0-SNAPSHOT-jar-with-dependencies.jar`

### 4. Verify the Build

The following files should be created:
- `target/burpai-agent-1.0.0-SNAPSHOT.jar` - Main JAR without dependencies
- `target/burpai-agent-1.0.0-SNAPSHOT-jar-with-dependencies.jar` - Complete JAR (use this)

## Installing in Burp Suite

### Method 1: Manual Installation

1. Start Burp Suite Professional
2. Navigate to `Extender` -> `Extensions`
3. Click the `Add` button
4. Select `Extension file`
5. Browse to: `target/burpai-agent-1.0.0-SNAPSHOT-jar-with-dependencies.jar`
6. Click `Next` to load the extension
7. You should see `BurpAI Agent` in the Extensions tab

### Method 2: Automatic Loading (Optional)

To automatically load the extension when Burp Suite starts:

1. Go to `Extender` -> `Extensions`
2. Click `Add` and load the extension
3. Check the "Load this extension on startup" checkbox
4. Extension will load automatically in future sessions

## Verifying Installation

### Check for Tabs

After successful installation, you should see two new tabs:

1. **BurpAI Config** - Configuration interface
2. **BurpAI Dashboard** - Scan results and thought chain visualization

### Check for Context Menu

1. Open the `Repeater` module
2. Select any HTTP request
3. Right-click in the request area
4. You should see `BurpAI Agent` menu with sub-items:
   - Auto Analysis
   - SQL Injection Only
   - XSS Only
   - File Upload Analysis
   - IDOR Only
   - Custom Prompt

## Common Build Issues

### Issue: "mvn: command not found"

**Solution**: Install Maven following the Prerequisites section above.

### Issue: "Unsupported class file major version"

**Solution**: This indicates you're using an older Java version. Install JDK 8 or higher:
```bash
# Check Java version
java -version

# Install JDK 8 on Ubuntu/Debian
sudo apt-get install openjdk-8-jdk

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
```

### Issue: "Dependency resolution failed"

**Solution**: Try cleaning and rebuilding:
```bash
mvn clean install -U
```
The `-U` flag forces update of snapshots and releases.

### Issue: "Cannot find symbol: BurpExtender"

**Solution**: Ensure you have the correct Burp API dependency in `pom.xml`. The project should include:
```xml
<dependency>
    <groupId>net.portswigger.burp.extender</groupId>
    <artifactId>burp-extender-api</artifactId>
    <version>2.1.07</version>
    <scope>provided</scope>
</dependency>
```

## Development Setup

### Using an IDE (IntelliJ IDEA)

1. Open IntelliJ IDEA
2. `File` -> `Open` -> Select the project directory
3. IntelliJ will automatically detect the Maven project
4. Wait for dependency indexing to complete
5. Set up Maven run configuration:
   - Run -> Edit Configurations
   - Click `+` -> Maven
   - Command line: `clean package`
   - Save configuration

### Using an IDE (Eclipse)

1. Install M2Eclipse plugin (if not installed)
2. `File` -> `Import` -> `Maven` -> `Existing Maven Projects`
3. Browse to the project directory
4. Eclipse will import the project
5. Configure build path and run configurations

### Using an IDE (VS Code)

1. Install the "Extension Pack for Java" extension
2. Install the "Maven for Java" extension
3. Open the project folder in VS Code
4. VS Code will automatically detect the Maven project
5. Use the integrated terminal for Maven commands

## Testing

### Manual Testing

1. Build the extension: `mvn clean package`
2. Load in Burp Suite
3. Test configuration panel:
   - Configure API settings
   - Click "Test Connection"
4. Test scanning:
   - Use Repeater with a sample request
   - Right-click and select scan type
   - View results in Dashboard

### Automated Testing (Future)

Unit and integration tests will be added in future releases.

## Continuous Integration

The project can be set up with CI/CD pipelines:

### GitHub Actions

Create `.github/workflows/build.yml`:
```yaml
name: Build
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 8
        uses: actions/setup-java@v2
        with:
          java-version: '8'
          distribution: 'temurin'
      - name: Build with Maven
        run: mvn clean package
```

## Release Preparation

To prepare a release:

1. Update version in `pom.xml`
2. Update version in `README.md`
3. Update `CHANGELOG.md`
4. Tag the release:
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```
5. Build the release:
   ```bash
   mvn clean package
   ```
6. Upload JAR to GitHub Releases
7. Update documentation

## Performance Tips

### Faster Builds

Skip tests during development:
```bash
mvn package -DskipTests
```

### Offline Builds

If you have all dependencies cached:
```bash
mvn package -o
```

### Parallel Builds

Use multiple threads for faster compilation:
```bash
mvn package -T 4
```

## Clean Build

To ensure a clean build:
```bash
mvn clean
rm -rf target/
mvn package
```

## Next Steps

After building and installing:

1. Read the [README.md](README.md) for usage instructions
2. Check [FILE_UPLOAD_QUICK_START.md](docs/FILE_UPLOAD_QUICK_START.md) for file upload testing
3. Review [FILE_UPLOAD_PAYLOADS.md](docs/FILE_UPLOAD_PAYLOADS.md) for payload details
4. Configure your LLM API in the BurpAI Config tab
5. Start scanning!

## Getting Help

- GitHub Issues: https://github.com/yourusername/burpai-agent/issues
- Documentation: [docs/](docs/)
- OWASP File Upload Testing: https://owasp.org/www-community/attacks/Unrestricted_File_Upload

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
