# Architecture

```text
consumer screens / tests
   ├── framework-testng ───────────┐
   └── framework-cucumber-testng ─┤
                                  ▼
                           framework-core
                                  ▼
                       Appium Java Client
```

Core không chứa application screen hoặc Cucumber/TestNG runtime. Consumer sở hữu screen/component, logical locator definitions, features, steps, credentials và assertions.

## Locator and action pipeline

```text
MobileLocator logical name
  → LocatorSet for current platform
  → ordered candidate polling
  → first displayed/enabled element
  → action
```

Framework không tự đoán business text và không retry action có side effect. `typeSensitive` không log value.

## Lifecycle

```text
layered config → fail-fast validation → DriverSession
→ zero implicit wait → explicit actions → redacted failure evidence
→ driver.quit in finally
```

Mỗi test/scenario sở hữu một context và session. Không có static driver hoặc mutable global application state.

## Parallel safety

Code hỗ trợ context độc lập nhưng hardware là giới hạn thật. `MobileTargetMatrix` mở rộng scenario/test qua các target đã cấu hình và kiểm tra collision trước khi tạo session. Runner Cucumber mặc định vẫn serial; `AbstractMobileMatrixCucumberTest` là opt-in parallel runner khi mỗi worker có endpoint, device UDID và forwarding port riêng.
