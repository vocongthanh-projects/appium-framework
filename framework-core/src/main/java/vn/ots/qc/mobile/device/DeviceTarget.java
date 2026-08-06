package vn.ots.qc.mobile.device;

import vn.ots.qc.mobile.config.FrameworkConfig;
import vn.ots.qc.mobile.config.Platform;

import java.util.Map;

/** One isolated execution target; all application identifiers remain configuration-owned. */
public record DeviceTarget(String id, Platform platform, Map<String, String> overrides) {
    public DeviceTarget {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Device target id is required");
        platform = java.util.Objects.requireNonNull(platform, "platform");
        overrides = Map.copyOf(overrides == null ? Map.of() : overrides);
    }

    public FrameworkConfig applyTo(FrameworkConfig base) {
        return base.withOverrides(overrides);
    }
}
