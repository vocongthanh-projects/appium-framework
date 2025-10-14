package hooks;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;

public class AppiumServerManager {
    private static AppiumDriverLocalService service;

    public static synchronized AppiumDriverLocalService getInstance() {
        if (service == null || !service.isRunning()) {
            System.out.println("🚀 Đang khởi động Appium server...");
            service = new AppiumServiceBuilder()
                    .withIPAddress("127.0.0.1")
                    .usingPort(4723)
                    .build();
            service.start();
            System.out.println("✅ Appium server đã sẵn sàng tại: " + service.getUrl());
        } else {
            System.out.println("♻️ Reuse Appium server đang chạy.");
        }
        return service;
    }

    public static synchronized void stop() {
        if (service != null && service.isRunning()) {
            service.stop();
            System.out.println("🛑 Appium server đã được dừng.");
        }
    }
}
