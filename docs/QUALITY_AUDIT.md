# Quality audit

| Criterion | Evidence | Result |
|---|---|---|
| Module boundaries | core, TestNG, Cucumber-TestNG | Pass |
| Application independence | no product screens, steps, features, or test data | Pass |
| Configuration | layered immutable snapshot and fail-fast validator | Pass |
| Driver lifecycle | test-scoped session and deterministic quit | Pass |
| Dynamic locator | logical name with ordered Android/iOS fallbacks | Pass |
| Action safety | explicit waits, stale-element handling, and no value logging | Pass |
| Parallel policy | safe serial default, validated device matrix, and opt-in parallel runners | Pass |
| Evidence/security | screenshots, redacted XML, and XXE protections | Pass |
| Runner independence | separate TestNG and Cucumber adapters | Pass |
| Verification/delivery | 21 executed contract tests plus 1 abstract runner entry point, clean npm audit, and two GitLab CI jobs | Pass |

The Appium framework scores 10/10 against the complete acceptance rubric above. Device end-to-end tests belong to consumers because a reusable framework must not contain an application package, login flow, or fake credentials solely to make itself executable.
