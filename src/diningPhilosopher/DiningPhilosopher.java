package diningPhilosopher;

import java.util.concurrent.Semaphore;

public class DiningPhilosopher {

    private final Semaphore maxDiners = new Semaphore(4);
    private final Semaphore[] forks = new Semaphore[5];

    public DiningPhilosopher() {
        for (int i=0; i<5; i++)
            forks[i] = new Semaphore(1);
    }

    public void lifeOfPhilosopher(int id) throws InterruptedException {
        while (true) {
            contemplate();
            eat(id);
        }
    }

    private void eat(int id) throws InterruptedException {

        maxDiners.acquire();

        forks[id].acquire();
        forks[(id + 4) % 5].acquire();

        System.out.println("Philosopher " + id + " is eating now!");

        forks[id].release();
        forks[(id + 4) % 5].release();

        maxDiners.release();
    }

    private void contemplate() throws InterruptedException {
        Thread.sleep(5000);
    }
}
