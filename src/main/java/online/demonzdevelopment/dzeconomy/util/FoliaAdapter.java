package online.demonzdevelopment.dzeconomy.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Adapter class providing Folia-compatible task scheduling.
 * <p>
 * Automatically detects whether the server is running Folia (region-based threading)
 * or standard Bukkit/Paper/Spigot and delegates to the appropriate scheduler API.
 * <p>
 * On Folia, uses:
 * <ul>
 *   <li>{@code Bukkit.getGlobalRegionScheduler()} for global tasks</li>
 *   <li>{@code Bukkit.getRegionScheduler()} for location-based tasks</li>
 *   <li>{@code Entity.getScheduler()} for entity-bound tasks</li>
 * </ul>
 * On non-Folia, falls back to {@code Bukkit.getScheduler()}.
 * <p>
 * All scheduling methods take {@link JavaPlugin} as the first parameter for ownership tracking.
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * // Run a task on next tick (works on both Folia and standard Bukkit)
 * FoliaAdapter.runTask(plugin, () -> {
 *     player.sendMessage("Hello!");
 * });
 *
 * // Run at a specific location (uses region scheduler on Folia)
 * FoliaAdapter.runAtLocation(plugin, location, () -> {
 *     location.getWorld().strikeLightning(location);
 * });
 *
 * // Run at an entity (uses entity scheduler on Folia)
 * FoliaAdapter.runAtEntity(plugin, entity, () -> {
 *     entity.remove();
 * });
 * }</pre>
 *
 * @since 2.0.0
 */
public final class FoliaAdapter {

    /** Whether the server is running Folia (region-based threading). */
    private static final boolean FOLIA;

    // ---- Cached reflection methods for Folia schedulers ----
    // These are initialized only when Folia is detected to avoid
    // ClassNotFoundException on standard Bukkit/Paper/Spigot.

    private static Method getGlobalRegionSchedulerMethod;
    private static Method getRegionSchedulerMethod;

    // GlobalRegionScheduler methods
    private static Method globalRunDelayedMethod;
    private static Method globalRunAtFixedRateMethod;

    // RegionScheduler methods
    private static Method regionRunDelayedMethod;
    private static Method regionRunAtFixedRateMethod;

    // EntityScheduler methods
    private static Method entityGetSchedulerMethod;
    private static Method entityRunDelayedMethod;
    private static Method entityRunAtFixedRateMethod;

    // ScheduledTask.cancel() method (Folia)
    private static Method scheduledTaskCancelMethod;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;

