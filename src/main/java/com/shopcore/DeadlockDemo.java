package com.shopcore;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class DeadlockDemo {

    private static final Object MOUSE_LOCK = new Object();
    private static final Object BOOK_LOCK = new Object();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Part 1: each customer locks in THEIR cart's order ===");
        Thread alice = new Thread(() -> lockBoth("Alice", MOUSE_LOCK, "mouse", BOOK_LOCK, "book"), "Alice");
        Thread bob = new Thread(() -> lockBoth("Bob", BOOK_LOCK, "book", MOUSE_LOCK, "mouse"), "Bob");
        alice.setDaemon(true);
        bob.setDaemon(true);
        alice.start();
        bob.start();

        Thread.sleep(1000);
        reportDeadlocks();

        System.out.println();
        System.out.println("=== Part 2: everyone locks in the SAME order (book before mouse, alphabetical) ===");
        Object fixMouse = new Object();
        Object fixBook = new Object();
        Thread carol = new Thread(() -> lockBoth("Carol", fixBook, "book", fixMouse, "mouse"), "Carol");
        Thread dave = new Thread(() -> lockBoth("Dave", fixBook, "book", fixMouse, "mouse"), "Dave");
        carol.start();
        dave.start();
        carol.join();
        dave.join();
        System.out.println("Both finished. No deadlock.");
    }

    private static void lockBoth(String who, Object first, String firstName, Object second, String secondName) {
        synchronized (first) {
            System.out.println(who + " locked the " + firstName + ", now wants the " + secondName);
            pause(100);   // give the other customer time to grab their first lock
            synchronized (second) {
                System.out.println(who + " locked both and checked out");
            }
        }
    }

    private static void reportDeadlocks() {
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        long[] stuckIds = threads.findDeadlockedThreads();
        if (stuckIds == null) {
            System.out.println("No deadlock detected.");
            return;
        }
        System.out.println("DEADLOCK! " + stuckIds.length + " threads are stuck forever:");
        for (ThreadInfo info : threads.getThreadInfo(stuckIds)) {
            System.out.println("  " + info.getThreadName() + " is " + info.getThreadState()
                    + ", waiting for a lock held by " + info.getLockOwnerName());
        }
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}