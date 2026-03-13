package com.photon.util.os;

public class MultiThreadWorker
{
    private final ThreadCallback callback;

    /**
     * Create a multi-threaded worker that will run the given callback in parallel
     * @param cb the callback to run
     * @return the worker
     */
    public static MultiThreadWorker createWorker(ThreadCallback cb) { return new MultiThreadWorker(cb); }

    private MultiThreadWorker(ThreadCallback cb) { this.callback = cb; }

    public void run() { run(Runtime.getRuntime().availableProcessors()); }

    public void run(final int threads) {
        final ThreadWorker[] WORKERS = new ThreadWorker[threads];
        for (int i = 0; i < WORKERS.length; ++i) {
            final ThreadWorker worker = new ThreadWorker();
            worker.setDaemon(true);
            (WORKERS[i] = worker).start();
        }
        try {
            for (int i = 0; i < WORKERS.length; ++i) WORKERS[i].join();
        } catch (InterruptedException e) { e.printStackTrace(); }
    }
    
    private class ThreadWorker extends Thread {
        private static int id = 0;

        private ThreadWorker() {
            super("MultiThreadWorker-" + (++id));
            this.setPriority(Thread.MIN_PRIORITY);
        }

        @Override public void run() {
        	try {
                while (MultiThreadWorker.this.callback.work()) {}
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FunctionalInterface
    public static interface ThreadCallback {
        /**
         * 
         * @return true if there is more work to do, false otherwise
         * @throws Exception
         */
        public boolean work() throws Exception;
    }
}
