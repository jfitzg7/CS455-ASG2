package cs455.scaling.util;

import cs455.scaling.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

public class WorkerThread extends Thread {

    private Logger LOG = LogManager.getLogger(WorkerThread.class);

    private final LinkedList<Task> workList;

    public WorkerThread(LinkedList<Task> taskList) {
        this.workList = taskList;
    }

    public void run() {
        Task task;
        while (true) {
            synchronized (workList) {
                while (workList.isEmpty()) {
                    try {
                        workList.wait();
                    } catch (InterruptedException e) {
                        LOG.error("The wait() call was interrupted", e);
                    }
                }
                task = workList.removeFirst();
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
