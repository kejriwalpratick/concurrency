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

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition taskAdded = lock.newCondition();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

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
        readWriteLock.readLock().lock();
        try {
            if (!cache.containsKey(key))
                return -1;

            lock.lock();
            queue.add(key);
            taskAdded.signalAll();
            lock.unlock();

            return cache.get(key).value;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void put(int key, int value) {
        readWriteLock.writeLock().lock();
        try {

            if (cache.containsKey(key)) {
                cache.get(key).value = value;
                markUsed(key);
                return;
            }

            if (cache.size() == capacity)
                evict();

            Node node = new Node(key, value);
            cache.put(key, node);
            dll.insertAtHead(node);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private void markItemsUsed() {
        while (true) {
            lock.lock();
            try {
                while (queue.isEmpty())
                    taskAdded.await();

                readWriteLock.writeLock().lock();
                try {
                    while (!queue.isEmpty())
                        markUsed(queue.poll());
                } finally {
                    readWriteLock.writeLock().unlock();
                }
            } catch (InterruptedException ignore) {

            } finally {
                lock.unlock();
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
