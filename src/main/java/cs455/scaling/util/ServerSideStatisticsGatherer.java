package cs455.scaling.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.channels.SelectionKey;
import java.sql.Timestamp;
import java.util.*;

public class ServerSideStatisticsGatherer {

    private Logger LOG = LogManager.getLogger(ServerSideStatisticsGatherer.class);

    private Map<SelectionKey, Integer> individualClientThroughPuts;

    public ServerSideStatisticsGatherer() {
        this.individualClientThroughPuts = new HashMap<>();
    }

    public synchronized void addClient(SelectionKey key) {
        individualClientThroughPuts.put(key, 0);
    }

    public synchronized void removeClient(SelectionKey key) {
        individualClientThroughPuts.remove(key);
    }

    public synchronized void incrementClientThroughPut(SelectionKey key) {
        int currentThroughPut = individualClientThroughPuts.get(key);
        individualClientThroughPuts.put(key, currentThroughPut + 1);
    }

    public void startStatisticsGathering() {
        ServerSideStatisticsGatherer statisticsGatherer = this;
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                Map<SelectionKey, Integer> currentThroughPutList;
                LOG.info("Gathering current server statistics");
                synchronized (statisticsGatherer) {
                    currentThroughPutList = statisticsGatherer.deepCopiedThroughPutList();
                    statisticsGatherer.resetThroughPuts();
                }
                LOG.debug("Through put list from last 20 seconds: " + Arrays.asList(currentThroughPutList));
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                double totalMessagesPerSecond = calculateTotalThroughput(currentThroughPutList) / 20.0;
                int totalActiveClients = calculateTotalActiveClients(currentThroughPutList);
                double throughputMeanPerSecond = calculateMean(currentThroughPutList) / 20.0;
                double throughputStandardDeviation = calculatePerSecondStandardDeviation(currentThroughPutList);
                System.out.printf("%s Server Throughput: %f/s, Active Client Connections: %d, Mean Per-client Throughput: %f/s, Std. Dev. Of Per-client Throughput: %f/s\n",
                        timestamp, totalMessagesPerSecond, totalActiveClients, throughputMeanPerSecond, throughputStandardDeviation);
            }
        };

        timer.schedule(timerTask, 20 * 1000, 20 * 1000);
    }

    public synchronized Map<SelectionKey, Integer> deepCopiedThroughPutList() {
        Map<SelectionKey, Integer> copiedThroughPutList = new HashMap<>();
        for (Map.Entry<SelectionKey, Integer> entry : individualClientThroughPuts.entrySet()) {
            SelectionKey key = entry.getKey();
            int throughPut = entry.getValue();
            copiedThroughPutList.put(key, throughPut);
        }
        return copiedThroughPutList;
    }

    public synchronized void resetThroughPuts() {
        for (Map.Entry<SelectionKey, Integer> entry : individualClientThroughPuts.entrySet()) {
            SelectionKey key = entry.getKey();
            individualClientThroughPuts.put(key, 0);
        }
    }

    private int calculateTotalThroughput(Map<SelectionKey, Integer> throughputList) {
        int totalThroughput = 0;
        for (Map.Entry<SelectionKey, Integer> entry : throughputList.entrySet()) {
            totalThroughput += entry.getValue();
        }
        return totalThroughput;
    }

    private int calculateTotalActiveClients(Map<SelectionKey, Integer> throughputList) {
        return throughputList.size();
    }

    private double calculateMean(Map<SelectionKey, Integer> throughputList) {
        double totalThroughput = calculateTotalThroughput(throughputList);
        double totalActiveClients = calculateTotalActiveClients(throughputList);
        return totalThroughput / totalActiveClients;
    }

    private double calculatePerSecondStandardDeviation(Map<SelectionKey, Integer> throughputList) {
        double throughputMean = calculateMean(throughputList);
        int totalActiveClients = calculateTotalActiveClients(throughputList);
        double variance = 0;
        for (Map.Entry<SelectionKey, Integer> entry : throughputList.entrySet()) {
            double squaredDifference = Math.pow(entry.getValue() - throughputMean, 2.0) / 20.0;
            variance += (squaredDifference / totalActiveClients);
        }
        double standardDeviation = Math.sqrt(variance);
        return standardDeviation;
    }
}
