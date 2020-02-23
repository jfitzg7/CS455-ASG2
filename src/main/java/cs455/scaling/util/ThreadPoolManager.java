package cs455.scaling.util;

import cs455.scaling.task.Task;

import java.util.LinkedList;

public class ThreadPoolManager {
    private final ThreadPool threadPool;
    private final LinkedList<Task> taskList;

    public ThreadPoolManager(int threadPoolSize) {
        this.taskList = new LinkedList<>();
        this.threadPool = new ThreadPool(threadPoolSize);
    }

    public void startThreadsInThreadPool() {
        threadPool.initializeWorkerThreads(taskList);
    }

    public void addNewTaskToTaskList(Task task) {
        synchronized (taskList) {
            taskList.addLast(task);
            taskList.notify();
        }
    }
}
