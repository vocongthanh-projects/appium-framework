package vn.ots.qc.mobile.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import vn.ots.qc.mobile.evidence.MobileEvidence;

import java.nio.file.Files;
import java.nio.file.Path;

public final class MobileLifecycleHooks {
    private final MobileWorld world;
    public MobileLifecycleHooks(MobileWorld world) { this.world = world; }

    @Before(order = Integer.MIN_VALUE)
    public void startSession() { world.mobile().start(); }

    @After(order = Integer.MIN_VALUE)
    public void finishSession(Scenario scenario) {
        MobileEvidence evidence = world.mobile().finish(scenario.getName(), scenario.isFailed());
        if (!scenario.isFailed()) return;
        attach(scenario, "evidence.attach.screenshot", evidence.screenshot(), "image/png", "failure-screenshot");
        attach(scenario, "evidence.attach.page-source", evidence.pageSource(), "text/xml", "redacted-page-source");
    }

    private void attach(Scenario scenario, String setting, Path path, String mediaType, String name) {
        if (path == null || !world.config().getBoolean(setting)) return;
        try { scenario.attach(Files.readAllBytes(path), mediaType, name); }
        catch (Exception exception) { throw new IllegalStateException("Cannot attach mobile evidence", exception); }
    }
}
