package cs455.scaling.util;

import cs455.scaling.task.Task;

import java.util.LinkedList;

public class ThreadPoolManager {
    private final ThreadPool threadPool;
    private final LinkedList<Task> workList;

    public ThreadPoolManager(int threadPoolSize) {
        this.workList = new LinkedList<>();
        this.threadPool = new ThreadPool(threadPoolSize);
    }

    public void startThreadsInThreadPool() {
        threadPool.initializeWorkerThreads(workList);
    }

    public void addNewTaskToWorkList(Task task) {
        synchronized (workList) {
            workList.addLast(task);
            workList.notify();
        }
    }
}
