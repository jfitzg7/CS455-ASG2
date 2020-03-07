package cs455.scaling.util;

import cs455.scaling.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;

public class WorkerThread extends Thread {

    private Logger LOG = LogManager.getLogger(WorkerThread.class);

    private final LinkedBlockingQueue<Task> workQueue;

    public WorkerThread(LinkedBlockingQueue<Task> workQueue) {
        this.workQueue = workQueue;
    }

    public void run() {
        while (true) {
            try {
                LOG.info("Waiting for a task");
                Task task = workQueue.take();
                LOG.info("Attempting to execute a task");
                task.executeTask();
            } catch (Exception e) {
                LOG.error("An exception occurred while executing the task", e);
                //ignore, don't let thread die if an error occurs
            }
        }
    }
}
