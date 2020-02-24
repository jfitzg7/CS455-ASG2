package cs455.scaling.task;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class RegisterTask implements Task {

    private final Selector selector;
    private final ServerSocketChannel serverSocket;

    public RegisterTask(Selector selector, ServerSocketChannel serverSocket) {
        this.selector = selector;
        this.serverSocket = serverSocket;
    }

    @Override
    public void executeTask() {
        try {
            SocketChannel client = serverSocket.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
