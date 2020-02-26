package cs455.scaling.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {

    private static Logger LOG = LogManager.getLogger(Hashing.class);

    public static byte[] SHA1FromBytes(byte[] data) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error occurred while converting byte data to hash", e);
        }
        byte[] hash = digest.digest(data);
        return hash;
    }

    public static String SHA1StringFromBytes(byte[] data) {
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
