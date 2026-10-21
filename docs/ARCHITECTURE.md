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

Consumer repositories group screen objects and scenarios by portal/application and feature module. This mirrors the shared ownership model while keeping mobile-specific names (`screens`, optional `components`) instead of API `client/model` packages. Android/iOS differences stay in locator candidates or explicit implementations only where behavior differs. See [Consumer Source Layout](CONSUMER_SOURCE_LAYOUT.md).

```text
src/test/java/<package_root>/
├── base/ config/ data/
├── portals/<app>/modules/<module>/{screens,components,tests}/
└── workflows/<workflow>/tests/
src/test/resources/features/<portal>/<module>/  # optional Cucumber files
```

App identifiers, screen selectors, and business workflows stay in the consumer.
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
