package online.demonzdevelopment.dzeconomy.adapter;

import online.demonzdevelopment.dzeconomy.adapter.impl.LegacyServerAdapter;
import online.demonzdevelopment.dzeconomy.adapter.impl.ModernServerAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerAdapterProviderTest {

    @AfterEach
    void tearDown() {
        ServerAdapterProvider.reset();
    }

    @Test
    @DisplayName("detectPlatform returns LEGACY for Java 1.8")
    void detectPlatformLegacyJava8() {
        ServerPlatform platform = runWithJavaVersion("1.8.0_402",
                () -> ServerAdapterProvider.detectPlatform());
        assertEquals(ServerPlatform.LEGACY, platform);
    }

    @Test
    @DisplayName("detectPlatform returns LEGACY for Java 11")
    void detectPlatformLegacyJava11() {
        ServerPlatform platform = runWithJavaVersion("11.0.22",
                () -> ServerAdapterProvider.detectPlatform());
        assertEquals(ServerPlatform.LEGACY, platform);
    }

    @Test
    @DisplayName("detectPlatform returns MODERN for Java 17")
    void detectPlatformModernJava17() {
        ServerPlatform platform = runWithJavaVersion("17.0.12",
                () -> ServerAdapterProvider.detectPlatform());
        assertEquals(ServerPlatform.MODERN, platform);
    }

    @Test
    @DisplayName("detectPlatform returns MODERN for Java 21")
    void detectPlatformModernJava21() {
        ServerPlatform platform = runWithJavaVersion("21.0.4",
                () -> ServerAdapterProvider.detectPlatform());
        assertEquals(ServerPlatform.MODERN, platform);
    }

    @Test
    @DisplayName("getAdapter returns LegacyServerAdapter on Java 11")
    void getAdapterLegacy() {
        ServerAdapter adapter = runWithJavaVersion("11.0.22",
                () -> ServerAdapterProvider.getAdapter());
        assertInstanceOf(LegacyServerAdapter.class, adapter);
    }

    @Test
    @DisplayName("getAdapter returns ModernServerAdapter on Java 21")
    void getAdapterModern() {
        ServerAdapter adapter = runWithJavaVersion("21.0.4",
                () -> ServerAdapterProvider.getAdapter());
        assertInstanceOf(ModernServerAdapter.class, adapter);
    }

    @Test
    @DisplayName("getAdapter returns cached instance")
    void getAdapterCached() {
        ServerAdapter first = ServerAdapterProvider.getAdapter();
        ServerAdapter second = ServerAdapterProvider.getAdapter();
        assertSame(first, second);
    }

    @Test
    @DisplayName("reset clears cached adapter")
    void resetClearsCache() {
        ServerAdapter first = ServerAdapterProvider.getAdapter();
        ServerAdapterProvider.reset();
        ServerAdapter second = ServerAdapterProvider.getAdapter();
        assertNotNull(second);
    }

    @Test
    @DisplayName("LegacyServerAdapter platform is LEGACY")
    void legacyPlatform() {
        assertEquals(ServerPlatform.LEGACY, new LegacyServerAdapter().getPlatform());
    }

    @Test
    @DisplayName("ModernServerAdapter platform is MODERN")
    void modernPlatform() {
        assertEquals(ServerPlatform.MODERN, new ModernServerAdapter().getPlatform());
    }

    private <T> T runWithJavaVersion(String version, java.util.function.Supplier<T> action) {
        String original = System.getProperty("java.version");
        try {
            System.setProperty("java.version", version);
            return action.get();
        } finally {
            if (original != null) {
                System.setProperty("java.version", original);
            } else {
                System.clearProperty("java.version");
            }
        }
    }
}
