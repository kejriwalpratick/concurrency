package lruCache;

public class Main {

    public static void main(String[] args) {

        LruCache cache = LruCache.create(3);

        Thread writeThread = new Thread(() -> {
            cache.put(1, 1);
            cache.put(2, 2);
            cache.put(3, 3);
            cache.put(4, 4);
        });

        Thread readThread = new Thread(() -> {
            System.out.println("key 2: " + cache.get(2));
            System.out.println("key 3: " + cache.get(3));

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignore) {

            }

            System.out.println("key 1: " + cache.get(1));
            System.out.println("key 4: " + cache.get(4));
            System.out.println("key 5: " + cache.get(5));
            System.out.println("key 2: " + cache.get(2));
            System.out.println("key 3: " + cache.get(3));
            System.out.println("key 6: " + cache.get(6));

            System.out.println("key 2: " + cache.get(2));
            System.out.println("key 3: " + cache.get(3));
        });

        Thread writerThread2 = new Thread(() -> {
            cache.put(5, 5);
            cache.put(6, 6);
            cache.put(2, 10);
            cache.put(3, 15);
        });

        writeThread.start();
        readThread.start();
        writerThread2.start();

        try {
            writeThread.join();
            readThread.join();
            writerThread2.join();
        } catch (InterruptedException ignore) {

        }
    }
}
