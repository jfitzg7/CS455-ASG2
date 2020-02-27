package cs455.scaling.server;

import cs455.scaling.task.BatchTask;
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
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    private static Logger LOG = LogManager.getLogger(Server.class);

    private Batch batch;
    private ThreadPoolManager threadPoolManager;
    private Timer timer;
    private int batchTimeout;

    public Server(ThreadPoolManager threadPoolManager, Batch batch, int batchTimeout) {
        this.threadPoolManager = threadPoolManager;
        this.batch = batch;
        this.batchTimeout = batchTimeout;
        this.timer = new Timer();
    }

    public static void main(String[] args) throws IOException {
        Batch batch = new Batch(10);
        ThreadPoolManager threadPoolManager = new ThreadPoolManager(10);
        threadPoolManager.startThreadsInThreadPool();
        Server server = new Server(threadPoolManager, batch, 5);

        server.handleClientCommunication();
    }

    public void handleClientCommunication() throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 7000));
        serverSocket.configureBlocking(false);

        SelectionKey serverKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        //Attach an object to help with managing queuing
        serverKey.attach(new ServerSocketAttachment());

        //create reentrant lock for blocking when registering new client channels
        final ReentrantLock selectorLock = new ReentrantLock();

        startBatchTimer();

        while(true) {
            LOG.debug("Waiting to acquire lock...");
            selectorLock.lock();
            selectorLock.unlock();
            LOG.debug("Lock acquired and subsequently released...");

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
                        ServerSocketAttachment attachment = (ServerSocketAttachment) key.attachment();
                        if (!attachment.isQueuedForAccept) {
                            LOG.debug("Constructing new RegisterTask");
                            RegisterTask registerTask = new RegisterTask(selector, serverSocket, attachment, selectorLock);
                            attachment.isQueuedForAccept = true;
                            threadPoolManager.addNewTaskToWorkList(registerTask);
                        } else {
                            LOG.warn("The server socket is already trying to accept a connection!");
                        }
                    }

                    if (key.isReadable()) {
                        LOG.debug("Removing read interest from a client channel");
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                        LOG.debug("Constructing new ReadTask");
                        ReadTask readTask = new ReadTask(selector, key, this.batch, threadPoolManager, this);
                        threadPoolManager.addNewTaskToWorkList(readTask);
                    }

                    iter.remove();
                }
            }
        }
    }

    public void startBatchTimer() {
        TimerTask timerTask = createNewBatchTimerTask();
        this.timer.schedule(timerTask, this.batchTimeout * 1000, this.batchTimeout * 1000);
    }

    public void restartBatchTimer() {
        LOG.debug("Restarting the batch timer");
        TimerTask timerTask = createNewBatchTimerTask();
        this.timer.cancel();
        this.timer = new Timer();
        this.timer.schedule(timerTask, this.batchTimeout * 1000, this.batchTimeout * 1000);
    }

    private TimerTask createNewBatchTimerTask() {
        Server server = this;
        TimerTask timer = new TimerTask() {
            @Override
            public void run() {
                LOG.debug("Timeout has expired, adding the batch to the task queue");
                synchronized (batch) {
                    Batch deepCopiedBatch = batch.deepCopy();
                    batch.clearBatch();
                    BatchTask batchTask = new BatchTask(deepCopiedBatch, server);
                    threadPoolManager.addNewTaskToWorkList(batchTask);
                }
            }
        };
        return timer;
    }
}
