package com.ovchinnikov.reactor;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Reactor {
    private static final Processable POISON = () -> {};

    private final ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task);
        thread.setName("Reactor");
        return thread;
    });
    private final BlockingQueue<Processable> in;
    private final Runnable emergencyAction;

    public Reactor(final Runnable emergencyAction, final int reactorCapacity) {
        this.in = new ArrayBlockingQueue<>(reactorCapacity);
        this.emergencyAction = emergencyAction;
    }

    public void start() {
        executor.submit(this::runReactorLoop);
    }

    public void stop() {
        try {
            in.put(POISON);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeInterruptedException(e);
        }
    }

    public void submit(final Processable processable) {
        try {
            in.put(processable);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeInterruptedException(e);
        }
    }

    private void runReactorLoop() {
        try {
            while (true) {
                Processable processable = in.take();
                if (processable == POISON) {
                    executor.shutdown();
                    return;
                }
                processable.process();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            emergencyAction.run();
        }
    }


}
