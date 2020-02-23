package cs455.scaling.task;

import cs455.scaling.util.Batch;
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
        int numberOfItemsInBatch = batch.sizeOfDataList();
        for (int i=0; i < numberOfItemsInBatch; i++) {
            byte[] data = batch.removeDataFromBatch();
            String hashString = SHA1FromBytes(data);
            LOG.debug("computed hash: " + hashString);
        }
    }

    public String SHA1FromBytes(byte[] data) {
        String hashString = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            byte[] hash = digest.digest(data);
            BigInteger hashInt = new BigInteger(1, hash);

            hashString = hashInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error occurred while converting byte data to hash", e);
        }
        return hashString;
    }
}
