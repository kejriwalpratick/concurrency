package barrier;

public class TestBarrier {

    public static void main(String[] args) throws InterruptedException {
        final int NUM_THREADS = 5;
        final Barrier barrier = new Barrier(NUM_THREADS);

        // Create and start 5 worker threads
        Thread[] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    System.out.println("Thread " + threadId + " doing some work before the barrier...");
                    Thread.sleep((long) (Math.random() * 1000));

                    System.out.println("Thread " + threadId + " waiting at barrier...");
                    barrier.await();  // Block until all threads reach this line
                    System.out.println("Thread " + threadId + " has passed the barrier.");

                } catch (InterruptedException e) {
                    System.out.println("Something went wrong: " + e.getMessage());
                }
            });
            threads[i].start();
        }

        // Wait for all threads to finish
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("All threads have passed the barrier and completed.");
    }
}
