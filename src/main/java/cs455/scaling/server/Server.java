package cs455.scaling.server;

import cs455.scaling.task.ReadTask;
import cs455.scaling.task.RegisterTask;
import cs455.scaling.util.ThreadPoolManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private static Logger LOG = LogManager.getLogger(Server.class);

    public static void main(String[] args) throws IOException {
        ThreadPoolManager threadPoolManager = new ThreadPoolManager(10);
        threadPoolManager.startThreadsInThreadPool();
        Selector selector = Selector.open();

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 7000));
        serverSocket.configureBlocking(false);

        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while(true) {
            LOG.debug("Waiting for activity...");
            int keysReady = selector.select();
            LOG.debug("Activity detected...");

            if (keysReady > 0) {

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                LOG.debug("selected keys = " + selectedKeys.toString());

                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    if (key.isValid() == false) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        SocketChannel clientSocket = serverSocket.accept();
                        LOG.debug("Constructing new RegisterTask");
                        RegisterTask registerTask = new RegisterTask(selector, clientSocket);
                        threadPoolManager.addNewTaskToWorkList(registerTask);
                    }

                    if (key.isReadable()) {
                        LOG.debug("Removing read interest from a client channel");
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                        LOG.debug("Constructing new ReadTask");
                        ReadTask readTask = new ReadTask(selector, key);
                        threadPoolManager.addNewTaskToWorkList(readTask);
                    }

                    iter.remove();
                }
            }
        }
    }
}
