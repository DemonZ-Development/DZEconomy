package online.demonzdevelopment.dzeconomy;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.data.CurrencyRequest;
import online.demonzdevelopment.dzeconomy.data.PlayerData;
import online.demonzdevelopment.dzeconomy.manager.RankManager;
import online.demonzdevelopment.dzeconomy.storage.StorageProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for CurrencyManager – the core economy logic.
 * <p>
 * Uses MockBukkit for Bukkit API stubs (PlayerData needs Bukkit.getOfflinePlayer())
 * and Mockito to mock the DZEconomy plugin and its dependencies for isolation.
 */
class CurrencyManagerTest {

    private ServerMock server;
    private DZEconomy mockPlugin;
    private StorageProvider mockStorage;
    private RankManager mockRankManager;
    private CurrencyManager currencyManager;
    private FileConfiguration testConfig;

    @BeforeEach
    void setUp() {
        // MockBukkit server needed for PlayerData constructor (Bukkit.getOfflinePlayer)
        server = MockBukkit.mock();

        // Mock the plugin and its dependencies
        mockPlugin = Mockito.mock(DZEconomy.class);
        mockStorage = Mockito.mock(StorageProvider.class);
        mockRankManager = Mockito.mock(RankManager.class);

        when(mockPlugin.getStorageProvider()).thenReturn(mockStorage);
        when(mockPlugin.getRankManager()).thenReturn(mockRankManager);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("CurrencyManagerTest"));

        // Storage returns null by default so CurrencyManager creates fresh PlayerData
        when(mockStorage.loadPlayerData(any(UUID.class))).thenReturn(null);

        // RankManager returns 0% tax by default for predictable testing
        when(mockRankManager.getTransferTaxRate(any(UUID.class), any(CurrencyType.class))).thenReturn(0.0);

        // Real YAML config for conversion rate testing
        testConfig = new YamlConfiguration();
        online.demonzdevelopment.dzeconomy.config.ConfigManager mockConfigManager =
                Mockito.mock(online.demonzdevelopment.dzeconomy.config.ConfigManager.class);
        when(mockPlugin.getConfigManager()).thenReturn(mockConfigManager);
        when(mockConfigManager.getConfig()).thenReturn(testConfig);

