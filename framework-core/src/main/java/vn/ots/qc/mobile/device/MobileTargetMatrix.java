package vn.ots.qc.mobile.device;

import vn.ots.qc.mobile.config.ConfigurationException;
import vn.ots.qc.mobile.config.FrameworkConfig;
import vn.ots.qc.mobile.config.Platform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Parses an ordered, collision-safe device matrix from consumer configuration. */
public final class MobileTargetMatrix {
    private MobileTargetMatrix() {}

    public static List<DeviceTarget> from(FrameworkConfig config) {
        String raw = config.get("device.targets");
        if (raw.isBlank()) {
            return List.of(new DeviceTarget("default", config.platform(), Map.of()));
        }

        List<DeviceTarget> targets = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (String candidate : raw.split(",")) {
            String id = slug(candidate);
            if (!ids.add(id)) throw new ConfigurationException("Duplicate device target: " + id);
            targets.add(target(config, id));
        }
        validateIsolation(targets, config);
        return List.copyOf(targets);
    }

    private static DeviceTarget target(FrameworkConfig base, String id) {
        String prefix = "device." + id + ".";
        Platform platform = base.get(prefix + "platform").isBlank()
                ? base.platform() : Platform.from(base.get(prefix + "platform"));
        Map<String, String> overrides = new LinkedHashMap<>();
        overrides.put("platform", platform.name().toLowerCase(Locale.ROOT));
        copy(base, overrides, prefix + "server-url", "appium.server.url");
        copy(base, overrides, prefix + "name", "device.name");
        copy(base, overrides, prefix + "udid", "device.udid");
        copy(base, overrides, prefix + "platform-version", "device.platformVersion");
        copy(base, overrides, prefix + "app-path", "app.path");
        copy(base, overrides, prefix + "app-package", "android.app.package");
        copy(base, overrides, prefix + "bundle-id", "ios.app.bundleId");
        copy(base, overrides, prefix + "system-port", "android.systemPort");
        copy(base, overrides, prefix + "wda-local-port", "ios.wdaLocalPort");
        return new DeviceTarget(id, platform, overrides);
    }

    private static void validateIsolation(List<DeviceTarget> targets, FrameworkConfig base) {
        if (targets.size() < 2) return;
        Set<String> udids = new HashSet<>();
        Set<String> androidPorts = new HashSet<>();
        Set<String> iosPorts = new HashSet<>();
        for (DeviceTarget target : targets) {
            FrameworkConfig config = target.applyTo(base);
            uniqueRequired(udids, config.get("device.udid"), "UDID", target.id());
            if (target.platform() == Platform.ANDROID) {
                uniqueRequired(androidPorts, config.get("android.systemPort"), "Android system port", target.id());
            } else {
                uniqueRequired(iosPorts, config.get("ios.wdaLocalPort"), "iOS WDA port", target.id());
            }
        }
    }

    private static void uniqueRequired(Set<String> values, String value, String label, String id) {
        if (value == null || value.isBlank()) throw new ConfigurationException(label + " is required for target " + id);
        if (!values.add(value)) throw new ConfigurationException("Duplicate " + label + ": " + value);
    }

    private static void copy(FrameworkConfig source, Map<String, String> target, String from, String to) {
        String value = source.get(from);
        if (!value.isBlank()) target.put(to, value);
    }

    private static String slug(String value) {
        String slug = value == null ? "" : value.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (slug.isBlank()) throw new ConfigurationException("Device target name is blank");
        return slug;
    }
}
