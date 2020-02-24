package cs455.scaling.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class Client {
    private static SocketChannel client;

    private LinkedList<BigInteger> computedHashes = new LinkedList<>();

    public static void main(String[] args) {
        try {
            Selector selector = Selector.open();
            client = SocketChannel.open(new InetSocketAddress("localhost", 5001));
            client.configureBlocking(false);
            SelectionKey key = client.register(selector, SelectionKey.OP_READ);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
