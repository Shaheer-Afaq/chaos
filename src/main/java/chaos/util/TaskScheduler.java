package chaos.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntConsumer;

public class TaskScheduler {

    private static final List<ScheduledTask> tasks = new ArrayList<>();

    public static void init() {
        ServerTickEvents.START_SERVER_TICK.register(server -> tick());
    }

    public static ScheduledTask schedule(IntConsumer runnable, int delayTicks, int runs, boolean runFirst, Runnable onEnd) { //set runs to a negative value for infinite runs
        ScheduledTask task = new ScheduledTask(runnable, delayTicks, runs,  runFirst, onEnd);
        tasks.add(task);
        if (runFirst){
            runnable.accept(0);
        }
        return task;
    }

    private static void tick() {
        Iterator<ScheduledTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();
            task.ticksLeft--;
            if (task.cancelled) {
                iterator.remove();
            }else if (task.ticksLeft <= 0) {
                task.runnable.accept(task.currentRun);
                task.ticksLeft = task.delayTicks;
                if (task.currentRun < task.runs ) {
                    task.currentRun++;
                }else if (task.currentRun == task.runs) {
                    iterator.remove();
                    if (task.onEnd != null){
                        task.onEnd.run();
                    }
                }
            }
        }
    }
    public static void remove(ScheduledTask task) {
        task.cancelled = true;
        tasks.remove(task);
    }

    public static class ScheduledTask {
        private final IntConsumer runnable;
        public int delayTicks;
        public int ticksLeft;
        public final int runs;
        public int currentRun;
        public boolean cancelled;
        public Runnable onEnd;
        public ScheduledTask(IntConsumer runnable, int delayTicks, int runs, boolean runFirst, Runnable onEnd) {
            this.runnable = runnable;
            this.delayTicks = delayTicks;
//            this.ticksLeft = runFirst ? 0 : delayTicks;
            this.ticksLeft = delayTicks;
            this.runs = runs;
            this.currentRun = 1;
            this.cancelled = false;
            this.onEnd = onEnd;
        }
    }
}
