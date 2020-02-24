package cs455.scaling.util;

import cs455.scaling.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

public class WorkerThread extends Thread {

    private Logger LOG = LogManager.getLogger(WorkerThread.class);

    private final LinkedList<Task> taskList;

    public WorkerThread(LinkedList<Task> taskList) {
        this.taskList = taskList;
    }

    public void run() {
        Task task;
        while (true) {
            synchronized (taskList) {
                while (taskList.isEmpty()) {
                    try {
                        taskList.wait();
                    } catch (InterruptedException e) {
                        LOG.error("The wait() call was interrupted", e);
                    }
                }
                task = taskList.removeFirst();
            }
            try {
                LOG.info("Attempting to execute a task...");
                task.executeTask();
            } catch (Exception e) {
                LOG.error("An exception occurred while executing the task", e);
                //ignore, don't let thread die if an error occurs
            }
        }
    }
}
