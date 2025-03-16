package boundedBuffer.coutingSemaphore;

import countingSemaphore.CountingSemaphore;

import java.util.LinkedList;
import java.util.Queue;

public class BlockingQueue<T> {
    private final Queue<T> queue;
    private final CountingSemaphore producerSemaphore;
    private final CountingSemaphore consumerSemaphore;

    private final CountingSemaphore lock = new CountingSemaphore(1);

    public BlockingQueue(int capacity) {
        this.queue = new LinkedList<>();
        this.producerSemaphore = new CountingSemaphore(capacity);
        this.consumerSemaphore = new CountingSemaphore(capacity, capacity);
    }

    public void enqueue(T item) throws InterruptedException {

        lock.acquire();
        producerSemaphore.acquire();

        queue.add(item);

        lock.release();
        consumerSemaphore.release();
    }

    public T dequeue() throws InterruptedException {
        lock.acquire();
        consumerSemaphore.acquire();

        T item = queue.poll();

        lock.release();
        producerSemaphore.release();

        return item;
    }
}
