package cs455.scaling.util;

import java.nio.channels.SelectionKey;

public class DataAndSelectionKeyPair {
    public final byte[] data;
    public final SelectionKey key;

    public DataAndSelectionKeyPair(byte[] data, SelectionKey key) {
        this.data = data;
        this.key = key;
    }
}
