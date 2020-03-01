package cs455.scaling.client;

import cs455.scaling.util.ClientSendMessageThread;
import cs455.scaling.util.ClientSideStatisticsGatherer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Client {

    private Logger LOG = LogManager.getLogger(Client.class);
    private SocketChannel clientSocket;
    private LinkedList<String> pendingHashes;
    private ClientSideStatisticsGatherer statisticsGatherer;

    public Client() {
        pendingHashes = new LinkedList<>();
        statisticsGatherer = new ClientSideStatisticsGatherer();
    }

    public static void main(String[] args) {
        if(args.length == 3) {
            try {
                String hostName = args[0];
                int portNumber = Integer.parseInt(args[1]);
                Client client = new Client();
                client.establishSocketChannelWithServer(hostName, portNumber);
                client.configureClientToBeNonBlocking();
                int messageRate = Integer.parseInt(args[2]);
                client.statisticsGatherer.startStatisticsGathering();
                client.startSendingMessagesToServer(messageRate);
                client.listenForResponsesFromServer();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Incorrect number of arguments provided.");
        }
    }

    public void establishSocketChannelWithServer(String hostname, int portNumber) throws IOException {
        LOG.info("Opening new socket channel with the server");
        clientSocket = SocketChannel.open(new InetSocketAddress(hostname, portNumber));
        LOG.info("Successfully opened new socket channel with the server");
    }

    public void configureClientToBeNonBlocking() throws IOException {
        clientSocket.configureBlocking(false);
    }

    public void listenForResponsesFromServer() throws IOException {
        Selector selector = Selector.open();
        clientSocket.register(selector, SelectionKey.OP_READ);

        while(true) {
            selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();

            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while(iter.hasNext()) {
                SelectionKey key = iter.next();

                if (key.isValid() == false) {
                    continue;
                }

                if (key.isReadable()) {
                    handleServerResponse(key);
                }

                iter.remove();
            }
        }
    }

    public void startSendingMessagesToServer(int messageRate) {
        (new Thread(new ClientSendMessageThread(clientSocket, messageRate, pendingHashes, statisticsGatherer))).start();
    }

    private void handleServerResponse(SelectionKey key) {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        int bytesRead = 0;
        while(buffer.hasRemaining() && bytesRead != -1) {
            try {
                bytesRead = clientSocket.read(buffer);
            } catch (IOException e) {
                LOG.error("An error occurred while reading a response from the server", e);
            }
        }
        LOG.info("Incrementing the total received count");
        statisticsGatherer.incrementTotalReceivedCount();
        ((Buffer) buffer).rewind();
        byte[] receivedData = new byte[20];
        buffer.get(receivedData);
        //convert byte array into a string representation of the hash
        BigInteger hashInt = new BigInteger(1, receivedData);
        String hashString = hashInt.toString(16);
        LOG.info("Received a response from the server");
        LOG.debug("Server response: " + hashString);
        removeHashFromPendingHashes(hashString);
    }

    private boolean removeHashFromPendingHashes(String hash) {
        synchronized (pendingHashes) {
            if (pendingHashes.contains(hash)) {
                pendingHashes.removeFirstOccurrence(hash);
                LOG.info("Removed a SHA1 hash from the pending hash list");
                return true;
            }
            else {
                LOG.warn("Received a SHA1 hash acknowledgement from the server that is not in the pending hash list");
                return false;
            }
        }
    }

    public void addHashToPendingHashes(String hash) {
        synchronized (pendingHashes) {
            pendingHashes.addLast(hash);
        }
    }

}
