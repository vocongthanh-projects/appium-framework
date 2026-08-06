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

Core contains no application screen or Cucumber/TestNG runtime. The consumer owns screens and components, logical locator definitions, features, steps, credentials, and assertions.

## Locator and action pipeline

```text
MobileLocator logical name
  → LocatorSet for current platform
  → ordered candidate polling
  → first displayed and enabled element
  → action
```

The framework does not infer business text and does not retry actions with side effects. `typeSensitive` never logs its value.

## Lifecycle

```text
layered configuration → fail-fast validation → DriverSession
→ zero implicit wait → explicit actions → redacted failure evidence
→ driver.quit in finally
```

Every test or scenario owns one context and session. There is no static driver or mutable global application state.

## Parallel safety

The code supports isolated contexts, but hardware is the real constraint. `MobileTargetMatrix` expands scenarios and tests across configured targets and detects collisions before creating a session. The default Cucumber runner remains serial; `AbstractMobileMatrixCucumberTest` is an opt-in parallel runner for environments where every worker has its own endpoint, device UDID, and forwarding port.
