package com.photon.util.os;

import com.photon.util.ConsoleManager;

public abstract class MultiThreadWorker
{
    public void run() {
        final ThreadWorker[] workers = new ThreadWorker[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < workers.length; ++i) {
            final ThreadWorker worker = new ThreadWorker();
            worker.setDaemon(true);
            (workers[i] = worker).start();
        }
        try {
            for (int i = 0; i < workers.length; ++i) workers[i].join();
        } catch (InterruptedException e) { ConsoleManager.create(e).error().end(); }
    }
    
    protected abstract boolean work();
    
    private class ThreadWorker extends Thread {
        @Override public void run() {
        	while (MultiThreadWorker.this.work()) {}
        }
    }
}
