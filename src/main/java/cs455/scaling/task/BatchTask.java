package cs455.scaling.task;

import cs455.scaling.util.Batch;
import cs455.scaling.util.DataAndSelectionKeyPair;
import cs455.scaling.util.Hashing;
import cs455.scaling.util.ThreadPoolManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
        int numberOfItemsInBatch = batch.sizeOfDataList();
        for (int i=0; i < numberOfItemsInBatch; i++) {
            DataAndSelectionKeyPair pair = batch.removeDataFromBatch();
            byte[] hash = Hashing.SHA1FromBytes(pair.data);
            SocketChannel clientSocket = (SocketChannel) pair.key.channel();
            ByteBuffer sendBuffer = ByteBuffer.wrap(hash);
            try {
                while(sendBuffer.hasRemaining()) {
                    clientSocket.write(sendBuffer);
                }
            } catch (IOException e) {
                LOG.error("An error occurred while writing to the channel", e);
            }
            LOG.info("The hashed data has been sent back to the client");
            threadPoolManager.statisticsGatherer.incrementClientThroughPut(pair.key);
            LOG.info("The client's through put has been incremented");
        }
    }
}
