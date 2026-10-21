# Repository AI Instructions

This repository contains reusable Appium infrastructure for Android and iOS. The framework owns shared driver lifecycle and automation primitives; consumer repositories own app identifiers, screens, selectors, credentials, test data, and business scenarios.

## Before changing files

- Read this file, `README.md`, [consumer source layout](docs/CONSUMER_SOURCE_LAYOUT.md), and the relevant module documentation.
- Keep Android- and iOS-specific capabilities explicit in configuration and platform-specific code. Do not infer that a shared framework must be split into separate artifacts by operating system; follow the existing module and Maven coordinates.
- Treat the existing framework and consumer boundary as authoritative. Add a capability to the framework only when it is reusable across products.
- Group framework source by reusable mobile capability inside the existing Maven modules and mirror production packages in contract tests. Keep consumer screens, app models, app identifiers, and workflows in consumer repositories. Group them by `portal/application → module`; use `screens/components/tests` vocabulary and do not invent API service packages for UI code.
- Preserve existing package and artifact coordinates unless the task explicitly asks to change them.

## OTS/FLI CI boundary

- A source push pipeline on GitLab 10 must stay lightweight: validate configuration and dependency metadata, then compile/package as configured. Do not add or trigger device, emulator, simulator, or application test suites on that runner.
- Mobile suites run through separately configured test infrastructure, such as an explicit manual or scheduled trigger. Do not assume the GitLab source runner has Appium, a device, a simulator, signing configuration, application binaries, or suitable network access.
- Do not publish, deploy, push, or trigger remote pipelines unless the user explicitly asks for that action.
