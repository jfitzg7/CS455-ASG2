package cs455.scaling.server;


import cs455.scaling.util.ThreadPoolManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

    public static void main(String[] args) throws IOException {
        ThreadPoolManager threadPoolManager = new ThreadPoolManager(10);
        threadPoolManager.startThreadsInThreadPool();
        Selector selector = Selector.open();

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 7000));
        serverSocket.configureBlocking(false);

        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while(true) {
            selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();

            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while(iter.hasNext()) {
                SelectionKey key = iter.next();

                if (key.isValid() == false) {
                    continue;
                }

                if (key.isAcceptable()) {

                }

                if (key.isReadable()) {

                }
            }
        }
    }
}
