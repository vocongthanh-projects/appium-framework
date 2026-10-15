# Appium Consumer Test Template

Starter project for a new Android/iOS test repository using the Appium Java framework. This directory is an independent Maven project: it does not inherit from the framework reactor. The framework owns driver lifecycle, typed configuration, locators/actions, and failure evidence; the consumer owns screens, app identifiers, locators, and test scenarios.

## Start from the template

Copy this directory into a new repository and update the Maven `groupId`, `artifactId`, display name, and Java package `vn.ots.qc.consumer.template` to the consumer's namespace. The starter deliberately uses an OTS template namespace rather than `com.example`. The sample is Android-configured by default. For iOS, select `platform=ios` and set the iOS bundle ID and device settings.

## Install dependencies

The framework publishes its Java artifacts to the GitLab Maven Package Registry. Its current project ID is `1264`; `pom.xml` defaults to `3.0.1-SNAPSHOT`. CI reads the registry project ID from `APPIUM_FRAMEWORK_PROJECT_ID` and authenticates with `CI_JOB_TOKEN`. Set `APPIUM_FRAMEWORK_PROJECT_ID=1264` and add the consumer project to the framework project's job-token allowlist.

For local registry access, create a Maven server entry in `~/.m2/settings.xml` with ID `appium-framework-registry` and credentials stored locally:

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

Then run:

```bash
./mvnw -Dappium.framework.repository.url=https://<GITLAB_DOMAIN>/api/v4/projects/<PROJECT_ID>/packages/maven clean test-compile
```

Use a released version for stable consumer CI when one is available. `3.0.1-SNAPSHOT` tracks the framework's development branch. In CI, set `APPIUM_FRAMEWORK_PROJECT_ID` and allow the consumer project's job token in the framework project's CI/CD job-token allowlist. For local framework development, `cd ../appium-framework && ./mvnw clean install`, then return to this directory and run `./mvnw clean test-compile`.

The Appium server and platform drivers are a separate Node.js runtime. In the copied consumer repository, install and run them with:

```bash
npm ci
npm run appium:drivers
npm run appium
```

The server and driver package versions are pinned in `package.json` and `package-lock.json`. The postinstall check keeps the Android driver's nested `brace-expansion` dependency at the audited version pinned by this template.

Current limitation: the stable Android driver release bundles nested `axios`/`morgan` dependencies that `npm audit` reports with advisories. `npm audit fix` cannot replace those bundled copies; the template does not claim a clean audit. Recheck this dependency tree when updating the Appium server or Android driver, and do not move to a prerelease driver solely to silence the audit without validating device compatibility.

## Configure a device and app

The framework reads `src/test/resources/config/application.properties` automatically. For machine-local overrides, copy `config-local.properties.example` to `config-local.properties` and run:

```bash
./mvnw -Dconfig.file=config-local.properties test
```

The external config file overrides classpath application properties. `config-local.properties` is gitignored. Never commit real credentials, signing secrets, or private app binaries. In CI, use protected/masked variables or a secret manager; provide the app binary through a controlled artifact mechanism.

Before running the test, prepare the platform-specific prerequisites:

- Start an Appium server at the configured URL. This framework repository includes a pinned local Appium 3 setup; see its root README for `npm ci` and `npm run appium`.
- Start an Android emulator or connect a device, install/configure the app, and set `android.app.package`.
- For iOS, use macOS with Xcode and a simulator/device, configure `ios.app.bundleId`, and ensure WebDriverAgent signing is set up when needed.

The TestNG smoke test only checks that Appium can create a session for the configured application. It requires a real emulator/device and is not an offline contract test. The sample CI job compiles the consumer sources; run device tests in a runner that has an Appium server and an explicitly configured device or simulator.

## Add consumer screens and tests

`SampleScreen` demonstrates a consumer-owned screen extending `BaseScreen`. Replace `sample.screen.title` with accessibility identifiers owned by the application. Use platform-specific `LocatorSet` candidates when Android and iOS expose different native identifiers. Tests extend `BaseMobileTest`; use `MobileTargets.PROVIDER` only when every parallel target has isolated device IDs and forwarding ports.

The template CI compiles Java tests and checks the pinned Node/Appium runtime. It does not start a device test because device availability, runner tags, app artifact delivery, and iOS signing are organization-specific. Add a device-test job only after those runner details are defined; never assume a hosted generic runner has an emulator or Xcode.

## Commands

```bash
./mvnw clean test
./mvnw -Dconfig.file=config-local.properties -Dtest=MobileSmokeTest test
```

Failure screenshots and redacted page source are written under `target/evidence/`. Do not commit `target/`, app packages, or local config files.
