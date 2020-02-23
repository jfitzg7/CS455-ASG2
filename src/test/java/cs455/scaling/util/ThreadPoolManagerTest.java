package cs455.scaling.util;

import cs455.scaling.task.BatchTask;
import cs455.scaling.task.TestTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolManagerTest {

    ThreadPoolManager threadPoolManager;

    @BeforeEach
    public void initialize() {
        threadPoolManager = new ThreadPoolManager(10);
        threadPoolManager.startThreadsInThreadPool();
    }

    @Test
    public void ThreadPoolManagerTestTaskTest() {
        for (int i=0; i < 100; i++) {
            TestTask testTask = new TestTask(i);
            threadPoolManager.addNewTaskToTaskList(testTask);
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ThreadPoolManagerBatchTaskTest() {
        ArrayList<Batch> batchList = RandomBatchGenerator.generateAListOfBatches(100, 20);

        for(Batch batch : batchList) {
            BatchTask batchTask = new BatchTask(batch);
            threadPoolManager.addNewTaskToTaskList(batchTask);
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}