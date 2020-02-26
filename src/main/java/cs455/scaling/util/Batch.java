package cs455.scaling.util;

import java.util.LinkedList;

public class Batch {
    private final LinkedList<DataAndSelectionKeyPair> dataList;
    private final int batchSize;

    public Batch(int batchSize) {
        this.dataList = new LinkedList<>();
        this.batchSize = batchSize;
    }

    public boolean addDataToBatch(DataAndSelectionKeyPair pair) {
        if (dataList.size() < batchSize) {
            dataList.addLast(pair);
            return true;
        }
        else {
            return false;
        }
    }

    public int sizeOfDataList() {
        return dataList.size();
    }

    public DataAndSelectionKeyPair removeDataFromBatch() {
        return dataList.removeFirst();
    }

    public boolean isBatchFull() {
        if (dataList.size() >= batchSize) {
            return true;
        }
        else {
            return false;
        }
    }

    public void clearBatch() {
        dataList.clear();
    }

    public Batch deepCopy() {
        Batch deepCopiedBatch = new Batch(batchSize);
        //The DataAndSelectionKeyPairs only need to be shallow copies
        for (DataAndSelectionKeyPair pair : dataList) {
            deepCopiedBatch.addDataToBatch(pair);
        }
        return deepCopiedBatch;
    }
}
