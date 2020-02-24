package cs455.scaling.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class RegisterTask implements Task {

    private Logger LOG = LogManager.getLogger(RegisterTask.class);

    private final Selector selector;
    private final ServerSocketChannel serverSocket;

    public RegisterTask(Selector selector, ServerSocketChannel serverSocket) {
        this.selector = selector;
        this.serverSocket = serverSocket;
    }

    @Override
    public void executeTask() {
        try {
            LOG.info("Registering a new client socket channel");
            SocketChannel client = serverSocket.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            LOG.info("Successfully registered a new client socket channel!");
        } catch (IOException e) {
            LOG.error("An error occurred while handling the registration task.", e);
        }
    }
}
