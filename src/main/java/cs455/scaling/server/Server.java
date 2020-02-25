package cs455.scaling.server;

import cs455.scaling.task.ReadTask;
import cs455.scaling.task.RegisterTask;
import cs455.scaling.util.Batch;
import cs455.scaling.util.ServerSocketAttachment;
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
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    private static Logger LOG = LogManager.getLogger(Server.class);

    private static Batch batch;

    public static void main(String[] args) throws IOException {
        batch = new Batch(10);
        ThreadPoolManager threadPoolManager = new ThreadPoolManager(10);
        threadPoolManager.startThreadsInThreadPool();
        Selector selector = Selector.open();

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 7000));
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

            LOG.debug("Waiting for activity...");
            selector.select();
            LOG.debug("Activity detected...");

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            LOG.debug("selected keys = " + selectedKeys.toString());

            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while(iter.hasNext()) {
                SelectionKey key = iter.next();

                if (key.isValid() == false) {
                    continue;
                }

                if (key.isAcceptable()) {
                    ServerSocketAttachment attachment = (ServerSocketAttachment) key.attachment();
                    if (!attachment.isQueuedForAccept) {
                        LOG.debug("Constructing new RegisterTask");
                        RegisterTask registerTask = new RegisterTask(selector, serverSocket, attachment, selectorLock);
                        attachment.isQueuedForAccept = true;
                        threadPoolManager.addNewTaskToWorkList(registerTask);
                    }
                    else {
                        LOG.warn("The server socket is already trying to accept a connection!");
                    }
                }

                if (key.isReadable()) {
                    LOG.debug("Removing read interest from a client channel");
                    key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                    LOG.debug("Constructing new ReadTask");
                    ReadTask readTask = new ReadTask(selector, key, batch, threadPoolManager);
                    threadPoolManager.addNewTaskToWorkList(readTask);
                }

                iter.remove();
            }
        }
    }
}
