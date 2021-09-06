package debt.encryptor;

import java.security.*;

public class DigitalSignature {
    private static final String SIGNING_ALGORITHM = "SHA256withRSA";
    private static final String MESSAGE = "DEBT SIGNATURE";

    public static byte[] create(KeyPair keyPair) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
        signature.initSign(keyPair.getPrivate());
        signature.update(MESSAGE.getBytes());
        return signature.sign();
    }

    public static boolean check(KeyPair key , byte[] sig){
        try{
            Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
            signature.initVerify(key.getPublic());
            signature.update(MESSAGE.getBytes());
            return signature.verify(sig);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}
