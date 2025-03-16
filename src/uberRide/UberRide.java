package uberRide;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UberRide {
    private Integer democrats = 0;
    private Integer republicans = 0;

    private final CyclicBarrier barrier = new CyclicBarrier(4);
    private final Semaphore dSem = new Semaphore(0);
    private final Semaphore rSem = new Semaphore(0);
    private final Lock lock = new ReentrantLock();

    public UberRide() {
    }

    public void seatDemocrat() throws InterruptedException, BrokenBarrierException {
        boolean isLeader = false;

        lock.lock();
        democrats++;

        if (democrats == 4) {
            dSem.release(3);
            democrats -= 4;
            isLeader = true;
        } else if (democrats == 2 && republicans >= 2) {
            dSem.release(1);
            rSem.release(2);
            democrats -= 2;
            republicans -= 2;
            isLeader = true;
        } else {
            lock.unlock();
            dSem.acquire();
        }

        seated();
        barrier.await();

        if (isLeader) {
            drive();
            lock.unlock();
        }
    }

    private void drive() {
        System.out.println("The uber is on its way");
    }

    private void seated() {
        System.out.println(Thread.currentThread() + " has taken a seat");
    }
}
