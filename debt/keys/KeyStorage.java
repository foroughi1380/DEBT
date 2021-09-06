package debt.keys;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyPair;

public class KeyStorage {
    public static boolean store(File file , KeyPair key , String pass){
        try{
            String d = KeyConvertor.ConvertToString(key);

            Key pk = KeyConvertor.fromString(pass);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE , pk);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(new FileOutputStream(file) , cipher);
            cipherOutputStream.write(d.getBytes());
            cipherOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static KeyPair load(File file , String pass){
        try {
            Key pk = KeyConvertor.fromString(pass);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE , pk);
            CipherInputStream cipherInputStream = new CipherInputStream(new FileInputStream(file) , cipher);

            StringBuilder p = new StringBuilder();
            int i;
            while ((i = cipherInputStream.read()) != -1){
                p.append((char) i);
            }

            cipherInputStream.close();

            return KeyConvertor.ConvertToKeyPair(p.toString());
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
