package debt.encryptor;

import debt.utils.Utils;

import javax.crypto.Cipher;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class Rsa implements IEncryptor {
    private static final String ALGORITHM = "RSA";
    public static final int READ_DATA_BUFFER_SIZE = 245;

    private final KeyPair key;
    public Rsa(KeyPair key){
        this.key = key;
    }

    @Override
    public InputStream encrypt(InputStream inputStream) throws InvalidKeyException {
        Cipher cipher;
        try{
            cipher = Cipher.getInstance(ALGORITHM);
        }catch (Exception e){
            throw new RuntimeException();
        }

        cipher.init(Cipher.ENCRYPT_MODE , this.key.getPublic());

        LinkedList<Integer> bytesList = new LinkedList<>();

        OutputStream bytesOutput = new OutputStream() {
            @Override
            public void write(int b)  {
                bytesList.add(b);
            }

            @Override
            public void write(byte[] bs)  {
                for (byte b : bs) {
                    bytesList.add(Byte.toUnsignedInt(b));
                }
            }
        };


        return new InputStream() {
            @Override
            public int read()  {
                try {
                    return bytesList.pop();
                }catch (NoSuchElementException noEl){
                    try{
                        byte[] r = Utils.readBuff(inputStream , READ_DATA_BUFFER_SIZE);
                        if (r.length != 0)
                            bytesOutput.write(cipher.doFinal(r));
                        return bytesList.pop();
                    }catch (Exception e ){
                        return -1;
                    }
                }
            }
        };
    }

    @Override
    public InputStream decrypt(InputStream inputStream) throws InvalidKeyException {
        Cipher cipher;

        try{
            cipher = Cipher.getInstance(ALGORITHM);
        }catch (Exception e){
            throw new RuntimeException();
        }

        cipher.init(Cipher.DECRYPT_MODE , this.key.getPrivate());



        return new InputStream() {
            private final LinkedList<Integer> bytes = new LinkedList<>();

            private final byte[] temp = new byte[256];

            @Override
            public int read() {
                try {
                    return bytes.pop();
                }catch (NoSuchElementException noEl){
                    try{
                        if ( inputStream.read(temp) != -1){
                            byte[] r = cipher.doFinal(temp);
                            for (byte b: r) {
                                bytes.add(Byte.toUnsignedInt(b));
                            }
                        }
                        return bytes.pop();
                    }catch (Exception e){
                        return -1;
                    }
                }
            }
        };
    }
}
