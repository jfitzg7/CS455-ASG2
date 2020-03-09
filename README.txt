Author: Jack Fitzgerald
Date: 3/8/2020
Assignment 2: Scalable Server Design: Using Thread Pools & Micro-Batching
to Manage and Load Balance Active Network Connections

HOW TO BUILD:


HOW TO RUN THE SERVER:


HOW TO RUN THE CLIENT:


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

        - Client:

    cs455.scaling.server:

        - Server:

    cs455.scaling.task:

        - Task:

        - BatchTask

        - ReadTask:

        - RegisterTask:

        - WriteTask:

        - TestTask:

    cs455.scaling.util:

        - Batch:

        - ClientSendMessageThread:

        - ClientSideStatisticsGatherer:

        - ServerSideStatisticsGatherer:

        - Hashing:

        - HashAndSelectionKeyPair:

        - ThreadPoolManager:

        - ThreadPool:

        - WorkerThread:
