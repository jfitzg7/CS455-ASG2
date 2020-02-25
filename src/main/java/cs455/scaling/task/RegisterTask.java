package cs455.scaling.task;

import cs455.scaling.util.ServerSocketAttachment;
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

    public RegisterTask(Selector selector, ServerSocketChannel serverSocket, ServerSocketAttachment attachment, ReentrantLock selectorLock) {
        this.selector = selector;
        this.serverSocket = serverSocket;
        this.attachment = attachment;
        this.selectorLock = selectorLock;
    }

    @Override
    public void executeTask() {
        try {
            SocketChannel clientSocket = serverSocket.accept();
            LOG.debug("Successfully established a new client socket channel");
            clientSocket.configureBlocking(false);
            selectorLock.lock();
            try {
                selector.wakeup();
                clientSocket.register(selector, SelectionKey.OP_READ);
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
