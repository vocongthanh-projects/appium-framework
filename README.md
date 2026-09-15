# Appium Automation Framework

Framework Appium 3 có thể tái sử dụng cho Android và iOS. Repo này chỉ chứa hạ tầng automation mobile; screen, locator, step, feature, credential, và test data của ứng dụng thuộc về consumer.

## Module

```text
appium-framework/
├── framework-core/                 # cấu hình, driver, locator, action, gesture, evidence
├── framework-testng/               # lifecycle TestNG, độc lập với Cucumber
└── framework-cucumber-testng/      # Cucumber world, hooks, và safe runner
```

## Cài đặt (dự án consumer)

Publish trên GitLab Maven Package Registry — đây là registry **private**, nên resolve
dependency cũng cần auth, không chỉ deploy. Thêm repository vào `pom.xml` của dự án consumer:

```xml
<repositories>
    <repository>
        <id>gitlab-maven</id>
        <url>http://10.0.0.40/api/v4/groups/qc-quality-control/-/packages/maven</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>vn.ots.qc</groupId>
        <artifactId>framework-testng</artifactId>
        <version>3.0.0-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
    <!-- hoặc framework-cucumber-testng cho BDD -->
</dependencies>
```

Sau đó thêm credential tương ứng vào `~/.m2/settings.xml` **local** của bạn (personal access
token, scope `read_api` là đủ — đừng dùng lại token có scope `write_repository`/`api` cho việc
này):

```xml
<settings>
  <servers>
    <server>
      <id>gitlab-maven</id>
      <configuration>
        <httpHeaders>
          <property>
            <name>Private-Token</name>
            <value>your-personal-access-token</value>
          </property>
        </httpHeaders>
      </configuration>
    </server>
  </servers>
</settings>
```

### Cấu trúc dự án consumer

Framework không đóng gói sẵn screen, step, hay feature nào — đó là layout riêng của consumer:

```text
your-app-repo/
├── src/test/java/com/yourcompany/
│   ├── screens/            # screen object, giữ các hằng số MobileLocator
│   └── tests/               # TestNG test, extend BaseMobileTest
├── src/test/resources/
│   ├── config/
│   │   └── application.properties   # platform, device, Appium server, app package/bundle id
│   └── features/            # *.feature, nếu dùng framework-cucumber-testng
└── pom.xml
```

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

`uiautomator2@8.2.2` hiện đi kèm shrinkwrap giữ lại `brace-expansion@5.0.8`. Script `postinstall` thay đúng dependency đó bằng version `5.0.9`, pin trong lockfile, sau đó CI chạy lại audit. Script fail nếu bản patch gốc không khớp version mong đợi.

Unit test và contract test không kết nối tới thiết bị thật. Test end-to-end của ứng dụng thuộc về repo consumer.

Xem thêm [architecture](docs/ARCHITECTURE.md) và [quality audit](docs/QUALITY_AUDIT.md).
