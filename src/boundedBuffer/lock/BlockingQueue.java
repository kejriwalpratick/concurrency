package boundedBuffer.lock;

import java.util.LinkedList;
import java.util.Queue;

public class BlockingQueue<T> {
    private final Queue<T> queue;
    private final Integer capacity;
    private final Object lock = new Object();

    public BlockingQueue(int capacity) {
        this.queue = new LinkedList<>();
        this.capacity = capacity;
    }

    public void enqueue(T item) throws InterruptedException {
        synchronized (lock) {
            while (queue.size() == capacity)
                lock.wait();
            queue.add(item);
            lock.notifyAll();
        }
    }

    public T dequeue() throws InterruptedException {
        synchronized (lock) {
            while (queue.size() == 0)
                lock.wait();
            T item = queue.poll();
            lock.notifyAll();
            return item;
        }
    }
}
