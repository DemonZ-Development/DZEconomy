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

public final class FoliaAdapter {

    private static final boolean FOLIA;

    private static Method getGlobalRegionSchedulerMethod;
    private static Method getRegionSchedulerMethod;

    private static Method globalRunDelayedMethod;
    private static Method globalRunAtFixedRateMethod;

    private static Method regionRunDelayedMethod;
    private static Method regionRunAtFixedRateMethod;

    private static Method entityGetSchedulerMethod;
    private static Method entityRunDelayedMethod;
    private static Method entityRunAtFixedRateMethod;

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

    private static void initFoliaMethods() {
        try {
            
            getGlobalRegionSchedulerMethod = Bukkit.class.getMethod("getGlobalRegionScheduler");
            getRegionSchedulerMethod = Bukkit.class.getMethod("getRegionScheduler");

            Class<?> globalRegionSchedulerClass = getGlobalRegionSchedulerMethod.getReturnType();
            globalRunDelayedMethod = globalRegionSchedulerClass.getMethod(
                    "runDelayed", Plugin.class, Consumer.class, long.class
            );
            globalRunAtFixedRateMethod = globalRegionSchedulerClass.getMethod(
                    "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class
            );

            Class<?> regionSchedulerClass = getRegionSchedulerMethod.getReturnType();
            regionRunDelayedMethod = regionSchedulerClass.getMethod(
                    "runDelayed", Plugin.class, Location.class, Consumer.class, long.class
            );
            regionRunAtFixedRateMethod = regionSchedulerClass.getMethod(
                    "runAtFixedRate", Plugin.class, Location.class, Consumer.class, long.class, long.class
            );

            entityGetSchedulerMethod = Entity.class.getMethod("getScheduler");
            Class<?> entitySchedulerClass = entityGetSchedulerMethod.getReturnType();
            
            entityRunDelayedMethod = entitySchedulerClass.getMethod(
                    "runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class
            );
            
            entityRunAtFixedRateMethod = entitySchedulerClass.getMethod(
                    "runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class
            );

            Class<?> scheduledTaskClass = globalRunDelayedMethod.getReturnType();
            scheduledTaskCancelMethod = scheduledTaskClass.getMethod("cancel");

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Failed to initialize Folia scheduler methods. " +
                    "The server may be running an incompatible Folia version.", e
            );
        }
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    public static @Nullable FoliaTask runTask(@NotNull JavaPlugin plugin, @NotNull Runnable task) {
        if (FOLIA) {
            return foliaRunDelayedGlobal(plugin, task, 1L);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTask(plugin, task);
            return new FoliaTask(bukkitTask);
        }
    }

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

    public static @Nullable FoliaTask runTaskLater(@NotNull JavaPlugin plugin, @NotNull Runnable task, long delay) {
        if (FOLIA) {
            return foliaRunDelayedGlobal(plugin, task, Math.max(1L, delay));
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delay);
            return new FoliaTask(bukkitTask);
        }
    }

    public static @Nullable FoliaTask runTaskTimer(@NotNull JavaPlugin plugin, @NotNull Runnable task,
                                                    long delay, long period) {
        if (FOLIA) {
            return foliaRunAtFixedRateGlobal(plugin, task, Math.max(1L, delay), Math.max(1L, period));
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
            return new FoliaTask(bukkitTask);
        }
    }

    public static @Nullable FoliaTask runAtEntity(@NotNull JavaPlugin plugin, @NotNull Entity entity,
                                                   @NotNull Runnable task) {
        if (FOLIA) {
            return foliaRunDelayedEntity(plugin, entity, task, 1L);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTask(plugin, task);
            return new FoliaTask(bukkitTask);
        }
    }

    public static @Nullable FoliaTask runAtLocation(@NotNull JavaPlugin plugin, @NotNull Location location,
                                                     @NotNull Runnable task) {
        if (FOLIA) {
            return foliaRunDelayedRegion(plugin, location, task, 1L);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTask(plugin, task);
            return new FoliaTask(bukkitTask);
        }
    }

    public static void cancelTasks(@NotNull JavaPlugin plugin) {
        if (FOLIA) {
            try {
                Object globalScheduler = getGlobalRegionSchedulerMethod.invoke(null);
                globalScheduler.getClass()
                        .getMethod("cancelTasks", Plugin.class)
                        .invoke(globalScheduler, plugin);

                try {
                    Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                    asyncScheduler.getClass()
                            .getMethod("cancelTasks", Plugin.class)
                            .invoke(asyncScheduler, plugin);
                } catch (Exception ignored) {
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to cancel Folia tasks", e);
            }
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }

    private static @NotNull Consumer<Object> wrapAsConsumer(@NotNull Runnable task) {
        return scheduledTask -> task.run();
    }

    private static final Runnable NO_OP_RETIREMENT = () -> {};

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

    public static final class FoliaTask {

        private final Object task;

        private FoliaTask(@NotNull Object task) {
            this.task = task;
        }

        public void cancel() {
            if (task == null) return;

            if (FOLIA) {
                try {
                    scheduledTaskCancelMethod.invoke(task);
                } catch (Exception e) {
                    
                }
            } else {
                ((BukkitTask) task).cancel();
            }
        }

        public @Nullable Object getRawTask() {
            return task;
        }

        public boolean isBukkitTask() {
            return task instanceof BukkitTask;
        }
    }
}
