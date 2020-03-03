package cs455.scaling.server;

import cs455.scaling.task.ReadTask;
import cs455.scaling.task.RegisterTask;
import cs455.scaling.util.ServerSocketAttachment;
import cs455.scaling.util.ThreadPoolManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    private static Logger LOG = LogManager.getLogger(Server.class);

    public static void main(String[] args) throws IOException {
        //parse command-line arguments
        int portNumber = Integer.parseInt(args[0]);
        int threadPoolSize = Integer.parseInt(args[1]);
        int batchSize = Integer.parseInt(args[2]);
        int batchTime = Integer.parseInt(args[3]);

        //start thread pool manager
        ThreadPoolManager threadPoolManager = new ThreadPoolManager(threadPoolSize, batchSize, batchTime);
        LOG.info("Starting the thread pool");
        threadPoolManager.startThreadsInThreadPool();
        LOG.info("Starting the batch timer");
        threadPoolManager.startBatchTimer();
        LOG.info("Starting the statistics gatherer");
        threadPoolManager.statisticsGatherer.startStatisticsGathering();

        Selector selector = Selector.open();

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(portNumber));
        serverSocket.configureBlocking(false);

        SelectionKey serverKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        //Attach an object to help with managing queuing
        serverKey.attach(new ServerSocketAttachment());

        //create reentrant lock for blocking when registering new client channels
        final ReentrantLock selectorLock = new ReentrantLock();

        while(true) {
            LOG.debug("Waiting to acquire lock...");
            selectorLock.lock();
            selectorLock.unlock();
            LOG.debug("Lock acquired and subsequently released...");

            LOG.info("Waiting for activity...");
            int keysReady = selector.select();
            LOG.info("Activity detected...");

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
                        ServerSocketAttachment attachment = (ServerSocketAttachment) key.attachment();
                        if (!attachment.isQueuedForAccept) {
                            LOG.info("Constructing new RegisterTask");
                            RegisterTask registerTask = new RegisterTask(selector, serverSocket, attachment, selectorLock, threadPoolManager);
                            attachment.isQueuedForAccept = true;
                            threadPoolManager.addNewTaskToWorkList(registerTask);
                        } else {
                            LOG.warn("The server socket is already trying to accept a connection!");
                        }
                    }

                    if (key.isReadable()) {
                        //remove read interest from the client channels interest set
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                        LOG.info("Constructing new ReadTask");
                        ReadTask readTask = new ReadTask(selector, key, threadPoolManager);
                        threadPoolManager.addNewTaskToWorkList(readTask);
                    }

                    iter.remove();
                }
            }
        }
    }
}
