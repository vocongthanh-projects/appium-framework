package vn.ots.qc.mobile.device;

import java.util.Optional;

/** Thread-bound target selected by a runner before scenario dependency injection starts. */
public final class ExecutionDevice {
    private static final ThreadLocal<DeviceTarget> CURRENT = new ThreadLocal<>();
    private ExecutionDevice() {}
    public static void set(DeviceTarget target) { CURRENT.set(java.util.Objects.requireNonNull(target)); }
    public static Optional<DeviceTarget> current() { return Optional.ofNullable(CURRENT.get()); }
    public static void clear() { CURRENT.remove(); }
}
