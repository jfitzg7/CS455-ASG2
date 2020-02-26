package cs455.scaling.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Random;

public class ClientSendMessageThread implements Runnable {

    private Logger LOG = LogManager.getLogger(ClientSendMessageThread.class);

    private SocketChannel clientSocket;
    private final int messageRate;
    private LinkedList<byte[]> pendingHashes;

    public ClientSendMessageThread(SocketChannel clientSocket, int messageRate, LinkedList<byte[]> pendingHashes) {
        this.clientSocket = clientSocket;
        this.messageRate = messageRate;
        this.pendingHashes = pendingHashes;
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
                synchronized (pendingHashes) {
                    pendingHashes.addLast(hash);
                }
                ByteBuffer sendBuffer = ByteBuffer.wrap(randomData);
                LOG.debug("Sending message to the server...");
                while (sendBuffer.hasRemaining()) {
                    try {
                        clientSocket.write(sendBuffer);
                    } catch (IOException e) {
                        LOG.error("An error occurred while writing to the channel");
                    }
                }
                LOG.debug("A message has been sent to the the server.");
                try {
                    Thread.sleep(1000 / messageRate);
                } catch (InterruptedException e) {
                    LOG.error("The message rate was interrupted");
                }
            }
        }
        else {
            LOG.warn("Message rate is below 1. Messaging thread shutting down...");
        }
    }
}
