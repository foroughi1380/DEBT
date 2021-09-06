package debt.keys;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;

public class KeyGenerator {
    public static KeyPair generateKeyPair() {
        try{
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public static Key generateAESKey(){
        try{
            javax.crypto.KeyGenerator generator = javax.crypto.KeyGenerator.getInstance("AES");
            generator.init(256);
            return generator.generateKey();
        }catch (Exception e){ e.printStackTrace(); }
        return null;
    }


    public static Key generateAesKeyFromString(String key){
        try{
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] byte_key = md.digest(key.getBytes());
            return new SecretKeySpec(byte_key , "AES");
        }catch (Exception e){e.printStackTrace();}
        return  null;
    }
}
