package cs455.scaling.util;

import cs455.scaling.task.BatchTask;
import cs455.scaling.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolManager {

    private Logger LOG = LogManager.getLogger(ThreadPoolManager.class);

    private final ThreadPool threadPool;
    private final LinkedBlockingQueue<Task> workQueue;
    private Batch batch;
    private final double batchTimeout;
    private Timer timer;
    public ServerSideStatisticsGatherer statisticsGatherer;

    public ThreadPoolManager(int threadPoolSize, int batchSize, double batchTimeout) {
        this.workQueue = new LinkedBlockingQueue<>();
        this.threadPool = new ThreadPool(threadPoolSize);
        this.batch = new Batch(batchSize);
        this.batchTimeout = batchTimeout;
        this.timer = new Timer();
        this.statisticsGatherer = new ServerSideStatisticsGatherer();
    }

    public void startThreadsInThreadPool() {
        threadPool.initializeWorkerThreads(this.workQueue);
    }

    public void addNewTaskToWorkQueue(Task task) {
        try {
            workQueue.put(task);
        } catch (InterruptedException e) {
            LOG.error("An error occurred while adding a task to the work queue", e);
        }
    }

    public void addNewTaskToBatch(Task task) {
        synchronized (this.batch) {
            this.batch.addTaskToBatch(task);
            if (this.batch.isBatchFull()) {
                Batch deepCopiedBatch = this.batch.deepCopy();
                this.batch.clearBatch();
                BatchTask batchTask = new BatchTask(deepCopiedBatch, this);
                this.addNewTaskToWorkQueue(batchTask);
            }
        }
    }

    public void startBatchTimer() {
        TimerTask timerTask = createNewBatchTimerTask();
        long batchTimeoutInMilliseconds = Math.round(this.batchTimeout * 1000);
        this.timer.schedule(timerTask, batchTimeoutInMilliseconds, batchTimeoutInMilliseconds);
    }

    public void restartBatchTimer() {
        LOG.info("Restarting the batch timer");
        TimerTask timerTask = createNewBatchTimerTask();
        this.timer.cancel();
        this.timer = new Timer();
        long batchTimeoutInMilliseconds = Math.round(this.batchTimeout * 1000);
        this.timer.schedule(timerTask, batchTimeoutInMilliseconds, batchTimeoutInMilliseconds);
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
                    threadPoolManager.addNewTaskToWorkQueue(batchTask);
                }
            }
        };
        return timer;
    }
}
