# Consumer Repository AI Instructions

This is an Appium consumer starter, not the shared framework source. The consumer owns app identifiers, screens, selectors, scenarios, credentials, and test data; the framework owns reusable driver and automation infrastructure.

- Read this file, `README.md`, and the framework APIs used by the change before editing.
- Keep Android/iOS device, simulator, application, signing, and credential settings in local or CI secret configuration; never commit secrets or private app binaries.
- Do not add product-specific behavior to the shared framework or invent APIs absent from its published/source version.
- On GitLab 10, source push pipelines are limited to configuration/dependency checks and compile/package. Run device suites only through separately configured test infrastructure with an explicitly configured Appium server and device/simulator.
- Do not publish, deploy, push, or trigger a remote pipeline unless explicitly requested.
