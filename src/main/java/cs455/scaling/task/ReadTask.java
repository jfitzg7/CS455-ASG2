package cs455.scaling.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ReadTask implements Task {

    private Logger LOG = LogManager.getLogger(ReadTask.class);

    private SelectionKey key;
    private Selector selector;

    public ReadTask(Selector selector, SelectionKey key) {
        this.key = key;
        this.selector = selector;
    }

    @Override
    public void executeTask() {
        ByteBuffer buffer = ByteBuffer.allocate(8000);

        SocketChannel clientChannel = (SocketChannel) key.channel();

        int bytesRead = 0;
        try {
            while (buffer.hasRemaining() && bytesRead != -1) {
                bytesRead = clientChannel.read(buffer);
                LOG.debug("bytes read from the client channel: " + bytesRead);
            }
        } catch (IOException e) {
            LOG.error("An error occurred while reading data from a client channel", e);
        }
        if (bytesRead == -1) {
            try {
                clientChannel.close();
            } catch (IOException e) {
                LOG.error("An error occurred while trying to close the client channel", e);
            }
        }
        else {
            byte[] receivedData = new byte[8000];
            buffer.rewind();
            LOG.debug("Buffer = " + buffer);
            buffer.get(receivedData);
            BigInteger hashInt = new BigInteger(1, receivedData);
            String hashString = hashInt.toString(16);
            LOG.debug("Data received from client channel: " + hashString);
            LOG.info("Making client channel readable again...");
            LOG.debug("Interest set before: " + key.interestOps());
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            LOG.debug("Interest set after: " + key.interestOps());
            //wakeup the selector other wise it will lead to dead lock
            //the interest set of this channel's key will not be updated if the selector is currently blocking
            selector.wakeup();
        }
    }
}
