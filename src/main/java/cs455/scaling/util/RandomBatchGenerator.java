package cs455.scaling.util;

import java.util.ArrayList;
import java.util.Random;

public class RandomBatchGenerator {

    public static ArrayList<Batch> generateAListOfBatches(int batchSize, int numberOfBatchesToGenerate) {
        ArrayList<Batch> batchList = new ArrayList<>();
        Random rand = new Random();

        for (int i=0; i < numberOfBatchesToGenerate; i++) {
            Batch batch = new Batch(batchSize) ;
            for (int j=0; j < batchSize; j++) {
                byte[] data = new byte[8000];
                rand.nextBytes(data);
                batch.addDataToBatch(data);
            }
            batchList.add(batch);
        }

        return batchList;
    }

}
