# GSafe CBCS Android UI Automation Framework

This project is a modern, enterprise-grade Android UI Automation framework designed specifically for the **GSafe CBCS** application. It is built using **Java 17, Appium 9+, Selenium 4.22+, Cucumber (BDD), and TestNG**, and outputs highly customized, professional Excel reports based on TestNG/Allure results.

## 🚀 Key Features
- **Appium 9+ Standardized**: Fully utilizes the latest `UiAutomator2Options` and strict port management.
- **Zero-Hardcoding Architecture**: No locator strings or strict logic is hardcoded in Java. Everything is configured dynamically through `ConfigManager` and `config.properties`.
- **Custom Excel Reporting Engine**: Generates a rich, interactive `.xlsx` test report with styling optimizations mapping tests directly to product specs.
- **Auto-Wait Utilities**: Reusable, reliable UI locators with built-in fluent webdriver waits mapped directly through `FunctionUtils.java`.

---

## 🛠 Prerequisites

Before cloning and running this project, ensure your machine has the following installed:
1. **Java Development Kit (JDK) 17+**
2. **Apache Maven** (3.8.x or newer)
3. **Appium 2.x server** (`npm install -g appium`)
4. **Appium UiAutomator2 driver** (`appium driver install uiautomator2`)
5. **Android SDK / Android Studio** (With `ANDROID_HOME` correctly set in your environment variables)
6. A physical Android device connected via Debug Mode or a running Emulator.

---

## 📥 Setup and Installation

### 1. Clone the repository
```bash
git clone <repository_url>
cd gsafe_cbcs_android_ui
```

### 2. Download Dependencies
Ensure you can compile the code and fetch all `pom.xml` dependencies:
```bash
mvn clean install -DskipTests
```

---

## ⚙️ Configuration (Using `config-local.properties`)

By default, the framework relies on `src/test/resources/config.properties`. This file contains the foundational/baseline properties for the environment and CI/CD pipelines.

**⚠️ IMPORTANT: Never edit your local device credentials directly into `config.properties`** as you run the risk of committing your passwords or unique device IDs (`Udid`) to Git!

Instead, we use a **Local Configuration Override** mechanism.

### How to configure for your Local Machine:
1. Navigate to `src/test/resources/`.
2. Create a new file named **`config-local.properties`**.
3. Copy the keys you need to override from `config.properties` into `config-local.properties` and add your specific local machine data.

> **Note**: `config-local.properties` is globally ignored in `.gitignore` and **will never be pushed to the repository**.

**Example `config-local.properties` file:**
```properties
# Your unique Android Device ID (Get it via 'adb devices')
android.device.name=My_Local_Phone
android.udid=RF8N1234567

# Your local Appium Port (if you prefer running different ports)
appium.server.port=4723

# Testing accounts
test.username=YOUR_TEST_ACCOUNT
test.password=YOUR_SECURE_PASSWORD
```

**How it works**: The `ConfigManager` class will always load `config.properties` first, then seamlessly over-write those baseline variables with anything you have placed inside your `config-local.properties`. 

---

## ▶️ Execution

### Running All Tests
To execute the entire test suite via Maven:
```bash
mvn clean test -U
```

### Running Specific Profiles/Tags
If you have configured different test suites in your `pom.xml`, run:
```bash
mvn clean test -P <ProfileName>
```
To run tests grouped by Cucumber tags (e.g., `@login`):
```bash
mvn test -Dcucumber.filter.tags="@login"
```

---

## 📊 Reporting

Once the execution completes, the custom `AllureToExcelExporter` hook captures the Cucumber JSON results and dynamically transforms them into an executive-level Excel Report. 

You can locate the newly generated Excel Automation Report `.xlsx` files at:
- `test-output/excel-report/`

Features of the Excel Report include:
- Executive Summary Dashboards (Pass/Fail %)
- Step-by-step breakdown directly mirroring internal Test Case standards.
- Embedded Screenshots (Failures and specific checkpoint successes).
- Categorization by feature modules.

---

## 🧠 Best Practices
- **No Vietnamese or Hardcoded Strings in Java classes**: Keep all dynamic localized Strings inside `config.properties` or resource files.
- **Lazy Initialization Approach**: Never pre-instantiate elements globally in a class; let standard locator look-ups take place at runtime for absolute stability.
