package com.example.javademo;

public class ProduceThread extends Thread {
    private Producer producer;
    public ProduceThread(Producer producer){
        this.producer = producer;
    }
    @Override
    public void run() {
        while (true) {
            producer.producer();
        }
    }
}