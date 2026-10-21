# Appium Automation Framework


> Maven namespace migration: this source tree now uses `vn.ots.qc.mobile` for its shared Android/iOS artifacts. Existing published artifacts under `vn.ots.qc` are not relocated; consumers must update their dependency group IDs when using a package published from this source state.

Framework Appium 3 có thể tái sử dụng cho Android và iOS. Repo này chỉ chứa hạ tầng automation mobile; screen, locator, step, feature, credential, và test data của ứng dụng thuộc về consumer.

Quy tắc tổ chức source dùng chung với các framework automation khác nằm trong [Framework Source Layout](docs/FRAMEWORK_SOURCE_LAYOUT.md). Các module Maven hiện tại được giữ theo capability mobile và adapter tương ứng.

## Module

```text
framework-appium/
├── framework-core/                 # cấu hình, driver, locator, action, gesture, evidence
├── framework-testng/               # lifecycle TestNG, độc lập với Cucumber
└── framework-cucumber-testng/      # Cucumber world, hooks, và safe runner
```

[`template-consumer-appium/`](template-consumer-appium/README.md) là starter project độc lập để copy thành repository consumer. Nó không nằm trong reactor và không chứa app ID hay nghiệp vụ sản phẩm. Khi copy, đổi cả Maven coordinates và Java package `vn.ots.qc.consumer.template` sang namespace dự án.

## Cài đặt (dự án consumer)

Source pipeline hiện chỉ compile/verify framework và kiểm tra runtime Node; nó không publish Maven artifact. Version source hiện tại là `3.0.1-SNAPSHOT`. Khi một artifact đã được publish vào GitLab Maven Package Registry, consumer cần quyền đọc package, thêm project consumer vào job-token allowlist của framework và cấu hình project ID registry tương ứng trong CI.

Dependency của consumer:

```xml
<repositories>
    <repository>
        <id>appium-framework-registry</id>
        <url>https://&lt;GITLAB_DOMAIN&gt;/api/v4/projects/&lt;PROJECT_ID&gt;/packages/maven</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>vn.ots.qc.mobile</groupId>
        <artifactId>framework-testng</artifactId>
        <version>3.0.1-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
    <!-- hoặc framework-cucumber-testng cho BDD -->
</dependencies>
```

Lưu thông tin xác thực local trong `~/.m2/settings.xml` (không commit token):

```xml
<settings>
  <servers>
    <server>
      <id>appium-framework-registry</id>
      <username>YOUR_GITLAB_USERNAME_OR_TOKEN_USER</username>
      <password>YOUR_LOCAL_READ_PACKAGE_TOKEN</password>
    </server>
  </servers>
</settings>
```

`template-consumer-appium/` includes a consumer CI example using `CI_JOB_TOKEN`; the framework project must allow that consumer project in its job-token allowlist. The Appium Node runtime job reports current npm audit advisories as a non-blocking warning; Java framework verification remains required before Maven publication.

### Cấu trúc source consumer

Consumer gom màn hình và bài kiểm thử theo cùng portal/module; Android/iOS khác nhau ở locator hoặc implementation khi hành vi thực sự khác, không phải ở cách nhân đôi toàn bộ cây source. Quy tắc chung và các ví dụ theo stack nằm ở [Consumer Source Layout](docs/CONSUMER_SOURCE_LAYOUT.md).

```text
src/test/java/<package_root>/
├── base/ config/ data/                         # hạ tầng consumer
├── portals/<app>/modules/<module>/
│   ├── screens/                                # screen objects và logical locators
│   ├── components/                             # vùng UI tái sử dụng nếu cần
│   └── tests/                                  # TestNG suite theo feature
└── workflows/<workflow>/tests/                # hành trình qua nhiều module

src/test/resources/features/<portal>/<module>/     # file Cucumber tùy chọn
```

Không tạo service package cho UI nếu không có API client. Với cùng một control trên Android/iOS, khai báo candidate theo nền tảng; chỉ tách screen khi flow hoặc cấu trúc UI khác thực sự.
## Locator động xuyên nền tảng (dynamic cross-platform locators)

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

`LocatorSet` giữ riêng 2 danh sách fallback cho Android và iOS theo đúng thứ tự consumer khai báo. Log action chỉ chứa tên logic, không bao giờ chứa giá trị input. Accessibility ID, resource ID, và native predicate nên đứng trước text; XPath nên là fallback cuối cùng do consumer tự sở hữu.

## Cấu hình

Tạo `src/test/resources/config/application.properties` bên consumer, hoặc dùng `-Dconfig.file`:

```properties
platform=android
appium.server.url=http://127.0.0.1:4723
device.name=Android emulator
device.udid=emulator-5554
android.systemPort=8201
android.app.package=com.yourcompany.application
```

Thứ tự ghi đè:

```text
system property → environment → file cấu hình bên ngoài
→ application.properties của consumer → framework.properties
```

Cấu hình được validate trước khi tạo session, bao gồm application identifier, endpoint, timeout, reset policy, và forwarding port.

## Runner adapter

Test TestNG thuần extend `BaseMobileTest`. Runner Cucumber extend `AbstractMobileCucumberTest` và thêm `vn.ots.qc.mobile.cucumber` vào glue. PicoContainer tạo 1 `MobileWorld` riêng cho mỗi scenario.

Chạy song song bị tắt mặc định vì các scenario không được tranh chấp cùng 1 thiết bị vật lý. Để chạy worker đồng thời trên CI, gán cho mỗi worker 1 Appium endpoint, UDID, Android system port, hoặc iOS WDA port riêng.

Với nhiều thiết bị, cấu hình `device.targets` và dùng `MobileTargets.PROVIDER` với TestNG hoặc extend `AbstractMobileMatrixCucumberTest`. Matrix fail nhanh khi thiếu hoặc trùng UDID, `systemPort`, hoặc `wdaLocalPort`; không cần duplicate runner, hook, hay feature nào.

```java
@Test(dataProvider = MobileTargets.PROVIDER, dataProviderClass = MobileTargets.class)
public void savesProfile(DeviceTarget target) {
    actions().click(SAVE);
}
```

`BaseMobileTest` đọc `DeviceTarget` từ test data và tạo context với override của target đó.

`mobile().application()` cung cấp `activate`, `terminate`, `restart`, `background`, `isInstalled`, và các thao tác trạng thái ứng dụng dùng package/bundle identifier từ cấu hình. `ElementActions` hỗ trợ thao tác enabled/disabled/text/attribute/wait-until-gone; `Gestures` hỗ trợ swipe 4 hướng, tap, và long press.

## Evidence và bảo mật

Khi fail, framework lưu screenshot và XML page source đã redact. XML parser tắt DOCTYPE declaration, external entity, và XInclude. Artifact được lưu dưới `target/evidence` và không được commit.

## Kiểm thử (verification)

```bash
./mvnw clean verify
npm ci
npm audit --audit-level=high
npm run appium:drivers
```

Consumer template hiện pin Appium `3.8.0`, UiAutomator2 `8.7.0`, XCUITest `12.15.2` và Node `>=20.19.0`. `npm audit` trên dependency tree Appium là cảnh báo không chặn pipeline do một số dependency lồng nhau được đóng gói sẵn; `npm audit fix` không thể thay thế mọi bản copy. Không bỏ pin hoặc nâng prerelease chỉ để làm audit xanh. Kiểm tra advisories và tương thích thiết bị khi nâng version.

Unit test và contract test không kết nối tới thiết bị thật. Test end-to-end của ứng dụng thuộc về repo consumer.

Xem thêm [architecture](docs/ARCHITECTURE.md) và [quality audit](docs/QUALITY_AUDIT.md).
