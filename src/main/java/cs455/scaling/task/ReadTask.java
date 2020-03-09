package cs455.scaling.task;

import cs455.scaling.util.HashAndSelectionKeyPair;
import cs455.scaling.util.Hashing;
import cs455.scaling.util.ThreadPoolManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ReadTask implements Task {

    private Logger LOG = LogManager.getLogger(ReadTask.class);

    private SelectionKey key;
    private Selector selector;
    private ThreadPoolManager threadPoolManager;

    public ReadTask(Selector selector, SelectionKey key, ThreadPoolManager threadPoolManager) {
        this.key = key;
        this.selector = selector;
        this.threadPoolManager = threadPoolManager;
    }

    @Override
    public void executeTask() {
        ByteBuffer buffer = ByteBuffer.allocate(8000);
        SocketChannel clientChannel = (SocketChannel) key.channel();

        int bytesRead = 0;
        try {
            while(buffer.hasRemaining() && bytesRead != -1) {
                bytesRead = clientChannel.read(buffer);
                LOG.debug("bytes read from the client channel: " + bytesRead);
            }
        } catch (IOException e) {
            LOG.error("An error occurred while reading data from a client channel", e);
        } finally {
            //allow the channel to be readable again
            key.interestOps(key.interestOps() | (SelectionKey.OP_READ));
        }

        if (bytesRead == -1) {
            try {
                clientChannel.close();
                threadPoolManager.statisticsGatherer.removeClient(key);
            } catch (IOException e) {
                LOG.error("An error occurred while trying to close the client channel", e);
            }
        }

        if (bytesRead != -1 && !buffer.hasRemaining()) {
            byte[] receivedData = new byte[8000];
            ((Buffer) buffer).rewind();
            LOG.debug("Buffer = " + buffer);
            buffer.get(receivedData);
            /* wakeup the selector other wise it will lead to dead lock because
               the interest set of this channel's key will not be updated if
               the selector is currently blocking */
            selector.wakeup();
            byte[] hash = Hashing.SHA1FromBytes(receivedData);
            HashAndSelectionKeyPair pair = new HashAndSelectionKeyPair(hash, key);
            threadPoolManager.addNewTaskToBatch(new WriteTask(pair, threadPoolManager));
        }
    }
}
