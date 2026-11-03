package com.shopcore;

public class StackDemo {

    private static int depth = 0;

    public static void main(String[] args) {
        try {
            goDeeper();
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError after " + depth + " nested calls");
        }
    }

    private static void goDeeper() {
        depth++;
        goDeeper();   // calls itself forever: each call adds a frame to the stack
    }
}