# Appium Automation Framework

A reusable Appium 3 framework for Android and iOS. This repository contains only mobile automation infrastructure; application screens, locators, steps, features, credentials, and test data belong to the consumer.

## Modules

```text
appium-framework/
├── framework-core/                 # configuration, driver, locator, actions, gestures, evidence
├── framework-testng/               # TestNG lifecycle independent of Cucumber
└── framework-cucumber-testng/      # Cucumber world, hooks, and safe runner
```

## Dynamic cross-platform locators

```java
private static final MobileLocator EMAIL = MobileLocator.of(
    "account.email",
    new LocatorSet(
        List.of(AppiumBy.accessibilityId("account.email"), AppiumBy.id("email")),
        List.of(AppiumBy.accessibilityId("account.email"),
                AppiumBy.iOSNsPredicateString("name == 'account.email'"))
    )
);

actions.type(EMAIL, email);
actions.typeSensitive(PASSWORD, password);
actions.click(SAVE);
```

`LocatorSet` keeps separate Android and iOS fallbacks in the exact order declared by the consumer. Action logs contain only the logical name, never the input value. Accessibility IDs, resource IDs, and native predicates should precede text; XPath should be a consumer-owned final fallback.

## Configuration

Create `src/test/resources/config/application.properties` in the consumer or use `-Dconfig.file`:

```properties
platform=android
appium.server.url=http://127.0.0.1:4723
device.name=Android emulator
device.udid=emulator-5554
android.systemPort=8201
android.app.package=com.yourcompany.application
```

Override order:

```text
system property → environment → external configuration file
→ consumer application.properties → framework.properties
```

Configuration is validated before session creation, including the application identifier, endpoint, timeout, reset policy, and forwarding port.

## Runner adapters

Plain TestNG tests extend `BaseMobileTest`. Cucumber runners extend `AbstractMobileCucumberTest` and add `vn.ots.qc.mobile.cucumber` to glue. PicoContainer creates a separate `MobileWorld` for each scenario.

Parallel execution is disabled by default because scenarios must not compete for the same physical device. To run workers concurrently in CI, assign each one a dedicated Appium endpoint, UDID, Android system port, or iOS WDA port.

For multiple devices, configure `device.targets` and use `MobileTargets.PROVIDER` with TestNG or extend `AbstractMobileMatrixCucumberTest`. The matrix fails fast on missing or duplicate UDIDs, `systemPort`, or `wdaLocalPort`; no runner, hook, or feature duplication is required.

```java
@Test(dataProvider = MobileTargets.PROVIDER, dataProviderClass = MobileTargets.class)
public void savesProfile(DeviceTarget target) {
    actions().click(SAVE);
}
```

`BaseMobileTest` reads `DeviceTarget` from test data and creates a context with that target's overrides.

`mobile().application()` provides `activate`, `terminate`, `restart`, `background`, `isInstalled`, and application-state operations using the package or bundle identifier from configuration. `ElementActions` supports enabled/disabled/text/attribute/wait-until-gone operations; `Gestures` supports four-way swipe, tap, and long press.

## Evidence and security

On failure, the framework stores a screenshot and redacted XML page source. The XML parser disables DOCTYPE declarations, external entities, and XInclude. Artifacts are stored under `target/evidence` and must not be committed.

## Verification

```bash
./mvnw clean verify
npm ci
npm audit --audit-level=high
npm run appium:drivers
```

`uiautomator2@8.2.2` currently ships with a shrinkwrap that retains `brace-expansion@5.0.8`. The `postinstall` script replaces that exact dependency with version `5.0.9`, pinned in the lockfile, and CI then runs the audit again. The script fails if the root patch does not match the expected version.

Unit and contract tests do not connect to a device. End-to-end application tests belong in the consumer repository.

See [architecture](docs/ARCHITECTURE.md) and the [quality audit](docs/QUALITY_AUDIT.md).
