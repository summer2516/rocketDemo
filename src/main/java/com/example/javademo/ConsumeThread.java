package com.example.javademo;

public class ConsumeThread extends Thread {
    private Consumer consumer;

    public ConsumeThread(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void run() {
        while (true) {
            consumer.consmer();
        }
    }
}