package com.shopcore;

import java.util.concurrent.atomic.AtomicInteger;

public class VisibilityDemo {

    // Part 1: a plain field. Other threads are NOT guaranteed to ever see changes.
    private static boolean plainStoreOpen = true;

    // Part 2: a volatile field. Every read sees the latest write.
    private static volatile boolean volatileStoreOpen = true;

    // Part 3: volatile does NOT make count++ safe.
    private static volatile int volatileCount = 0;
    private static final AtomicInteger atomicCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Part 1: plain boolean ===");
        Thread plainWorker = new Thread(() -> {
            long processed = 0;
            while (plainStoreOpen) {
                processed++;          // keep "processing orders" while the store is open
            }
            System.out.println("Plain worker saw the store close after " + processed + " orders");
        });
        runAndClose(plainWorker, () -> plainStoreOpen = false);

        System.out.println();
        System.out.println("=== Part 2: volatile boolean ===");
        Thread volatileWorker = new Thread(() -> {
            long processed = 0;
            while (volatileStoreOpen) {
                processed++;
            }
            System.out.println("Volatile worker saw the store close after " + processed + " orders");
        });
        runAndClose(volatileWorker, () -> volatileStoreOpen = false);

        System.out.println();
        System.out.println("=== Part 3: volatile is not atomic ===");
        Runnable addVolatile = () -> {
            for (int i = 0; i < 1_000_000; i++) {
                volatileCount++;                 // still read, add, write: 3 steps
            }
        };
        Runnable addAtomic = () -> {
            for (int i = 0; i < 1_000_000; i++) {
                atomicCount.incrementAndGet();   // one indivisible step
            }
        };
        runTwice(addVolatile);
        runTwice(addAtomic);
        System.out.println("Expected 2000000. volatile int: " + volatileCount
                + ", AtomicInteger: " + atomicCount.get());
    }

    private static void runTwice(Runnable job) throws InterruptedException {
        Thread a = new Thread(job);
        Thread b = new Thread(job);
        a.start();
        b.start();
        a.join();
        b.join();
    }

    private static void runAndClose(Thread worker, Runnable closeStore) throws InterruptedException {
        worker.setDaemon(true);     // if it never stops, it won't keep the program alive
        worker.start();
        Thread.sleep(1000);         // let it run long enough for the JIT to optimize the loop
        closeStore.run();
        System.out.println("Main thread closed the store");
        worker.join(2000);          // wait up to 2 seconds for the worker to notice
        if (worker.isAlive()) {
            System.out.println("Worker is STILL running 2 seconds later. It never saw the change.");
        }
    }
}