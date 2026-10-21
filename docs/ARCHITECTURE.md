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

## Source organization

The shared framework is grouped by reusable capability (`config`, `device`, `driver`, `evidence`, `interaction`, `locator`, `screen`). This is appropriate while the code implements platform-neutral lifecycle and actions. Android/iOS differences belong behind explicit platform-specific adapters or packages only when behavior or dependencies actually differ; do not duplicate the entire shared package tree or split Maven artifacts merely by operating system.

Consumer repositories group screens, components, and tests by application feature. A screen may use one logical locator with Android/iOS candidates when it represents the same control; genuinely different flows can use explicit platform-specific screen implementations. Application IDs, screen selectors, and business workflows stay in the consumer.

```text
src/test/java/<package_root>/
├── config/                         # consumer/device configuration
├── shared/                         # consumer-wide helpers
└── features/<feature>/
    ├── screens/                    # screen objects
    ├── components/                 # reusable screen regions, when needed
    └── tests/                      # TestNG suites for feature behavior
src/test/resources/features/<feature>/  # optional Cucumber feature files
```

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
