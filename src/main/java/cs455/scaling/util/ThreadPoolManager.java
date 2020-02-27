package cs455.scaling.util;

import cs455.scaling.task.BatchTask;
import cs455.scaling.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class ThreadPoolManager {

    private Logger LOG = LogManager.getLogger(ThreadPoolManager.class);

    private final ThreadPool threadPool;
    private final LinkedList<Task> workList;
    private Batch batch;
    private final int batchTimeout;
    private Timer timer;
    public ServerSideStatisticsGatherer statisticsGatherer;

    public ThreadPoolManager(int threadPoolSize, int batchSize, int batchTimeout) {
        this.workList = new LinkedList<>();
        this.threadPool = new ThreadPool(threadPoolSize);
        this.batch = new Batch(batchSize);
        this.batchTimeout = batchTimeout;
        this.timer = new Timer();
        this.statisticsGatherer = new ServerSideStatisticsGatherer();
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

    public void addNewDataAndSelectionKeyPairToBatch(DataAndSelectionKeyPair pair) {
        synchronized (this.batch) {
            if (this.batch.isBatchFull()) {
                Batch deepCopiedBatch = this.batch.deepCopy();
                this.batch.clearBatch();
                BatchTask batchTask = new BatchTask(deepCopiedBatch, this);
                this.addNewTaskToWorkList(batchTask);
            }
            this.batch.addDataToBatch(pair);
        }
    }

    public void startBatchTimer() {
        TimerTask timerTask = createNewBatchTimerTask();
        this.timer.schedule(timerTask, this.batchTimeout * 1000, this.batchTimeout * 1000);
    }

    public void restartBatchTimer() {
        LOG.info("Restarting the batch timer");
        TimerTask timerTask = createNewBatchTimerTask();
        this.timer.cancel();
        this.timer = new Timer();
        this.timer.schedule(timerTask, this.batchTimeout * 1000, this.batchTimeout * 1000);
    }

    private TimerTask createNewBatchTimerTask() {
        ThreadPoolManager threadPoolManager = this;
        TimerTask timer = new TimerTask() {
            @Override
            public void run() {
                LOG.debug("Timeout has expired, adding the batch to the task queue");
                synchronized (batch) {
                    Batch deepCopiedBatch = batch.deepCopy();
                    batch.clearBatch();
                    BatchTask batchTask = new BatchTask(deepCopiedBatch, threadPoolManager);
                    addNewTaskToWorkList(batchTask);
                }
            }
        };
        return timer;
    }
}
