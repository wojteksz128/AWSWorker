package com.psoir.projekt;

public class ApplicationListener {


    public static void main(String[] args) {
        final Thread listener = new Thread(new Runnable() {
            public void run() {
            	System.out.println("Connecting...");
                SqsListener sqsListener= new SqsListener();
                try {
                    sqsListener.listen();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        listener.start();
    }
}
