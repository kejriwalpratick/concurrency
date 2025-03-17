package countingSemaphore;

public class CountingSemaphore {
    private final Integer maxPermits;
    private Integer availablePermits;
    private final Object lock = new Object();

    public CountingSemaphore(int maxPermits) {
        this(maxPermits, 0);
    }

    public CountingSemaphore(int maxPermits, int usedPermits) {
        this.maxPermits = maxPermits;
        this.availablePermits = maxPermits - usedPermits;
    }

    public void acquire() throws InterruptedException {
        synchronized (lock) {
            while (availablePermits == 0)
                lock.wait();
            availablePermits--;
            lock.notifyAll();
        }
    }

    public void release() throws InterruptedException {
        synchronized (lock) {
            while (availablePermits.intValue() == maxPermits.intValue())
                lock.wait();
            availablePermits++;
            lock.notifyAll();
        }
    }
}
