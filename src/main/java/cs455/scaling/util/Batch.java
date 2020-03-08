package cs455.scaling.util;

import cs455.scaling.task.Task;

import java.util.LinkedList;

public class Batch {
    private final LinkedList<Task> taskList;
    private final int batchSize;

    public Batch(int batchSize) {
        this.taskList = new LinkedList<>();
        this.batchSize = batchSize;
    }

    public boolean addTaskToBatch(Task task) {
        if (taskList.size() < batchSize) {
            taskList.addLast(task);
            return true;
        }
        else {
            return false;
        }
    }

    public int sizeOfTaskList() {
        return taskList.size();
    }

    public Task removeTaskFromBatch() {
        return taskList.removeFirst();
    }

    public boolean isBatchFull() {
        if (taskList.size() >= batchSize) {
            return true;
        }
        else {
            return false;
        }
    }

    public void clearBatch() {
        taskList.clear();
    }

    public Batch deepCopy() {
        Batch deepCopiedBatch = new Batch(batchSize);
        //The Tasks only need to be shallow copies
        for (Task task : taskList) {
            deepCopiedBatch.addTaskToBatch(task);
        }
        return deepCopiedBatch;
    }
}
