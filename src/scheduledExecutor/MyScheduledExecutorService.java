package scheduledExecutor;

import lombok.SneakyThrows;
import scheduledExecutor.Task.FixedDelayTask;
import scheduledExecutor.Task.FixedRateTask;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyScheduledExecutorService implements ScheduledExecutorService {
    private final PriorityQueue<Task> tasks;
    private final Worker worker;
    private final Lock lock = new ReentrantLock();
    private final Condition taskAdded = lock.newCondition();

    private MyScheduledExecutorService() {
        this.tasks = new PriorityQueue<>(Comparator.comparing(Task::getScheduleTime));
        this.worker = new Worker();
    }

    public static MyScheduledExecutorService create() {
        MyScheduledExecutorService scheduledExecutorService = new MyScheduledExecutorService();
        Thread workerThread = new Thread(scheduledExecutorService.worker);
        workerThread.start();
        return scheduledExecutorService;
    }

    @Override
    public void schedule(Runnable command, long delay, TimeUnit unit) {
        lock.lock();
        try {
            tasks.add(Task.createTask(command, delay, unit));
            taskAdded.signalAll();
        } catch (Exception ignored) {

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        lock.lock();
        try {
            tasks.add(FixedRateTask.createTask(command, initialDelay, period, unit));
            taskAdded.signalAll();
        } catch (Exception ignored) {

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        lock.lock();
        try {
            tasks.add(FixedDelayTask.createTask(command, initialDelay, delay, unit));
            taskAdded.signalAll();
        } catch (Exception ignored) {

        } finally {
            lock.unlock();
        }
    }

    private class Worker implements Runnable {
        private final TaskHandlerFactory taskHandlerFactory;

        Worker() {
            this.taskHandlerFactory = new TaskHandlerFactory();
        }

        @SneakyThrows
        @Override
        public void run() {
            long sleep;

            while (true) {
                lock.lock();

                try {
                    while (tasks.isEmpty())
                        taskAdded.await();

                    while (!tasks.isEmpty()) {
                        sleep = tasks.peek().getScheduleTime() - System.currentTimeMillis();

                        if (sleep <= 0)
                            break;

                        taskAdded.await(sleep, TimeUnit.MILLISECONDS);
                    }

                    Task task = tasks.poll();
                    taskHandlerFactory.getTaskHandler(task.getClass()).handleTask(task);
                } catch (Exception ignored) {

                } finally {
                    lock.unlock();
                }
            }
        }
    }

    private static class TaskHandler {
        protected final ExecutorService executor;

        TaskHandler(int threadPoolSize) {
            executor = Executors.newFixedThreadPool(threadPoolSize);
        }

        void handleTask(Task task) {
            System.out.println("here1");
            executor.submit(task.getRunnable());
        }
    }

    private class FixedRateTaskHandler extends TaskHandler {

        FixedRateTaskHandler(int threadPoolSize) {
            super(threadPoolSize);
        }

        @Override
        void handleTask(Task task) {
            System.out.println("here2");
            executor.submit(task.getRunnable());
            tasks.add(task.nextTask());
        }
    }

    private class FixedDelayTaskHandler extends TaskHandler {

        FixedDelayTaskHandler(int threadPoolSize) {
            super(threadPoolSize);
        }

        @Override
        void handleTask(Task task) {
            System.out.println("here3");
            CompletableFuture.runAsync(task.getRunnable(), executor)
                    .thenRun(() -> tasks.add(task.nextTask()));
        }
    }

    private class TaskHandlerFactory {
        private final Map<Class<? extends  Task>, TaskHandler> taskHandlers = new HashMap<>();

        TaskHandlerFactory() {
            taskHandlers.put(Task.class, new TaskHandler(5));
            taskHandlers.put(FixedRateTask.class, new FixedRateTaskHandler(5));
            taskHandlers.put(FixedDelayTask.class, new FixedDelayTaskHandler(5));
        }

        TaskHandler getTaskHandler(Class<? extends Task> tClass) {
            return taskHandlers.get(tClass);
        }
    }
}
