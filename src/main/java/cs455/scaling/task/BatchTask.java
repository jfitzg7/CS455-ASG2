package cs455.scaling.task;

import cs455.scaling.util.Batch;
import cs455.scaling.util.ThreadPoolManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BatchTask implements Task {

    private Logger LOG = LogManager.getLogger(BatchTask.class);

    private Batch batch;
    private ThreadPoolManager threadPoolManager;


    public BatchTask(Batch batch, ThreadPoolManager threadPoolManager) {
        this.batch = batch;
        this.threadPoolManager = threadPoolManager;
    }

    @Override
    public void executeTask() {
        threadPoolManager.restartBatchTimer();
        LOG.info("Executing a batch task");
        int numberOfTasksInBatch = batch.sizeOfTaskList();
        for (int i=0; i < numberOfTasksInBatch; i++) {
            Task nextTask = batch.removeTaskFromBatch();
            nextTask.executeTask();
        }
    }
}
