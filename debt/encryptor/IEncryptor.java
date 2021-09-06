package debt.encryptor;

import java.io.InputStream;
import java.security.InvalidKeyException;

public interface IEncryptor {
    InputStream encrypt(InputStream inputStream) throws InvalidKeyException;
    InputStream decrypt(InputStream inputStream) throws InvalidKeyException;
}
