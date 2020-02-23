package cs455.scaling.util;

import java.util.LinkedList;

public class Batch {
    private final LinkedList<byte[]> dataList;
    private final int batchSize;

    public Batch(int batchSize) {
        this.dataList = new LinkedList<>();
        this.batchSize = batchSize;
    }

    public boolean addDataToBatch(byte[] data) {
        if (dataList.size() < batchSize) {
            dataList.addLast(data);
            return true;
        }
        else {
            return false;
        }
    }

    public int sizeOfDataList() {
        return dataList.size();
    }

    public byte[] removeDataFromBatch() {
        return dataList.removeFirst();
    }
}
