# Appium Automation Framework

Reusable Appium 3 framework cho Android và iOS. Repository chỉ chứa mobile automation infrastructure; application screens, locators, steps, features, credentials và test data thuộc consumer.

## Modules

```text
appium-framework/
├── framework-core/                 # config, driver, locator, actions, gestures, evidence
├── framework-testng/               # TestNG lifecycle không phụ thuộc Cucumber
└── framework-cucumber-testng/      # Cucumber world, hooks và safe runner
```

## Dynamic cross-platform locator

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

`LocatorSet` giữ fallback riêng cho Android/iOS theo đúng thứ tự consumer khai báo. Action log chỉ ghi logical name, không ghi giá trị input. Accessibility ID/resource ID/native predicate nên đứng trước text; XPath chỉ là fallback cuối do consumer sở hữu.

## Configuration

Tạo `src/test/resources/config/application.properties` trong consumer hoặc dùng `-Dconfig.file`:

```properties
platform=android
appium.server.url=http://127.0.0.1:4723
device.name=Android emulator
device.udid=emulator-5554
android.systemPort=8201
android.app.package=com.yourcompany.application
```

Thứ tự override:

```text
system property → environment → external config file
→ consumer application.properties → framework.properties
```

Config được validate trước khi tạo session: app ID, endpoint, timeout, reset policy và forwarding port.

## Runner adapters

TestNG thuần kế thừa `BaseMobileTest`. Cucumber runner kế thừa `AbstractMobileCucumberTest` và thêm `vn.ots.qc.mobile.cucumber` vào glue. `MobileWorld` được PicoContainer tạo riêng cho từng scenario.

Parallel mặc định tắt vì hai scenario không được tranh cùng physical device. CI muốn chạy song song phải cấp riêng cho mỗi worker: Appium endpoint, UDID, Android system port hoặc iOS WDA port.

Khi có nhiều thiết bị, khai báo `device.targets` và dùng `MobileTargets.PROVIDER` với TestNG hoặc kế thừa `AbstractMobileMatrixCucumberTest`. Matrix fail-fast nếu thiếu/trùng UDID, `systemPort` hoặc `wdaLocalPort`; không cần copy runner/hook/feature theo từng máy.

```java
@Test(dataProvider = MobileTargets.PROVIDER, dataProviderClass = MobileTargets.class)
public void savesProfile(DeviceTarget target) {
    actions().click(SAVE);
}
```

`BaseMobileTest` đọc `DeviceTarget` từ test data và tạo context bằng đúng override của target đó.

`mobile().application()` cung cấp `activate`, `terminate`, `restart`, `background`, `isInstalled` và application state bằng package/bundle ID từ config. `ElementActions` có enabled/disabled/text/attribute/wait-until-gone; `Gestures` hỗ trợ swipe bốn hướng, tap và long press.

## Evidence and security

Khi fail, framework lưu screenshot và XML page source đã redact secure fields. XML parser tắt DOCTYPE, external entity và XInclude. Artifact nằm dưới `target/evidence` và không được commit.

## Verification

```bash
./mvnw clean verify
npm ci
npm audit --audit-level=high
npm run appium:drivers
```

`uiautomator2@8.2.2` hiện phát hành kèm shrinkwrap giữ `brace-expansion@5.0.8`. `postinstall` thay đúng dependency đó bằng bản `5.0.9` đã pin trong lockfile, sau đó CI bắt buộc audit lại. Script sẽ fail nếu bản vá root không đúng phiên bản mong đợi.

Unit/contract tests không kết nối device. E2E application tests nằm ở consumer repository.

Xem [architecture](docs/ARCHITECTURE.md) và [quality audit](docs/QUALITY_AUDIT.md).
