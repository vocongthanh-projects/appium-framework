package hooks;

import io.cucumber.java.*;

/**
 * Global Hooks - Managed project lifecycle following ui_gtel_map exactly.
 * Standardized for parallel Android testing.
 */
public class Hooks extends AbstractBaseHooks {

    @Before
    public void setUp(Scenario scenario) {
        setUpBase(scenario);
    }

    @After
    public void afterScenario(Scenario scenario) {
        tearDownBase(scenario);
    }

    @SuppressWarnings("unused")
    @AfterAll
    public static void afterAll() {
        shutDownBase();
    }
}