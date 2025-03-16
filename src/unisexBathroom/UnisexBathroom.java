package unisexBathroom;

import java.util.concurrent.Semaphore;

public class UnisexBathroom {
    private Use use;
    private Integer occupied;
    private final Semaphore semaphore;

    private final Object lock = new Object();

    public UnisexBathroom(int capacity) {
        this.use = Use.NONE;
        this.occupied = 0;
        this.semaphore = new Semaphore(capacity);
    }

    public void useBathroomMale(String name) throws InterruptedException {

        synchronized (lock) {
            while (use.equals(Use.FEMALE))
                lock.wait();

            semaphore.acquire();
            occupied++;
            use = Use.MALE;
        }

        useBathroom(name);
        semaphore.release();

        synchronized (lock) {
            occupied--;
            if (occupied == 0)
                use = Use.NONE;
            lock.notifyAll();
        }
    }

    public void useBathroomFemale(String name) throws InterruptedException {

        synchronized (lock) {
            while (use.equals(Use.MALE))
                lock.wait();
            semaphore.acquire();
            occupied++;
            use = Use.FEMALE;
        }

        useBathroom(name);
        semaphore.release();

        synchronized (lock) {
            occupied--;
            if (occupied == 0)
                use = Use.NONE;
            lock.notifyAll();
        }
    }

    private void useBathroom(String name) throws InterruptedException {
        System.out.println(name + " has stepped into the bathroom, current occupancy: " + occupied);
        Thread.sleep(5000);
        System.out.println(name + " has stepped out of the bathroom");
    }

    enum Use {
        MALE, FEMALE, NONE
    }
}
