package net.nikev2.nikev23;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class DelayUtil {
    private static final List<DelayedTask> tasks = new ArrayList<>();

    static {
        // Registering a single tick event listener
        ServerTickEvents.END_SERVER_TICK.register(server -> tick());
    }

    public static void wait(int seconds, Runnable task) {
        tasks.add(new DelayedTask(seconds * 20, task)); // 20 ticks per second
    }

    private static void tick() {
        tasks.forEach(task -> {
            task.tick();
            if(task.isReady()) {
                task.run();
            }
        });
        tasks.removeIf(task -> task.hasFinished());
    }

    private static class DelayedTask {
        private int ticksRemaining;
        private boolean hasFinished;
        private final Runnable task;

        private DelayedTask(int ticks, Runnable task) {
            this.ticksRemaining = ticks;
            this.task = task;
        }

        private void tick() {
            if (ticksRemaining > 0) {
                ticksRemaining--;
            }
        }

        private boolean isReady() {
            return ticksRemaining <= 0;
        }

        private boolean hasFinished() {
            return this.hasFinished;
        }

        private void run() {
            if (task != null) {
                task.run();
            }
            this.hasFinished = true;
        }
    }
}