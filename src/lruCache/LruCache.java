package lruCache;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LruCache {
    private final Integer capacity;
    private final Map<Integer, Node> cache;
    private final Dll dll;
    private final Queue<Integer> queue;

    private final ReentrantLock queueLock = new ReentrantLock();
    private final Condition notEmpty = queueLock.newCondition();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();


    private LruCache(int capacity) {
        this.capacity = capacity;
        this.queue = new ArrayDeque<>();
        this.cache = new HashMap<>();
        this.dll = new Dll();
    }

    public static LruCache create(int capacity) {
        LruCache lruCache = new LruCache(capacity);
        Thread worker = new Thread(lruCache::markItemsUsed);
        worker.start();
        return lruCache;
    }

    public int get(int key) {
        // 1) Acquire the *read lock* to check the cache
        readLock.lock();
        try {
            Node node = cache.get(key);
            if (node == null) {
                return -1;
            }
            int value = node.value;
            // 2) Release the read lock before enqueuing (so other readers aren’t blocked)
            readLock.unlock();

            // 3) Acquire queue lock to add the key for reordering
            queueLock.lock();
            try {
                queue.add(key);
                notEmpty.signal();
            } finally {
                queueLock.unlock();
            }

            return value;
        } finally {
            // If we returned early, we still must unlock.
            // But the “typical” path unlocks *before* enqueuing.
            if (readWriteLock.getReadHoldCount() > 0) {
                readLock.unlock();
            }
        }
    }

    public void put(int key, int value) {
        // 1) Lock the queue first
        queueLock.lock();
        try {
            // 2) Then lock the cache for writing
            writeLock.lock();
            try {
                // If key exists, just update value
                Node node = cache.get(key);
                if (node != null) {
                    node.value = value;
                    // Enqueue for reordering
                    queue.add(key);
                    notEmpty.signal();
                    return;
                }

                // Evict if full
                if (cache.size() == capacity) {
                    evict();
                }
                // Insert the new node at the front
                Node newNode = new Node(key, value);
                cache.put(key, newNode);
                dll.insertAtHead(newNode);
            } finally {
                writeLock.unlock();
            }
        } finally {
            queueLock.unlock();
        }
    }

    private void markItemsUsed() {
        while (true) {
            // 1) Lock the queue
            queueLock.lock();
            try {
                // Wait until queue has something
                while (queue.isEmpty()) {
                    notEmpty.await();
                }
                // 2) Lock the cache for writing while we reorder
                writeLock.lock();
                try {
                    while (!queue.isEmpty()) {
                        int key = queue.poll();
                        // Possibly evicted before reorder
                        if (cache.containsKey(key)) {
                            markUsed(key);
                        }
                    }
                } finally {
                    writeLock.unlock();
                }
            } catch (InterruptedException e) {
                // If interrupted, decide whether to exit or continue
                // For a daemon thread, usually just continue
            } finally {
                queueLock.unlock();
            }
        }
    }


    private void evict() {
        Node tail = dll.getTail();
        cache.remove(tail.key);
        dll.delete(tail);
    }

    private void markUsed(int key) {
        Node node = cache.get(key);
        dll.delete(node);
        dll.insertAtHead(node);
    }

    static class Node {
        int key;
        int value;
        Node prev;
        Node next;

        Node(int key, int val) {
            this.key = key;
            this.value = val;
            this.prev = null;
            this.next = null;
        }
    }

    static class Dll {
        Node head;
        Node tail;

        Dll() {
            head = new Node(-1, -1);
            tail = new Node(-1, -1);
            head.next = tail;
            tail.prev = head;
        }

        public void insertAtHead(Node node) {
            head.next.prev = node;
            node.next = head.next;
            head.next = node;
            node.prev = head;
        }

        public void delete(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        public Node getTail() {
            return tail.prev;
        }
    }
}
