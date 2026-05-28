package online.demonzdevelopment.dzeconomy;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.data.PlayerData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the PlayerData model – the per-player data store.
 * Uses MockBukkit for Bukkit.getOfflinePlayer() in the PlayerData constructor.
 */
class PlayerDataTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // ━━ Initialization Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("PlayerData initializes with default balances from CurrencyType")
    void testBalanceInitialization() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        // CurrencyType default balances are all 0.0
        for (CurrencyType type : CurrencyType.values()) {
            assertThat(data.getBalance(type)).isEqualTo(type.getDefaultBalance());
        }
    }

    @Test
    @DisplayName("PlayerData stores the correct UUID")
    void testUuidInitialization() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        assertThat(data.getUuid()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("PlayerData initializes daily counts to zero")
    void testDailyCountsInitialization() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        for (CurrencyType type : CurrencyType.values()) {
            assertThat(data.getDailySendCount(type)).isEqualTo(0L);
            assertThat(data.getDailyRequestCount(type)).isEqualTo(0L);
        }
    }

    // ━━ Balance Operation Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("addBalance increases balance atomically")
    void testAddBalanceAtomic() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        double result = data.addBalance(CurrencyType.MONEY, 100.0);

        assertThat(result).isEqualTo(100.0);
        assertThat(data.getBalance(CurrencyType.MONEY)).isEqualTo(100.0);
    }

    @Test
    @DisplayName("addBalance returns current balance for negative amount")
    void testAddBalanceNegativeReturnsCurrent() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);
        data.setBalance(CurrencyType.MONEY, 50.0);

        double result = data.addBalance(CurrencyType.MONEY, -10.0);

        // Negative amount is rejected, balance unchanged
        assertThat(result).isEqualTo(50.0);
        assertThat(data.getBalance(CurrencyType.MONEY)).isEqualTo(50.0);
    }

    @Test
    @DisplayName("addBalance accumulates correctly across multiple calls")
    void testAddBalanceAccumulates() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        data.addBalance(CurrencyType.MONEY, 100.0);
        data.addBalance(CurrencyType.MONEY, 200.0);
        data.addBalance(CurrencyType.MONEY, 50.0);

        assertThat(data.getBalance(CurrencyType.MONEY)).isEqualTo(350.0);
    }

    @Test
    @DisplayName("removeBalance prevents negative balance")
    void testRemoveBalancePreventsNegative() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);
        data.setBalance(CurrencyType.MONEY, 50.0);

        double result = data.removeBalance(CurrencyType.MONEY, 200.0);

        // Balance is clamped to 0, not allowed to go negative
        assertThat(result).isEqualTo(0.0);
        assertThat(data.getBalance(CurrencyType.MONEY)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("removeBalance returns current balance for negative amount")
    void testRemoveBalanceNegativeReturnsCurrent() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);
        data.setBalance(CurrencyType.MONEY, 50.0);

        double result = data.removeBalance(CurrencyType.MONEY, -10.0);

        assertThat(result).isEqualTo(50.0);
        assertThat(data.getBalance(CurrencyType.MONEY)).isEqualTo(50.0);
    }

    @Test
    @DisplayName("setBalance clamps negative values to zero")
    void testSetBalanceClampsNegative() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        data.setBalance(CurrencyType.MONEY, -100.0);

        assertThat(data.getBalance(CurrencyType.MONEY)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Concurrent addBalance operations are thread-safe")
    void testConcurrentAddBalance() throws Exception {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        int threadCount = 100;
        double amountPerThread = 10.0;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    data.addBalance(CurrencyType.MONEY, amountPerThread);
                    successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishGate.countDown();
                }
            });
        }

        startGate.countDown();
        boolean completed = finishGate.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(data.getBalance(CurrencyType.MONEY)).isEqualTo(threadCount * amountPerThread);
    }

    // ━━ Daily Count Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("incrementDailySendCount increments correctly")
    void testDailySendCountIncrements() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        assertThat(data.getDailySendCount(CurrencyType.MONEY)).isEqualTo(0L);

        long count1 = data.incrementDailySendCount(CurrencyType.MONEY);
        assertThat(count1).isEqualTo(1L);
        assertThat(data.getDailySendCount(CurrencyType.MONEY)).isEqualTo(1L);

        long count2 = data.incrementDailySendCount(CurrencyType.MONEY);
        assertThat(count2).isEqualTo(2L);
        assertThat(data.getDailySendCount(CurrencyType.MONEY)).isEqualTo(2L);
    }

    @Test
    @DisplayName("incrementDailyRequestCount increments correctly")
    void testDailyRequestCountIncrements() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        assertThat(data.getDailyRequestCount(CurrencyType.MOBCOIN)).isEqualTo(0L);

        data.incrementDailyRequestCount(CurrencyType.MOBCOIN);
        data.incrementDailyRequestCount(CurrencyType.MOBCOIN);
        data.incrementDailyRequestCount(CurrencyType.MOBCOIN);

        assertThat(data.getDailyRequestCount(CurrencyType.MOBCOIN)).isEqualTo(3L);
    }

    @Test
    @DisplayName("resetDailyCounts resets all daily counters to zero")
    void testResetDailyCounts() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        // Increment some daily counts
        data.incrementDailySendCount(CurrencyType.MONEY);
        data.incrementDailySendCount(CurrencyType.MONEY);
        data.incrementDailyRequestCount(CurrencyType.MOBCOIN);

        assertThat(data.getDailySendCount(CurrencyType.MONEY)).isEqualTo(2L);
        assertThat(data.getDailyRequestCount(CurrencyType.MOBCOIN)).isEqualTo(1L);

        // Reset
        data.resetDailyCounts();

        // All daily counts should be zero
        for (CurrencyType type : CurrencyType.values()) {
            assertThat(data.getDailySendCount(type)).isEqualTo(0L);
            assertThat(data.getDailyRequestCount(type)).isEqualTo(0L);
        }
    }

    // ━━ Money Sent/Received Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("addMoneySent and addMoneyReceived accumulate correctly")
    void testMoneySentReceived() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        data.addMoneySent(CurrencyType.MONEY, 100.0);
        data.addMoneySent(CurrencyType.MONEY, 50.0);
        data.addMoneyReceived(CurrencyType.MONEY, 75.0);

        assertThat(data.getMoneySent(CurrencyType.MONEY)).isEqualTo(150.0);
        assertThat(data.getMoneyReceived(CurrencyType.MONEY)).isEqualTo(75.0);
    }

    // ━━ Unmodifiable View Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("getBalances returns unmodifiable view")
    void testGetBalancesUnmodifiable() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);
        data.addBalance(CurrencyType.MONEY, 100.0);

        Map<CurrencyType, Double> balances = data.getBalances();

        assertThat(balances.get(CurrencyType.MONEY)).isEqualTo(100.0);

        // Attempting to modify should throw
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                balances.put(CurrencyType.MONEY, 999.0)
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    // ━━ Username Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("Username can be set and retrieved")
    void testUsernameSetterGetter() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        data.setUsername("TestPlayer");

        assertThat(data.getUsername()).isEqualTo("TestPlayer");
    }

    // ━━ Timestamp Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("firstJoin and lastSeen are set on creation")
    void testTimestampsOnCreation() {
        UUID uuid = UUID.randomUUID();
        long before = System.currentTimeMillis();

        PlayerData data = new PlayerData(uuid);

        long after = System.currentTimeMillis();
        assertThat(data.getFirstJoin()).isBetween(before, after);
        assertThat(data.getLastSeen()).isBetween(before, after);
    }

    @Test
    @DisplayName("lastSeen can be updated")
    void testLastSeenUpdate() {
        UUID uuid = UUID.randomUUID();
        PlayerData data = new PlayerData(uuid);

        long newLastSeen = System.currentTimeMillis() + 10000;
        data.setLastSeen(newLastSeen);

        assertThat(data.getLastSeen()).isEqualTo(newLastSeen);
    }
}
