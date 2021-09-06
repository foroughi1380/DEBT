package debt;

import debt.encryptor.Aes;
import debt.encryptor.Rsa;
import debt.keys.KeyConvertor;
import debt.keys.KeyGenerator;
import debt.store.StoreAtEnd;
import debt.store.StoredData;
import debt.utils.Utils;
import org.json.JSONObject;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Debt {
    public static final byte AESTAG = 1, RSATAG = 2;

    public void encryptAES(File behind, InputStream plain, OutputStream destination, String pass , HashMap<String, Object> ext) throws IOException, InvalidKeyException {
        Key encryptKey = KeyGenerator.generateAESKey();
        Aes aes = new Aes(encryptKey);

        //encrypting plain file
        InputStream fileEncryptedInputStream = aes.encrypt(plain);

        //create and encrypt extra
        InputStream extraEncrypted = new Aes(KeyConvertor.fromString(pass))
                .encrypt(new ByteArrayInputStream(createData(encryptKey, ext).getBytes()));

        byte[] extra = Utils.inputStreamToBytes(extraEncrypted );
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(new byte[]{AESTAG});
        stream.write(extra);
        extra = stream.toByteArray();


        StoreAtEnd.append(new FileInputStream(behind), destination, fileEncryptedInputStream, extra);
    }

    private DecryptedData decryptAES (RandomAccessFile target , String pass) throws IOException, InvalidKeyException {
        Key extraEncrypted = KeyConvertor.fromString(pass);
        StoredData data = StoreAtEnd.read(target);

        byte[] extra = Arrays.copyOfRange(data.getExtra() , 1, data.getExtra().length);

        Extra extras = readData(new String(Utils.inputStreamToBytes(new Aes(extraEncrypted).decrypt(new ByteArrayInputStream(extra)))));

        Key dataKey = extras.key;

        Aes aes = new Aes(dataKey);

        DecryptedData ret = new DecryptedData();
        ret.extra = extras.data.toMap();
        ret.data = aes.decrypt(data.getInput());
        return ret;
    }


    public void encryptRSA(File behind, InputStream plain, OutputStream destination, KeyPair key , HashMap<String, Object> ext) throws IOException, InvalidKeyException {
        Key encryptKey = KeyGenerator.generateAESKey();
        Aes aes = new Aes(encryptKey);

        //encrypting plain file
        InputStream fileEncryptedInputStream = aes.encrypt(plain);

        //create and encrypt extra
        InputStream extraEncrypted = new Rsa(key)
                .encrypt(new ByteArrayInputStream(createData(encryptKey, ext).getBytes()));
        byte[] extra = Utils.inputStreamToBytes(extraEncrypted);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(RSATAG);
        stream.write(extra);
        extra = stream.toByteArray();

        StoreAtEnd.append(new FileInputStream(behind), destination, fileEncryptedInputStream, extra);
    }

    private DecryptedData decryptRSA (RandomAccessFile target , KeyPair key) throws IOException, InvalidKeyException {
        StoredData data = StoreAtEnd.read(target);

        byte[] extra = Arrays.copyOfRange(data.getExtra() , 1, data.getExtra().length);

        Extra extras = readData(new String(Utils.inputStreamToBytes(new Rsa(key).decrypt(new ByteArrayInputStream(extra)))));

        Key dataKey = extras.key;

        Aes aes = new Aes(dataKey);

        DecryptedData ret = new DecryptedData();
        ret.extra = extras.data.toMap();
        ret.data = aes.decrypt(data.getInput());
        return ret;
    }



    public DecryptedData decrypt(RandomAccessFile file , KeyPair keyPair , IDecryptRequestListener listener) throws IOException, InvalidKeyException {
        StoredData fileParts = StoreAtEnd.read(file);
        switch (fileParts.getExtra()[0]){
            case AESTAG:
                String pass = listener.requestPassword();
                return decryptAES(file , pass);
            case RSATAG:
                return decryptRSA(file , keyPair);
        }
        return null;
    }




    private String createData(Key key, HashMap<String, Object> data) {
        JSONObject json = new JSONObject();

        json.put("VERSION", Config.DEBT_VERSION);
        json.put("KEY", KeyConvertor.convertKeyToString(key));

        JSONObject d = new JSONObject();
        data.forEach(d::put);

        json.put("DATA", d);
        return json.toString();
    }

    private Extra readData(String json) {
        Extra ret = new Extra();
        JSONObject data = new JSONObject(json);

        ret.version = data.getInt("VERSION");
        ret.key = KeyConvertor.convertStringToKey(data.getString("KEY"));
        ret.data = data.getJSONObject("DATA");

        return ret;
    }




    public static class Extra {
        JSONObject data;
        Key key;
        int version;
    }

    public static class DecryptedData {
        Map<String, Object> extra;
        InputStream data;

        public InputStream getData() {
            return data;
        }

        public void setData(InputStream data) {
            this.data = data;
        }

        public Map<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, Object> extra) {
            this.extra = extra;
        }
    }

    public interface IDecryptRequestListener{
         String requestPassword();
    }
}
