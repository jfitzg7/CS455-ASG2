package cs455.scaling.task;

import cs455.scaling.util.ServerSocketAttachment;
import cs455.scaling.util.ThreadPoolManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

public class RegisterTask implements Task {

    private Logger LOG = LogManager.getLogger(RegisterTask.class);

    private final Selector selector;
    private ServerSocketChannel serverSocket;
    private ServerSocketAttachment attachment;
    private final ReentrantLock selectorLock;
    private ThreadPoolManager threadPoolManager;

    public RegisterTask(Selector selector, ServerSocketChannel serverSocket, ServerSocketAttachment attachment, ReentrantLock selectorLock, ThreadPoolManager threadPoolManager) {
        this.selector = selector;
        this.serverSocket = serverSocket;
        this.attachment = attachment;
        this.selectorLock = selectorLock;
        this.threadPoolManager = threadPoolManager;
    }

    @Override
    public void executeTask() {
        try {
            SocketChannel clientSocket = serverSocket.accept();
            LOG.info("Successfully established a new client socket channel");
            clientSocket.configureBlocking(false);
            selectorLock.lock();
            try {
                selector.wakeup();
                SelectionKey newClientKey = clientSocket.register(selector, SelectionKey.OP_READ);
                LOG.info("Adding new client SelectionKey to the statistics gatherer");
                threadPoolManager.statisticsGatherer.addClient(newClientKey);
            } finally {
                selectorLock.unlock();
            }
            LOG.info("Successfully registered a new client socket channel!");
            LOG.debug("Selector keys = " + selector.keys());
            attachment.isQueuedForAccept = false;
        } catch (IOException e) {
            LOG.error("An error occurred while handling the registration task.", e);
        }
    }
}
