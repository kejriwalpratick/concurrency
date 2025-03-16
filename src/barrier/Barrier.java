package barrier;

public class Barrier {
    private final Integer maxThreads;
    private Integer count;
    private Integer released;

    public Barrier(int maxThreads) {
        this.maxThreads = maxThreads;
        this.count = 0;
        this.released = 0;
    }

    public synchronized void await() throws InterruptedException {
        if (count.intValue() == maxThreads.intValue())
            wait();

        count++;

        if (count.intValue() == maxThreads.intValue()) {
            notifyAll();
            released = count;
        } else {
            while (count < maxThreads)
                wait();
        }

        released--;
        if (released == 0) {
            count = 0;
            notifyAll();
        }
    }
}
