package cs455.scaling.task;

import cs455.scaling.util.HashAndSelectionKeyPair;
import cs455.scaling.util.ThreadPoolManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriteTask implements Task {

    private Logger LOG = LogManager.getLogger(WriteTask.class);

    private final HashAndSelectionKeyPair pair;
    private final ThreadPoolManager threadPoolManager;

    public WriteTask(HashAndSelectionKeyPair pair, ThreadPoolManager threadPoolManager) {
        this.pair = pair;
        this.threadPoolManager = threadPoolManager;
    }

    @Override
    public void executeTask() {
        byte[] hash = pair.hash;
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
