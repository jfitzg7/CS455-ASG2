Author: Jack Fitzgerald
Date: 3/8/2020
Assignment 2: Scalable Server Design: Using Thread Pools & Micro-Batching
to Manage and Load Balance Active Network Connections


HOW TO BUILD:

1. Stay in the same directory where the tar file was unpacked.

2. Execute 'gradle build' at the command line to build the project and create the build directory.
    - Alternatively, you can run './gradlew build' to use the gradle wrapper instead.


HOW TO RUN THE SERVER:

1. After building the project, stay in the same directory where you executed the build command to keep the classpath consistent.

2. execute the command: java -cp build/libs/ASG2-1.0.jar cs455.scaling.server.Server portnum thread-pool-size batch-size batch-time
    - Example: java -cp build/libs/ASG2-1.0.jar cs455.scaling.server.Server 7000 12 10 2.5
        - This will execute the server on port 7000, listening on any IP address (0.0.0.0), with 12 threads in the thread pool,
        a batch size of 10, and a batch time of 2.5 seconds.


HOW TO RUN THE CLIENT:

1. Make sure you are in the same directory where you originally built the project.

2. execute the command: java -cp build/libs/ASG2-1.0.jar cs455.scaling.client.Client server-host server-port message-rate
    - Example: java -cp build/libs/ASG2-1.0.jar cs455.scaling.client.Client denver.cs.colostate.edu 7000 4
        - This will execute a client that will try to connect to a server that is running on the denver machine
        on port 7000. If it successfully connects to the server, then it will start sending messages at the specified rate.


OVERVIEW:

When the server starts, and clients start connecting, it will only generate a single RegisterTask at a time.
It does this by removing OP_ACCEPT interest from the interestOps of the channel when there is a new incoming connection,
and then adds OP_ACCEPT interest back to the interestOps once accept() has been called in the RegisterTask. This is done
to avoid generating unnecessary RegisterTasks due to the delay of adding, removing, and processing the task,
and to avoid unnecessary cycles through the select loop in the main server thread.

The server will only generate a single ReadTask per client channel until it has filled the 8KB buffer associated
with each ReadTask. When a client sends a message, the OP_READ interest is removed from the client's channel and then
it is added back once the ReadTask has finished reading. The hashes are generated in the ReadTask and then passed to
a new WriteTask that gets added to a batch. The ReadTask performs the hashing right away to avoid storing the 8KB message.


IMPORTANT THINGS TO NOTE:

- The batch-size option must be an integer, but the batch-time option can be a floating point number. The batch-time will
be multiplied by 1000 to convert to milliseconds, and it will be rounded with Math.round().

- ReadTasks and WriteTasks are batched, while RegisterTasks are not batched. A RegisterTask will be immediately added to the
work queue for a worker thread to execute.
    - Because ReadTasks are batched, and only one ReadTask per channel can be active at a time, this can lead to reduced
    throughput with a small number of clients if the batch-size and batch-time options are set too high. For example,
    if there is only 1 client sending 4 messages per second, then the batch-size could be set to 1, or the batch-time
    can be set to 0.25 in order to keep up with the client and not reduce throughput.

FILE/CLASS DESCRIPTIONS:

    cs455.scaling.client:

        - Client: The class executed on the client side. It handles sending and receiving messages with the Server class.

    cs455.scaling.server:

        - Server: The class executed on the server side. It manages connections with clients, reads data from connected clients,
        and finally sends the hashed data back to the clients.

    cs455.scaling.task:

        - Task: An functional interface which all Task type objects must implement.

        - BatchTask: A task for executing Task objects in a Batch.

        - ReadTask: A task for reading data, and subsequently hashing the data, from a specific client.

        - RegisterTask: A task for registering a new client with the selector.

        - WriteTask: A task for sending the hashed data back to the client.

        - TestTask: A task for testing functionality of the thread pool.

    cs455.scaling.util:

        - Batch: A class that holds a certain amount of Task objects before being added to the work queue.
        This class is intended to reduce context switching.

        - ClientSendMessageThread: The thread on the client side that sends a specific amount of messages to the
        server per second.

        - ClientSideStatisticsGatherer: Handles statistics gathering on the client side, counts total sent and received
        messages in the past 20 seconds.

        - ServerSideStatisticsGatherer: Handles statistics gathering for the past 20 seconds on the server side. It
        counts total messages processed per second, the total number of active clients, the average number of messages
        processed per second, and the the standard deviation of the messages processed per second.

        - Hashing: Defines a static method intended for converting the 8KB messages into a SHA1 hashed byte[].

        - HashAndSelectionKeyPair: Defines a hash byte[] and a SelectionKey so that the WriteTask knows what to send
        and to which specific client it needs to send to.

        - ThreadPoolManager: Contains the thread pool of worker threads, the work queue, and a Batch object which is added
        to the work queue once it reaches the batch size or the batch time is exceeded.

        - ThreadPool: Contains a method for starting up the WorkerThreads in the thread pool.

        - WorkerThread: The threads comprising the thread pool. Executes Task objects that are added to the work queue.

    src/main/resources

        - log4j2.xml: configuration file for log4j.

        - machine_list: list of machines in the CS120 lab to be used with the startup_script.sh

        - startup_script.sh: a script for automatically starting up clients to connect to a server.
