package cs455.scaling.task;

import cs455.scaling.util.Batch;
import cs455.scaling.util.Hashing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BatchTask implements Task {

    private Logger LOG = LogManager.getLogger(BatchTask.class);

    private final Batch batch;

    public BatchTask(Batch batch) {
        this.batch = batch;
    }

    @Override
    public void executeTask() {
        LOG.info("Executing a batch task");
        int numberOfItemsInBatch = batch.sizeOfDataList();
        for (int i=0; i < numberOfItemsInBatch; i++) {
            byte[] data = batch.removeDataFromBatch();
            String hashString = Hashing.SHA1FromBytes(data);
            LOG.debug("computed hash: " + hashString);
        }
    }
}
