package scheduledExecutor;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.concurrent.TimeUnit;

@SuperBuilder
@Getter
public class Task {
    private Runnable runnable;
    private Long scheduleTime;
    private TimeUnit unit;

    public static Task createTask(Runnable runnable, long delay, TimeUnit unit) {
        return Task.builder()
                .runnable(runnable)
                .scheduleTime(System.currentTimeMillis() + unit.toMillis(delay))
                .unit(unit)
                .build();
    }

    public Task nextTask() {
        return this;
    }

    @SuperBuilder
    @Getter
    public static class FixedRateTask extends Task {
        private long period;

        public static Task createTask(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
            return FixedRateTask.builder()
                    .runnable(runnable)
                    .scheduleTime(System.currentTimeMillis() + unit.toMillis(initialDelay))
                    .unit(unit)
                    .period(period)
                    .build();
        }

        @Override
        public Task nextTask() {
            return FixedRateTask.builder()
                    .runnable(this.getRunnable())
                    .scheduleTime(System.currentTimeMillis() + this.getUnit().toMillis(this.getPeriod()))
                    .unit(this.getUnit())
                    .period(this.getPeriod())
                    .build();
        }
    }

    @SuperBuilder
    @Getter
    public static class FixedDelayTask extends Task {
        private long delay;

        public static Task createTask(Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
            return FixedDelayTask.builder()
                    .runnable(runnable)
                    .scheduleTime(System.currentTimeMillis() + unit.toMillis(initialDelay))
                    .unit(unit)
                    .delay(delay)
                    .build();
        }

        @Override
        public Task nextTask() {
            return FixedDelayTask.builder()
                    .runnable(this.getRunnable())
                    .scheduleTime(System.currentTimeMillis() + this.getUnit().toMillis(this.getDelay()))
                    .unit(this.getUnit())
                    .delay(this.getDelay())
                    .build();
        }
    }
}
