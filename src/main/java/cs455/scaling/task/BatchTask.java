package cs455.scaling.task;

import cs455.scaling.util.Batch;
import cs455.scaling.util.DataAndSelectionKeyPair;
import cs455.scaling.util.Hashing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class BatchTask implements Task {

    private Logger LOG = LogManager.getLogger(BatchTask.class);

    private final Batch batch;

    public BatchTask(Batch batch) {
        this.batch = batch;
    }

    @Override
    public void executeTask() {
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

        }
    }
}
