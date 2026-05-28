package online.demonzdevelopment.dzeconomy;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import online.demonzdevelopment.dzeconomy.config.ConfigMigrator;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ConfigMigrator – configuration migration between versions.
 * Uses MockBukkit to load the real plugin for full config integration.
 */
class ConfigMigratorTest {

    private ServerMock server;
    private DZEconomy plugin;

    @TempDir
    File tempDir;

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
    @DisplayName("Migration from v1 to v2 adds new keys")
    void testMigrationV1ToV2AddsNewKeys() {
        FileConfiguration config = plugin.getConfigManager().getConfig();

        // Simulate v1 config: set version to 1 and remove v2 keys
        config.set("config-version", 1);
        config.set("updates.check-interval", null);
        config.set("updates.modrinth-project-id", null);
        config.set("updates.notify.on-join", null);
        config.set("combat-tag.enabled", null);
        config.set("combat-tag.duration", null);
        config.set("baltop.limit", null);
        config.set("baltop.cache-minutes", null);
        config.set("payall.enabled", null);

        // Run migration
        ConfigMigrator migrator = new ConfigMigrator(plugin);
        migrator.migrate();

        // Verify new v2 keys were added
        assertThat(config.getInt("config-version")).isEqualTo(ConfigMigrator.CURRENT_CONFIG_VERSION);
        assertThat(config.contains("updates.check-interval")).isTrue();
        assertThat(config.contains("updates.modrinth-project-id")).isTrue();
        assertThat(config.contains("combat-tag.enabled")).isTrue();
        assertThat(config.contains("combat-tag.duration")).isTrue();
        assertThat(config.contains("baltop.limit")).isTrue();
        assertThat(config.contains("baltop.cache-minutes")).isTrue();
        assertThat(config.contains("payall.enabled")).isTrue();
    }

    @Test
    @DisplayName("Migration from v1 to v2 removes deprecated auto-update keys")
    void testMigrationRemovesDeprecatedAutoUpdateKeys() {
        FileConfiguration config = plugin.getConfigManager().getConfig();

        // Simulate v1 config with deprecated keys
        config.set("config-version", 1);
        config.set("auto-update", true);
        config.set("runtime-auto-update", true);

        // Run migration
        ConfigMigrator migrator = new ConfigMigrator(plugin);
        migrator.migrate();

        // Deprecated keys should be removed
        assertThat(config.contains("auto-update")).isFalse();
        assertThat(config.contains("runtime-auto-update")).isFalse();
    }

    @Test
    @DisplayName("No migration needed when already at current version")
    void testNoMigrationNeededWhenCurrent() {
        FileConfiguration config = plugin.getConfigManager().getConfig();

        // Config is already at current version (from plugin load)
        int versionBefore = config.getInt("config-version", -1);
        assertThat(versionBefore).isEqualTo(ConfigMigrator.CURRENT_CONFIG_VERSION);

        // Save a key to verify it's not changed by migration
        String existingKey = "baltop.limit";
        int limitBefore = config.getInt(existingKey);

        // Run migration (should be a no-op)
        ConfigMigrator migrator = new ConfigMigrator(plugin);
        migrator.migrate();

        // Config version should remain the same
        assertThat(config.getInt("config-version")).isEqualTo(ConfigMigrator.CURRENT_CONFIG_VERSION);
        // Existing key should not be modified
        assertThat(config.getInt(existingKey)).isEqualTo(limitBefore);
    }

    @Test
    @DisplayName("Migration creates backup before migrating")
    void testBackupIsCreated() {
        FileConfiguration config = plugin.getConfigManager().getConfig();

        // Simulate v1 config to trigger migration
        config.set("config-version", 1);
        plugin.getConfigManager().saveConfig();

        // Run migration
        ConfigMigrator migrator = new ConfigMigrator(plugin);
        migrator.migrate();

        // Check that backup directory exists
        File backupDir = new File(plugin.getDataFolder(), "backups");
        assertThat(backupDir).exists();
        assertThat(backupDir.isDirectory()).isTrue();

        // Check that at least one backup file exists
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.startsWith("config_v1_backup_"));
        assertThat(backupFiles).isNotNull();
        assertThat(backupFiles.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("Migration preserves existing values that are not deprecated")
    void testMigrationPreservesExistingValues() {
        FileConfiguration config = plugin.getConfigManager().getConfig();

        // Set up v1 config with a custom value that should be preserved
        config.set("config-version", 1);
        config.set("storage.type", "MYSQL"); // Custom value, should be preserved

        // Run migration
        ConfigMigrator migrator = new ConfigMigrator(plugin);
        migrator.migrate();

        // Custom value should be preserved
        assertThat(config.getString("storage.type")).isEqualTo("MYSQL");
    }

    @Test
    @DisplayName("Migration does not overwrite existing v2 keys during v1->v2 migration")
    void testMigrationDoesNotOverwriteExistingV2Keys() {
        FileConfiguration config = plugin.getConfigManager().getConfig();

        // Set up v1 config with a custom v2 key already present
        config.set("config-version", 1);
        config.set("updates.check-interval", 43200); // Custom value (default is 21600)

        // Run migration
        ConfigMigrator migrator = new ConfigMigrator(plugin);
        migrator.migrate();

        // The existing value should NOT be overwritten (migration only adds if missing)
        assertThat(config.getInt("updates.check-interval")).isEqualTo(43200);
    }

    @Test
    @DisplayName("CURRENT_CONFIG_VERSION is 2")
    void testCurrentConfigVersionIs2() {
        assertThat(ConfigMigrator.CURRENT_CONFIG_VERSION).isEqualTo(2);
    }
}
