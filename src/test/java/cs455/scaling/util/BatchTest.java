package cs455.scaling.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BatchTest {

    Batch batch;

    @BeforeEach
    public void initialize() {
        batch = new Batch(5);
    }

    @Test
    public void addDataToBatchTest() {
        Random rand = new Random();
        for (int i=0; i < 5; i++) {
            byte[] data = new byte[8000];
            rand.nextBytes(data);
            assertTrue(batch.addDataToBatch(data));
        }
    }

    @Test
    public void addTooMuchDataToBatchTest() {
        Random rand = new Random();
        for (int i=0; i < 5; i++) {
            byte[] data = new byte[8000];
            rand.nextBytes(data);
            batch.addDataToBatch(data);
        }
        byte[] data = new byte[8000];
        rand.nextBytes(data);
        assertFalse(batch.addDataToBatch(data));
    }

    @Test
    public void sizeOfDataListTest() {
        Random rand = new Random();
        for (int i=0; i < 5; i++) {
            byte[] data = new byte[8000];
            rand.nextBytes(data);
            batch.addDataToBatch(data);
        }
        assertTrue(batch.sizeOfDataList() == 5);
    }

    @Test
    public void removeDataFromBatchTest() {
    }
}