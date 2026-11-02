package com.shopcore;

import java.util.concurrent.atomic.AtomicInteger;

public class CounterRaceDemo {

    private static int unsafeCount = 0;
    private static final AtomicInteger safeCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        Runnable addOneHundredThousand = () -> {
            for (int i = 0; i < 100_000; i++) {
                unsafeCount++;                  // read, add 1, write back: 3 steps
                safeCount.incrementAndGet();    // one indivisible step
            }
        };

        Thread first = new Thread(addOneHundredThousand);
        Thread second = new Thread(addOneHundredThousand);

        first.start();
        second.start();

        first.join();
        second.join();

        System.out.println("Expected 200000");
        System.out.println("  plain int:     " + unsafeCount);
        System.out.println("  AtomicInteger: " + safeCount.get());
    }
}