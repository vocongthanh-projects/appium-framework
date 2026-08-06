# Quality audit

| Tiêu chí | Evidence | Result |
|---|---|---|
| Module boundaries | core, TestNG, Cucumber-TestNG | Pass |
| Application independence | không screen/step/feature/test data sản phẩm | Pass |
| Configuration | layered, immutable snapshot, fail-fast validator | Pass |
| Driver lifecycle | test-scoped session, deterministic quit | Pass |
| Dynamic locator | logical name, Android/iOS ordered fallback | Pass |
| Action safety | explicit wait, stale handling, no value logging | Pass |
| Parallel policy | safe serial default, validated device matrix và opt-in parallel runners | Pass |
| Evidence/security | screenshot, redacted XML, XXE protections | Pass |
| Runner independence | TestNG và Cucumber adapters tách riêng | Pass |
| Verification/delivery | 21 executed contract tests + 1 abstract runner entry point, clean npm audit, two GitLab CI jobs | Pass |

Appium framework đạt 10/10 theo toàn bộ acceptance rubric trên. Device E2E thuộc consumer vì reusable framework không được chứa application package, login flow hoặc credential giả để tự chạy.
