package cs455.scaling.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class RegisterTask implements Task {

    private Logger LOG = LogManager.getLogger(RegisterTask.class);

    private final Selector selector;
    private SocketChannel clientSocket;

    public RegisterTask(Selector selector, SocketChannel clientSocket) {
        this.selector = selector;
        this.clientSocket = clientSocket;
    }

    @Override
    public void executeTask() {
        try {
            clientSocket.configureBlocking(false);
            clientSocket.register(selector, SelectionKey.OP_READ);
            LOG.info("Successfully registered a new client socket channel!");
            LOG.debug("Selector keys = " + selector.keys());
            //The selector needs to wakeup other wise it will lead to deadlock with an incorrect key set
            selector.wakeup();
        } catch (IOException e) {
            LOG.error("An error occurred while handling the registration task.", e);
        }
    }
}
