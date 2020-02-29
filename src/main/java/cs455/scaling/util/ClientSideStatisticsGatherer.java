package cs455.scaling.util;

import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientSideStatisticsGatherer {
    private final AtomicInteger totalSentCount = new AtomicInteger(0);
    private final AtomicInteger totalReceivedCount = new AtomicInteger(0);

    public void startStatisticsGathering() {
        ClientSideStatisticsGatherer statisticsGatherer = this;
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                int currentTotalSentCount;
                int currentTotalReceivedCount;
                synchronized (statisticsGatherer){
                    currentTotalSentCount = statisticsGatherer.totalSentCount.get();
                    currentTotalReceivedCount = statisticsGatherer.totalReceivedCount.get();
                    statisticsGatherer.resetTotalSentAndReceivedCounts();
                }
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                System.out.printf("%s Total sent count: %d, Total received count: %d\n", timestamp, currentTotalSentCount, currentTotalReceivedCount);
            }
        };

        timer.schedule(timerTask, 20 * 1000, 20 * 1000);
    }

    public synchronized void incrementTotalSentCount() {
        totalSentCount.incrementAndGet();
    }

    public synchronized void incrementTotalReceivedCount() {
        totalReceivedCount.incrementAndGet();
    }

    public synchronized void resetTotalSentAndReceivedCounts() {
        totalSentCount.set(0);
        totalReceivedCount.set(0);
    }
}