        currencyManager = new CurrencyManager(mockPlugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // ━━ Balance Operations ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("addBalance increases balance correctly")
    void testAddBalanceWorks() {
        UUID uuid = UUID.randomUUID();

        boolean result = currencyManager.addBalance(uuid, CurrencyType.MONEY, 100.0);

        assertThat(result).isTrue();
        assertThat(currencyManager.getBalance(uuid, CurrencyType.MONEY)).isEqualTo(100.0);
    }

    @Test
    @DisplayName("addBalance with negative amount returns false")
    void testAddBalanceNegativeAmountFails() {
        UUID uuid = UUID.randomUUID();

        boolean result = currencyManager.addBalance(uuid, CurrencyType.MONEY, -50.0);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("addBalance accumulates across multiple calls")
    void testAddBalanceAccumulates() {
        UUID uuid = UUID.randomUUID();

        currencyManager.addBalance(uuid, CurrencyType.MONEY, 100.0);
        currencyManager.addBalance(uuid, CurrencyType.MONEY, 200.0);

        assertThat(currencyManager.getBalance(uuid, CurrencyType.MONEY)).isEqualTo(300.0);
    }

    @Test
    @DisplayName("removeBalance decreases balance correctly")
    void testRemoveBalanceWorks() {
        UUID uuid = UUID.randomUUID();
        currencyManager.addBalance(uuid, CurrencyType.MONEY, 200.0);

        boolean result = currencyManager.removeBalance(uuid, CurrencyType.MONEY, 50.0);

        assertThat(result).isTrue();
        assertThat(currencyManager.getBalance(uuid, CurrencyType.MONEY)).isEqualTo(150.0);
    }

    @Test
    @DisplayName("removeBalance with insufficient funds returns false")
    void testRemoveBalanceInsufficientFunds() {
        UUID uuid = UUID.randomUUID();
        currencyManager.addBalance(uuid, CurrencyType.MONEY, 50.0);

        boolean result = currencyManager.removeBalance(uuid, CurrencyType.MONEY, 100.0);

        assertThat(result).isFalse();
        // Balance should remain unchanged
        assertThat(currencyManager.getBalance(uuid, CurrencyType.MONEY)).isEqualTo(50.0);
    }

    @Test
    @DisplayName("removeBalance with negative amount returns false")
    void testRemoveBalanceNegativeAmountFails() {
        UUID uuid = UUID.randomUUID();

        boolean result = currencyManager.removeBalance(uuid, CurrencyType.MONEY, -10.0);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("setBalance sets balance correctly")
    void testSetBalanceWorks() {
        UUID uuid = UUID.randomUUID();

        boolean result = currencyManager.setBalance(uuid, CurrencyType.MONEY, 500.0);

        assertThat(result).isTrue();
        assertThat(currencyManager.getBalance(uuid, CurrencyType.MONEY)).isEqualTo(500.0);
    }

    @Test
    @DisplayName("setBalance with negative amount returns false")
    void testSetBalanceNegativeFails() {
        UUID uuid = UUID.randomUUID();

        boolean result = currencyManager.setBalance(uuid, CurrencyType.MONEY, -10.0);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("setBalance overwrites previous balance")
    void testSetBalanceOverwrites() {
        UUID uuid = UUID.randomUUID();
        currencyManager.addBalance(uuid, CurrencyType.MONEY, 100.0);

        currencyManager.setBalance(uuid, CurrencyType.MONEY, 999.0);

        assertThat(currencyManager.getBalance(uuid, CurrencyType.MONEY)).isEqualTo(999.0);
    }

    // ━━ Transfer Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("Transfer between players succeeds atomically")
    void testTransferBetweenPlayers() {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        currencyManager.setBalance(from, CurrencyType.MONEY, 1000.0);
        currencyManager.setBalance(to, CurrencyType.MONEY, 0.0);

        boolean result = currencyManager.transfer(from, to, CurrencyType.MONEY, 100.0);

        assertThat(result).isTrue();
        // With 0% tax, sender loses exactly 100, receiver gets exactly 100
        assertThat(currencyManager.getBalance(from, CurrencyType.MONEY)).isEqualTo(900.0);
        assertThat(currencyManager.getBalance(to, CurrencyType.MONEY)).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Transfer applies tax rate from RankManager")
    void testTransferAppliesTaxRate() {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        currencyManager.setBalance(from, CurrencyType.MONEY, 1000.0);
        currencyManager.setBalance(to, CurrencyType.MONEY, 0.0);

        // Set 10% tax rate
        when(mockRankManager.getTransferTaxRate(any(UUID.class), any(CurrencyType.class))).thenReturn(0.10);

        boolean result = currencyManager.transfer(from, to, CurrencyType.MONEY, 100.0);

        assertThat(result).isTrue();
        // Sender pays full 100
        assertThat(currencyManager.getBalance(from, CurrencyType.MONEY)).isEqualTo(900.0);
        // Receiver gets 100 - 10% = 90
        assertThat(currencyManager.getBalance(to, CurrencyType.MONEY)).isCloseTo(90.0, within(0.001));
    }

    @Test
    @DisplayName("Transfer fails with insufficient funds")
    void testTransferFailsInsufficientFunds() {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        currencyManager.setBalance(from, CurrencyType.MONEY, 50.0);
        currencyManager.setBalance(to, CurrencyType.MONEY, 0.0);

        boolean result = currencyManager.transfer(from, to, CurrencyType.MONEY, 100.0);

        assertThat(result).isFalse();
        assertThat(currencyManager.getBalance(from, CurrencyType.MONEY)).isEqualTo(50.0);
        assertThat(currencyManager.getBalance(to, CurrencyType.MONEY)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Transfer fails for self-transfer")
    void testTransferFailsSelfTransfer() {
        UUID uuid = UUID.randomUUID();
        currencyManager.setBalance(uuid, CurrencyType.MONEY, 1000.0);

        boolean result = currencyManager.transfer(uuid, uuid, CurrencyType.MONEY, 100.0);

        assertThat(result).isFalse();
        assertThat(currencyManager.getBalance(uuid, CurrencyType.MONEY)).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Transfer fails with zero or negative amount")
    void testTransferFailsZeroOrNegativeAmount() {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        currencyManager.setBalance(from, CurrencyType.MONEY, 1000.0);

        assertThat(currencyManager.transfer(from, to, CurrencyType.MONEY, 0.0)).isFalse();
        assertThat(currencyManager.transfer(from, to, CurrencyType.MONEY, -10.0)).isFalse();
    }

    @Test
    @DisplayName("Concurrent transfers don't cause race conditions")
    void testConcurrentTransfersNoRaceCondition() throws Exception {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        currencyManager.setBalance(from, CurrencyType.MONEY, 10000.0);

        int threadCount = 50;
        double transferAmount = 10.0;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startGate.await(); // All threads start at the same time
                    boolean result = currencyManager.transfer(from, to, CurrencyType.MONEY, transferAmount);
                    if (result) successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishGate.countDown();
                }
            });
        }

        startGate.countDown(); // Release all threads simultaneously
        boolean completed = finishGate.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();

        double fromFinal = currencyManager.getBalance(from, CurrencyType.MONEY);
        double toFinal = currencyManager.getBalance(to, CurrencyType.MONEY);
        double totalTransferred = successCount.get() * transferAmount;

        // Money conservation: from + to should equal original balance (10000)
        // With 0% tax: from = 10000 - totalTransferred, to = totalTransferred
        assertThat(fromFinal).isEqualTo(10000.0 - totalTransferred);
        assertThat(toFinal).isCloseTo(totalTransferred, within(0.01));

        // Verify no money was created or destroyed
        assertThat(fromFinal + toFinal).isCloseTo(10000.0, within(0.01));
    }

    // ━━ Convert Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("Convert between currencies applies conversion rate")
    void testConvertBetweenCurrencies() {
        UUID uuid = UUID.randomUUID();
        currencyManager.setBalance(uuid, CurrencyType.MONEY, 1000.0);

        // Set conversion rate: 1 money = 10 mobcoins
        testConfig.set("conversion.rates.money-to-mobcoin", 10.0);
        testConfig.set("conversion.fee-percent", 0.0);

        boolean result = currencyManager.convert(uuid, CurrencyType.MONEY, CurrencyType.MOBCOIN, 100.0);

        assertThat(result).isTrue();
        assertThat(currencyManager.getBalance(uuid, CurrencyType.MONEY)).isEqualTo(900.0);
        assertThat(currencyManager.getBalance(uuid, CurrencyType.MOBCOIN)).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Convert fails for same currency type")
    void testConvertSameCurrencyFails() {
        UUID uuid = UUID.randomUUID();
        currencyManager.setBalance(uuid, CurrencyType.MONEY, 1000.0);

        boolean result = currencyManager.convert(uuid, CurrencyType.MONEY, CurrencyType.MONEY, 100.0);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Convert fails with zero or negative amount")
    void testConvertZeroOrNegativeFails() {
        UUID uuid = UUID.randomUUID();
        currencyManager.setBalance(uuid, CurrencyType.MONEY, 1000.0);

        assertThat(currencyManager.convert(uuid, CurrencyType.MONEY, CurrencyType.MOBCOIN, 0.0)).isFalse();
        assertThat(currencyManager.convert(uuid, CurrencyType.MONEY, CurrencyType.MOBCOIN, -5.0)).isFalse();
    }

    @Test
    @DisplayName("Convert uses default rate of 1.0 when not configured")
    void testConvertDefaultRate() {
        UUID uuid = UUID.randomUUID();
        currencyManager.setBalance(uuid, CurrencyType.MONEY, 500.0);

        // No conversion rate configured, defaults to 1.0
        boolean result = currencyManager.convert(uuid, CurrencyType.MONEY, CurrencyType.GEM, 100.0);

        assertThat(result).isTrue();
        assertThat(currencyManager.getBalance(uuid, CurrencyType.MONEY)).isEqualTo(400.0);
        assertThat(currencyManager.getBalance(uuid, CurrencyType.GEM)).isEqualTo(100.0);
    }

    // ━━ Leaderboard Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("getBalanceTop returns players sorted by balance descending")
    void testGetBalanceTopReturnsSortedResults() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();

        currencyManager.setBalance(player1, CurrencyType.MONEY, 300.0);
        currencyManager.setBalance(player2, CurrencyType.MONEY, 100.0);
        currencyManager.setBalance(player3, CurrencyType.MONEY, 500.0);

        when(mockStorage.getTopBalances(CurrencyType.MONEY.getId(), 3)).thenReturn(List.of(
                new java.util.AbstractMap.SimpleEntry<>(player3, 500.0),
                new java.util.AbstractMap.SimpleEntry<>(player1, 300.0),
                new java.util.AbstractMap.SimpleEntry<>(player2, 100.0)
        ));

        List<Map.Entry<UUID, Double>> top = currencyManager.getBalanceTopAsync(CurrencyType.MONEY, 3).join();

        assertThat(top).hasSize(3);
        // Sorted descending
        assertThat(top.get(0).getKey()).isEqualTo(player3);
        assertThat(top.get(0).getValue()).isEqualTo(500.0);
        assertThat(top.get(1).getKey()).isEqualTo(player1);
        assertThat(top.get(1).getValue()).isEqualTo(300.0);
        assertThat(top.get(2).getKey()).isEqualTo(player2);
        assertThat(top.get(2).getValue()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("getBalanceTop respects limit parameter")
    void testGetBalanceTopRespectsLimit() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();

        currencyManager.setBalance(player1, CurrencyType.MONEY, 300.0);
        currencyManager.setBalance(player2, CurrencyType.MONEY, 100.0);
        currencyManager.setBalance(player3, CurrencyType.MONEY, 500.0);

        when(mockStorage.getTopBalances(CurrencyType.MONEY.getId(), 2)).thenReturn(List.of(
                new java.util.AbstractMap.SimpleEntry<>(player3, 500.0),
                new java.util.AbstractMap.SimpleEntry<>(player1, 300.0)
        ));

        List<Map.Entry<UUID, Double>> top = currencyManager.getBalanceTopAsync(CurrencyType.MONEY, 2).join();

        assertThat(top).hasSize(2);
        assertThat(top.get(0).getValue()).isEqualTo(500.0);
        assertThat(top.get(1).getValue()).isEqualTo(300.0);
    }

    @Test
    @DisplayName("getBalanceTop returns empty list for no players")
    void testGetBalanceTopEmptyWhenNoPlayers() {
        when(mockStorage.getTopBalances(CurrencyType.MONEY.getId(), 10)).thenReturn(List.of());
        List<Map.Entry<UUID, Double>> top = currencyManager.getBalanceTopAsync(CurrencyType.MONEY, 10).join();

        assertThat(top).isEmpty();
    }

    // ━━ Request Management Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("addRequest and removeRequest work correctly")
    void testAddAndRemoveRequest() {
        UUID requester = UUID.randomUUID();
        UUID requested = UUID.randomUUID();
        CurrencyRequest request = new CurrencyRequest(requester, requested, CurrencyType.MONEY, 100.0, 60000L);

        currencyManager.addRequest(request);

        assertThat(currencyManager.getRequestsForPlayer(requested)).hasSize(1);

        boolean removed = currencyManager.removeRequest(requested, request);
        assertThat(removed).isTrue();
        assertThat(currencyManager.getRequestsForPlayer(requested)).isEmpty();
    }

    @Test
    @DisplayName("removeRequest returns false for non-existent request")
    void testRemoveRequestNonExistent() {
        UUID requester = UUID.randomUUID();
        UUID requested = UUID.randomUUID();
        CurrencyRequest request = new CurrencyRequest(requester, requested, CurrencyType.MONEY, 100.0, 60000L);

        boolean removed = currencyManager.removeRequest(requested, request);
        assertThat(removed).isFalse();
    }

    @Test
    @DisplayName("findRequest returns matching request")
    void testFindRequest() {
        UUID requester = UUID.randomUUID();
        UUID requested = UUID.randomUUID();
        CurrencyRequest request = new CurrencyRequest(requester, requested, CurrencyType.MONEY, 100.0, 60000L);

        currencyManager.addRequest(request);

        CurrencyRequest found = currencyManager.findRequest(requester, requested, CurrencyType.MONEY);

        assertThat(found).isNotNull();
        assertThat(found.getRequesterUUID()).isEqualTo(requester);
        assertThat(found.getRequestedPlayerUUID()).isEqualTo(requested);
        assertThat(found.getCurrencyType()).isEqualTo(CurrencyType.MONEY);
        assertThat(found.getAmount()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("findRequest returns null when no match")
    void testFindRequestNoMatch() {
        UUID requester = UUID.randomUUID();
        UUID requested = UUID.randomUUID();

        CurrencyRequest found = currencyManager.findRequest(requester, requested, CurrencyType.MONEY);

        assertThat(found).isNull();
    }

    @Test
    @DisplayName("findRequest differentiates by currency type")
    void testFindRequestDifferentCurrencyType() {
        UUID requester = UUID.randomUUID();
        UUID requested = UUID.randomUUID();
        CurrencyRequest moneyRequest = new CurrencyRequest(requester, requested, CurrencyType.MONEY, 100.0, 60000L);

        currencyManager.addRequest(moneyRequest);

        // Searching for GEM type should not find the MONEY request
        CurrencyRequest found = currencyManager.findRequest(requester, requested, CurrencyType.GEM);
        assertThat(found).isNull();
    }

    // ━━ Multi-Currency Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("Balances are independent per currency type")
    void testBalancesIndependentPerCurrency() {
        UUID uuid = UUID.randomUUID();

        currencyManager.addBalance(uuid, CurrencyType.MONEY, 100.0);
        currencyManager.addBalance(uuid, CurrencyType.MOBCOIN, 50.0);
        currencyManager.addBalance(uuid, CurrencyType.GEM, 25.0);

        assertThat(currencyManager.getBalance(uuid, CurrencyType.MONEY)).isEqualTo(100.0);
        assertThat(currencyManager.getBalance(uuid, CurrencyType.MOBCOIN)).isEqualTo(50.0);
        assertThat(currencyManager.getBalance(uuid, CurrencyType.GEM)).isEqualTo(25.0);

        // Removing from one currency doesn't affect others
        currencyManager.removeBalance(uuid, CurrencyType.MONEY, 30.0);
        assertThat(currencyManager.getBalance(uuid, CurrencyType.MONEY)).isEqualTo(70.0);
        assertThat(currencyManager.getBalance(uuid, CurrencyType.MOBCOIN)).isEqualTo(50.0);
        assertThat(currencyManager.getBalance(uuid, CurrencyType.GEM)).isEqualTo(25.0);
    }
}
