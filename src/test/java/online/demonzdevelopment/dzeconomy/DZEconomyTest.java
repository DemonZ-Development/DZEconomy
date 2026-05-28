package online.demonzdevelopment.dzeconomy;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the main DZEconomy plugin lifecycle.
 * Uses MockBukkit to simulate the Bukkit server environment.
 */
class DZEconomyTest {

    private ServerMock server;
    private DZEconomy plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DZEconomy.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Plugin enables without errors")
    void testPluginEnablesWithoutErrors() {
        assertThat(plugin.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Plugin disables cleanly")
    void testPluginDisablesCleanly() {
        assertThat(plugin.isEnabled()).isTrue();

        plugin.onDisable();

        // After disable, the static instance should be nulled out
        assertThat(DZEconomy.getInstance()).isNull();
    }

    @Test
    @DisplayName("Startup banner prints without throwing")
    void testStartupBannerPrints() {
        // The plugin has already enabled in setUp(), which calls printStartupBanner().
        // If the banner method threw an exception, the plugin would not have enabled.
        assertThat(plugin.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Config loading works after enable")
    void testConfigLoading() {
        assertThat(plugin.getConfigManager()).isNotNull();
        assertThat(plugin.getConfigManager().getConfig()).isNotNull();

        // The default config.yml should have config-version >= 2
        int configVersion = plugin.getConfigManager().getConfig().getInt("config-version", -1);
        assertThat(configVersion).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Singleton instance is set after enable")
    void testSingletonInstanceSet() {
        assertThat(DZEconomy.getInstance()).isNotNull();
        assertThat(DZEconomy.getInstance()).isSameAs(plugin);
    }

    @Test
    @DisplayName("Core managers are initialized")
    void testCoreManagersInitialized() {
        assertThat(plugin.getCurrencyManager()).isNotNull();
        assertThat(plugin.getStorageProvider()).isNotNull();
        assertThat(plugin.getConfigManager()).isNotNull();
        assertThat(plugin.getRankManager()).isNotNull();
        assertThat(plugin.getAPI()).isNotNull();
    }
}
