package cs455.scaling.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.channels.SelectionKey;
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
}
