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
            System.out.println(cache.get(2));
            System.out.println(cache.get(3));

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignore) {

            }

            System.out.println(cache.get(1));
            System.out.println(cache.get(4));
            System.out.println(cache.get(5));
            System.out.println(cache.get(6));
        });

        Thread writerThread2 = new Thread(() -> {
            cache.put(5, 5);
            cache.put(6, 6);
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
