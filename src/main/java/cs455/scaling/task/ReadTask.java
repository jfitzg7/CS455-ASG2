package cs455.scaling.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ReadTask implements Task {

    private Logger LOG = LogManager.getLogger(ReadTask.class);

    private SelectionKey key;

    public ReadTask(SelectionKey key) {
        this.key = key;
    }

    @Override
    public void executeTask() {
        ByteBuffer buffer = ByteBuffer.allocate(8000);

        SocketChannel clientChannel = (SocketChannel) key.channel();

        int bytesRead = 0;
        while (buffer.hasRemaining() && bytesRead != -1) {
            try {
                bytesRead = clientChannel.read(buffer);
            } catch (IOException e) {
                LOG.error("An error occurred while reading data from a client channel");
            }
        }
        byte[] receivedData = new byte[8000];
        buffer.get(receivedData);
        BigInteger hashInt = new BigInteger(1, receivedData);
        String hashString = hashInt.toString(16);
        LOG.debug("Data received from client channel: " + hashString);
    }
}
