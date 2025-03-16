package scheduledExecutor;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        Runnable runnable1 = () -> System.out.println("Runnable1 : Completed at " + new Date());
        Runnable runnable2 = () -> System.out.println("Runnable2 : Completed at " + new Date());
        Runnable runnable3 = () -> {
            System.out.println("Runnable3 : Completed at " + new Date());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignore) {

            }
        };
        Runnable runnable4 = () -> System.out.println("Runnable4 : Completed at " + new Date());

        MyScheduledExecutorService scheduledExecutorService = MyScheduledExecutorService.create();

        Thread thread1 = new Thread(() -> scheduledExecutorService.schedule(runnable1, 2, TimeUnit.SECONDS));
        Thread thread2 = new Thread(() -> scheduledExecutorService.scheduleAtFixedRate(runnable2, 1, 10, TimeUnit.SECONDS));
        Thread thread3 = new Thread(() -> scheduledExecutorService.scheduleWithFixedDelay(runnable3, 1, 1, TimeUnit.SECONDS));

        Thread.sleep(10000);

        Thread thread4 = new Thread(() -> scheduledExecutorService.scheduleAtFixedRate(runnable4, 1, 8, TimeUnit.SECONDS));

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
    }
}
