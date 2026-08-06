package vn.ots.qc.mobile.cucumber;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import vn.ots.qc.mobile.config.FrameworkConfig;
import vn.ots.qc.mobile.device.DeviceTarget;
import vn.ots.qc.mobile.device.ExecutionDevice;
import vn.ots.qc.mobile.device.MobileTargetMatrix;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Opt-in Cucumber runner that expands each scenario across validated isolated devices. */
public abstract class AbstractMobileMatrixCucumberTest extends AbstractTestNGCucumberTests {
    private final Map<String, Deque<DeviceTarget>> targetsByScenario = new LinkedHashMap<>();

    @Override
    @DataProvider(parallel = true)
    public synchronized Object[][] scenarios() {
        Object[][] original = super.scenarios();
        List<DeviceTarget> targets = MobileTargetMatrix.from(FrameworkConfig.load());
        Object[][] expanded = new Object[original.length * targets.size()][2];
        int row = 0;
        for (Object[] scenario : original) {
            PickleWrapper pickle = (PickleWrapper) scenario[0];
            FeatureWrapper feature = (FeatureWrapper) scenario[1];
            Deque<DeviceTarget> queue = targetsByScenario.computeIfAbsent(key(pickle, feature), ignored -> new ArrayDeque<>());
            for (DeviceTarget target : targets) {
                expanded[row++] = new Object[]{pickle, feature};
                queue.addLast(target);
            }
        }
        return expanded;
    }

    @Override
    @Test(groups = "cucumber", description = "Runs Cucumber scenarios on isolated mobile targets", dataProvider = "scenarios")
    public void runScenario(PickleWrapper pickle, FeatureWrapper feature) {
        DeviceTarget target;
        synchronized (this) {
            Deque<DeviceTarget> queue = targetsByScenario.get(key(pickle, feature));
            if (queue == null || queue.isEmpty()) throw new IllegalStateException("No device target assigned");
            target = queue.removeFirst();
        }
        ExecutionDevice.set(target);
        try { super.runScenario(pickle, feature); }
        finally { ExecutionDevice.clear(); }
    }

    private static String key(PickleWrapper pickle, FeatureWrapper feature) {
        return feature + "::" + pickle.getPickle().getUri() + "::" + pickle.getPickle().getName();
    }
}
