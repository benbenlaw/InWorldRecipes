package com.benbenlaw.inworldrecipes.util;

import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DelayedTaskManager {

    private static final List<ScheduledTask> tasks = new ArrayList<>();

    public static void schedule(Level level, int delayTicks, Runnable action) {
        tasks.add(new ScheduledTask(level, delayTicks, action));
    }

    public static void tick(Level level) {
        Iterator<ScheduledTask> iter = tasks.iterator();
        while (iter.hasNext()) {
            ScheduledTask task = iter.next();
            if (task.level != level) continue;

            task.delay--;
            if (task.delay <= 0) {
                task.action.run();
                iter.remove();
            }
        }
    }

    private static class ScheduledTask {
        final Level level;
        int delay;
        final Runnable action;

        ScheduledTask(Level level, int delay, Runnable action) {
            this.level = level;
            this.delay = delay;
            this.action = action;
        }
    }
}