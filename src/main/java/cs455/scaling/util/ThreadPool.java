package cs455.scaling.util;

import cs455.scaling.task.Task;

import java.util.LinkedList;

public class ThreadPool {
    private final WorkerThread[] workerThreads;

    public ThreadPool(int threadPoolSize) {
        workerThreads = new WorkerThread[threadPoolSize];
    }

    public void initializeWorkerThreads(LinkedList<Task> taskList) {
        for (int i = 0; i < workerThreads.length; i++) {
            workerThreads[i] = new WorkerThread(taskList);
            workerThreads[i].start();
        }
    }

}
