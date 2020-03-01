package cs455.scaling.util;

import cs455.scaling.client.Client;
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
    private final Client client;

    public ClientSendMessageThread(SocketChannel clientSocket, int messageRate, Client client) {
        this.clientSocket = clientSocket;
        this.messageRate = messageRate;
        this.client = client;
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
                client.addHashToPendingHashes(hashString);
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
                client.incrementTotalSentCount();
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