        if (FOLIA) {
            initFoliaMethods();
        }
    }

    private FoliaAdapter() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    /**
     * Initializes cached reflection method handles for the Folia scheduler APIs.
     * Called once during class loading if Folia is detected.
     *
     * @throws RuntimeException if any expected Folia API method is missing,
     *                          indicating an incompatible Folia version
     */
    private static void initFoliaMethods() {
        try {
            // Bukkit accessor methods
            getGlobalRegionSchedulerMethod = Bukkit.class.getMethod("getGlobalRegionScheduler");
            getRegionSchedulerMethod = Bukkit.class.getMethod("getRegionScheduler");

            // GlobalRegionScheduler: runDelayed(Plugin, Consumer<ScheduledTask>, long)
            Class<?> globalRegionSchedulerClass = getGlobalRegionSchedulerMethod.getReturnType();
            globalRunDelayedMethod = globalRegionSchedulerClass.getMethod(
                    "runDelayed", Plugin.class, Consumer.class, long.class
            );
            globalRunAtFixedRateMethod = globalRegionSchedulerClass.getMethod(
                    "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class
            );

            // RegionScheduler: runDelayed(Plugin, Location, Consumer<ScheduledTask>, long)
            Class<?> regionSchedulerClass = getRegionSchedulerMethod.getReturnType();
            regionRunDelayedMethod = regionSchedulerClass.getMethod(
                    "runDelayed", Plugin.class, Location.class, Consumer.class, long.class
            );
            regionRunAtFixedRateMethod = regionSchedulerClass.getMethod(
                    "runAtFixedRate", Plugin.class, Location.class, Consumer.class, long.class, long.class
            );

            // EntityScheduler (accessed via Entity.getScheduler())
            entityGetSchedulerMethod = Entity.class.getMethod("getScheduler");
            Class<?> entitySchedulerClass = entityGetSchedulerMethod.getReturnType();
            // runDelayed(Plugin, Consumer<ScheduledTask>, Runnable retirement, long delay)
            entityRunDelayedMethod = entitySchedulerClass.getMethod(
                    "runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class
            );
            // runAtFixedRate(Plugin, Consumer<ScheduledTask>, Runnable retirement, long initialDelay, long period)
            entityRunAtFixedRateMethod = entitySchedulerClass.getMethod(
                    "runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class
            );

            // ScheduledTask.cancel() — retrieve from the return type of runDelayed
            Class<?> scheduledTaskClass = globalRunDelayedMethod.getReturnType();
            scheduledTaskCancelMethod = scheduledTaskClass.getMethod("cancel");

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Failed to initialize Folia scheduler methods. " +
                    "The server may be running an incompatible Folia version.", e
            );
        }
    }

    // ========================================================================
    // Public API
    // ========================================================================

    /**
     * Returns whether the server is running Folia (region-based threading).
     * <p>
     * Detection is performed by checking for the presence of the Folia-specific
     * class {@code io.papermc.paper.threadedregions.RegionizedServer} at class-loading time.
     *
     * @return {@code true} if Folia is detected, {@code false} otherwise
     */
    public static boolean isFolia() {
        return FOLIA;
    }

    /**
     * Runs a task on the next tick on the global region (Folia) or
     * main thread (standard Bukkit).
     *
     * @param plugin the plugin that owns this task
     * @param task   the task to execute
     * @return a {@link FoliaTask} handle, or {@code null} on failure
     */
    public static @Nullable FoliaTask runTask(@NotNull JavaPlugin plugin, @NotNull Runnable task) {
        if (FOLIA) {
            return foliaRunDelayedGlobal(plugin, task, 1L);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTask(plugin, task);
            return new FoliaTask(bukkitTask);
        }
    }

    /**
     * Runs a task asynchronously. This works identically on both Folia and
     * standard Bukkit, as async scheduling is not region-dependent.
     *
     * @param plugin the plugin that owns this task
     * @param task   the task to execute
     * @return a {@link FoliaTask} handle
     */
    public static @NotNull FoliaTask runTaskAsynchronously(@NotNull JavaPlugin plugin, @NotNull Runnable task) {
        if (FOLIA) {
            try {
                Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                Method runNowMethod = asyncScheduler.getClass().getMethod("runNow", Plugin.class, Consumer.class);
                Object scheduledTask = runNowMethod.invoke(asyncScheduler, plugin, wrapAsConsumer(task));
                return new FoliaTask(scheduledTask);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to run async task on Folia", e);
            }
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        return new FoliaTask(bukkitTask);
    }

    /**
     * Runs a repeating task asynchronously. Works on both Folia and standard Bukkit.
     *
     * @param plugin      the plugin that owns this task
     * @param task        the task to execute
     * @param delayTicks  the delay in ticks before the first run
     * @param periodTicks the period in ticks between subsequent runs
     * @return a {@link FoliaTask} handle, or {@code null} on failure
     */
    public static @Nullable FoliaTask runTaskTimerAsynchronously(@NotNull JavaPlugin plugin, @NotNull Runnable task,
                                                                  long delayTicks, long periodTicks) {
        if (FOLIA) {
            try {
                Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                Method runAtFixedRateMethod = asyncScheduler.getClass().getMethod(
                        "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, java.util.concurrent.TimeUnit.class
                );
                long delayMs = delayTicks * 50L;
                long periodMs = periodTicks * 50L;
                Object scheduledTask = runAtFixedRateMethod.invoke(
                        asyncScheduler, plugin, wrapAsConsumer(task), delayMs, periodMs, java.util.concurrent.TimeUnit.MILLISECONDS
                );
                return scheduledTask != null ? new FoliaTask(scheduledTask) : null;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to schedule async timer task on Folia", e);
            }
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        return new FoliaTask(bukkitTask);
    }

    /**
     * Runs a task after a specified delay on the global region (Folia) or
     * main thread (standard Bukkit).
     *
     * @param plugin the plugin that owns this task
     * @param task   the task to execute
     * @param delay  the delay in ticks before the task runs (must be &gt;= 1 on Folia)
     * @return a {@link FoliaTask} handle, or {@code null} on failure
     */
    public static @Nullable FoliaTask runTaskLater(@NotNull JavaPlugin plugin, @NotNull Runnable task, long delay) {
        if (FOLIA) {
            return foliaRunDelayedGlobal(plugin, task, Math.max(1L, delay));
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delay);
            return new FoliaTask(bukkitTask);
        }
    }

    /**
     * Runs a repeating task starting after the specified delay, repeating at
     * the given period, on the global region (Folia) or main thread (standard Bukkit).
     *
     * @param plugin the plugin that owns this task
     * @param task   the task to execute
     * @param delay  the delay in ticks before the first run (must be &gt;= 1 on Folia)
     * @param period the period in ticks between subsequent runs (must be &gt;= 1 on Folia)
     * @return a {@link FoliaTask} handle, or {@code null} on failure
     */
    public static @Nullable FoliaTask runTaskTimer(@NotNull JavaPlugin plugin, @NotNull Runnable task,
                                                    long delay, long period) {
        if (FOLIA) {
            return foliaRunAtFixedRateGlobal(plugin, task, Math.max(1L, delay), Math.max(1L, period));
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
            return new FoliaTask(bukkitTask);
        }
    }

    /**
     * Runs a task on the region that owns the specified entity.
     * <p>
     * On Folia, this uses the entity's scheduler to ensure the task runs
     * on the correct region thread for the entity.
     * On standard Bukkit, this simply runs on the main thread.
     *
     * @param plugin the plugin that owns this task
     * @param entity the entity whose region should run the task
     * @param task   the task to execute
     * @return a {@link FoliaTask} handle, or {@code null} on failure or if the entity is retired
     */
    public static @Nullable FoliaTask runAtEntity(@NotNull JavaPlugin plugin, @NotNull Entity entity,
                                                   @NotNull Runnable task) {
        if (FOLIA) {
            return foliaRunDelayedEntity(plugin, entity, task, 1L);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTask(plugin, task);
            return new FoliaTask(bukkitTask);
        }
    }

    /**
     * Runs a task on the region that owns the specified location.
     * <p>
     * On Folia, this uses the region scheduler to ensure the task runs
     * on the correct region thread for the location.
     * On standard Bukkit, this simply runs on the main thread.
     *
     * @param plugin   the plugin that owns this task
     * @param location the location whose region should run the task
     * @param task     the task to execute
     * @return a {@link FoliaTask} handle, or {@code null} on failure
     */
    public static @Nullable FoliaTask runAtLocation(@NotNull JavaPlugin plugin, @NotNull Location location,
                                                     @NotNull Runnable task) {
        if (FOLIA) {
            return foliaRunDelayedRegion(plugin, location, task, 1L);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTask(plugin, task);
            return new FoliaTask(bukkitTask);
        }
    }

    /**
     * Cancels all tasks owned by the specified plugin across all schedulers.
     * <p>
     * On Folia, this cancels tasks in both the global and region schedulers.
     * On standard Bukkit, this delegates to {@code Bukkit.getScheduler().cancelTasks()}.
     *
     * @param plugin the plugin whose tasks to cancel
     */
    public static void cancelTasks(@NotNull JavaPlugin plugin) {
        if (FOLIA) {
            try {
                Object globalScheduler = getGlobalRegionSchedulerMethod.invoke(null);
                globalScheduler.getClass()
                        .getMethod("cancelTasks", Plugin.class)
                        .invoke(globalScheduler, plugin);

                Object regionScheduler = getRegionSchedulerMethod.invoke(null);
                regionScheduler.getClass()
                        .getMethod("cancelTasks", Plugin.class)
                        .invoke(regionScheduler, plugin);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to cancel Folia tasks", e);
            }
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }

    // ========================================================================
    // Folia internal helpers
    // ========================================================================

    /**
     * Wraps a plain {@link Runnable} into a {@code Consumer<ScheduledTask>} for
     * Folia scheduler APIs that require the consumer pattern.
     * The ScheduledTask parameter is discarded; the wrapper simply invokes the runnable.
     */
    private static @NotNull Consumer<Object> wrapAsConsumer(@NotNull Runnable task) {
        return scheduledTask -> task.run();
    }

    /** No-op retirement callback for entity scheduler calls. */
    private static final Runnable NO_OP_RETIREMENT = () -> {};

    /**
     * Schedules a delayed task on Folia's {@code GlobalRegionScheduler}.
     */
    private static @Nullable FoliaTask foliaRunDelayedGlobal(@NotNull JavaPlugin plugin,
                                                              @NotNull Runnable task, long delay) {
        try {
            Object scheduler = getGlobalRegionSchedulerMethod.invoke(null);
            Object scheduledTask = globalRunDelayedMethod.invoke(
                    scheduler, plugin, wrapAsConsumer(task), delay
            );
            return scheduledTask != null ? new FoliaTask(scheduledTask) : null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to schedule global delayed task on Folia", e);
            return null;
        }
    }

    /**
     * Schedules a fixed-rate (repeating) task on Folia's {@code GlobalRegionScheduler}.
     */
    private static @Nullable FoliaTask foliaRunAtFixedRateGlobal(@NotNull JavaPlugin plugin,
                                                                  @NotNull Runnable task,
                                                                  long initialDelay, long period) {
        try {
            Object scheduler = getGlobalRegionSchedulerMethod.invoke(null);
            Object scheduledTask = globalRunAtFixedRateMethod.invoke(
                    scheduler, plugin, wrapAsConsumer(task), initialDelay, period
            );
            return scheduledTask != null ? new FoliaTask(scheduledTask) : null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to schedule global timer task on Folia", e);
            return null;
        }
    }

    /**
     * Schedules a delayed task on Folia's {@code RegionScheduler} at a specific location.
     */
    private static @Nullable FoliaTask foliaRunDelayedRegion(@NotNull JavaPlugin plugin,
                                                              @NotNull Location location,
                                                              @NotNull Runnable task, long delay) {
        try {
            Object scheduler = getRegionSchedulerMethod.invoke(null);
            Object scheduledTask = regionRunDelayedMethod.invoke(
                    scheduler, plugin, location, wrapAsConsumer(task), delay
            );
            return scheduledTask != null ? new FoliaTask(scheduledTask) : null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to schedule region task on Folia", e);
            return null;
        }
    }

    /**
     * Schedules a delayed task on Folia's {@code EntityScheduler} for a specific entity.
     */
    private static @Nullable FoliaTask foliaRunDelayedEntity(@NotNull JavaPlugin plugin,
                                                              @NotNull Entity entity,
                                                              @NotNull Runnable task, long delay) {
        try {
            Object entityScheduler = entityGetSchedulerMethod.invoke(entity);
            Object scheduledTask = entityRunDelayedMethod.invoke(
                    entityScheduler, plugin, wrapAsConsumer(task), NO_OP_RETIREMENT, delay
            );
            return scheduledTask != null ? new FoliaTask(scheduledTask) : null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to schedule entity task on Folia", e);
            return null;
        }
    }

    // ========================================================================
    // FoliaTask wrapper
    // ========================================================================

    /**
     * A unified task handle that works on both Folia and standard Bukkit.
     * <p>
     * On standard Bukkit, wraps a {@link BukkitTask}.
     * On Folia, wraps a {@code io.papermc.paper.threadedregions.scheduler.ScheduledTask}.
     * <p>
     * Provides a single {@link #cancel()} method regardless of the underlying implementation.
     */
    public static final class FoliaTask {

        private final Object task;

        private FoliaTask(@NotNull Object task) {
            this.task = task;
        }

        /**
         * Cancels this task. If the task has already been cancelled or has
         * completed execution, this call is a no-op.
         */
        public void cancel() {
            if (task == null) return;

            if (FOLIA) {
                try {
                    scheduledTaskCancelMethod.invoke(task);
                } catch (Exception e) {
                    // Silently fail — task may have already completed or been cancelled
                }
            } else {
                ((BukkitTask) task).cancel();
            }
        }

        /**
         * Returns the underlying raw task object.
         * <p>
         * On standard Bukkit, this is a {@link BukkitTask}.
         * On Folia, this is a {@code io.papermc.paper.threadedregions.scheduler.ScheduledTask}.
         *
         * @return the raw task object, may be {@code null}
         */
        public @Nullable Object getRawTask() {
            return task;
        }

        /**
         * Returns whether this task handle wraps a BukkitTask (non-Folia).
         *
         * @return {@code true} if the underlying task is a BukkitTask
         */
        public boolean isBukkitTask() {
            return task instanceof BukkitTask;
        }
    }
}
