package cs455.scaling.util;

import cs455.scaling.task.Task;

import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {
    private final WorkerThread[] workerThreads;

    public ThreadPool(int threadPoolSize) {
        workerThreads = new WorkerThread[threadPoolSize];
    }

    public void initializeWorkerThreads(LinkedBlockingQueue<Task> workQueue) {
        for (int i = 0; i < workerThreads.length; i++) {
            workerThreads[i] = new WorkerThread(workQueue);
            workerThreads[i].start();
        }
    }
}
