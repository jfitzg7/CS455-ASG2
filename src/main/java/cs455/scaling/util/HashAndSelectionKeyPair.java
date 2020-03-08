package cs455.scaling.util;

import java.nio.channels.SelectionKey;

public class HashAndSelectionKeyPair {
    public final byte[] hash;
    public final SelectionKey key;

    public HashAndSelectionKeyPair(byte[] hash, SelectionKey key) {
        this.hash = hash;
        this.key = key;
    }
}
