package cs455.scaling.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Random;

public class ClientSendMessageThread implements Runnable {

    private Logger LOG = LogManager.getLogger(ClientSendMessageThread.class);

    private SocketChannel clientSocket;
    private final int messageRate;
    private LinkedList<String> pendingHashes;
    private ClientSideStatisticsGatherer statisticsGatherer;

    public ClientSendMessageThread(SocketChannel clientSocket, int messageRate, LinkedList<String> pendingHashes, ClientSideStatisticsGatherer statisticsGatherer) {
        this.clientSocket = clientSocket;
        this.messageRate = messageRate;
        this.pendingHashes = pendingHashes;
        this.statisticsGatherer = statisticsGatherer;
    }

    @Override
    public void run() {
        // Randomly generate an 8KB message to send to the server
        if (messageRate > 0) {
            while (true) {
                Random rand = new Random();
                byte[] randomData = new byte[8000];
                rand.nextBytes(randomData);
                byte[] hash = Hashing.SHA1FromBytes(randomData);
                BigInteger hashInt = new BigInteger(1, hash);
                String hashString = hashInt.toString(16);
                synchronized (pendingHashes) {
                    pendingHashes.addLast(hashString);
                }
                ByteBuffer sendBuffer = ByteBuffer.wrap(randomData);
                LOG.info("Sending message to the server...");
                while (sendBuffer.hasRemaining()) {
                    try {
                        clientSocket.write(sendBuffer);
                    } catch (IOException e) {
                        LOG.error("An error occurred while writing to the channel", e);
                    }
                }
                LOG.info("A message has been sent to the the server.");
                LOG.info("Incrementing the total sent count");
                statisticsGatherer.incrementTotalSentCount();
                try {
                    Thread.sleep(1000 / messageRate);
                } catch (InterruptedException e) {
                    LOG.error("The message rate was interrupted", e);
                }
            }
        }
        else {
            LOG.warn("Message rate is below 1. Messaging thread shutting down...");
        }
    }
}
