package debt.keys;

//import debt.store.Encryptor;
import debt.utils.Utils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyConvertor {

    private static final String KEYS_TAG_NAME = "keys";
    private static final String KEY_TAG_NAME = "key";
    private static final String TYPE_ATTR_NAME = "type";

    private static final String PUBLIC_KEY_TYPE = "public";
    private static final String PRIVATE_KEY_TYPE = "private";


    public static String ConvertToString(KeyPair keys){
        Element root = new Element(KEYS_TAG_NAME);

        if (keys.getPublic()!=null){
            root.getChildren().add(createKeyElement(PUBLIC_KEY_TYPE , keys.getPublic().getEncoded()));
        }

        if (keys.getPrivate()!=null){
            root.getChildren().add(createKeyElement(PRIVATE_KEY_TYPE , keys.getPrivate().getEncoded() ));
        }

        return new XMLOutputter().outputString(new Document(root));
    }

    private static Element createKeyElement(String type , byte[] encoded){
        Element el = new Element(KEY_TAG_NAME);
        el.setAttribute(TYPE_ATTR_NAME , type);

        String text = "";

        switch (type){
            case PUBLIC_KEY_TYPE:
                X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encoded);
                text = Base64.getEncoder().encodeToString(x509EncodedKeySpec.getEncoded());
                break;
            case PRIVATE_KEY_TYPE:
                PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encoded);
                text = Base64.getEncoder().encodeToString(pkcs8EncodedKeySpec.getEncoded());
                break;
        }

        el.setText(text);
        return el;
    }

    public static KeyPair ConvertToKeyPair(String temp) throws JDOMException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        PrivateKey privateKey = null;
        PublicKey publicKey = null;

        SAXBuilder builder = new SAXBuilder();
        InputStream stream = new ByteArrayInputStream(temp.getBytes(StandardCharsets.UTF_8));

        Document document = builder.build(stream);
        for (Element el : document.getRootElement().getChildren(KEY_TAG_NAME)){
            byte[] encoded = Base64.getDecoder().decode(el.getText());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            switch (el.getAttribute(TYPE_ATTR_NAME).getValue()){
                case PUBLIC_KEY_TYPE:
                    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encoded);
                    publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
                    break;
                case PRIVATE_KEY_TYPE:
                    PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encoded);
                    privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
                    break;
            }
        }

        return new KeyPair(publicKey , privateKey);
    }

    public static  String convertKeyToString(Key key){
        return  Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static  Key convertStringToKey(String key){
        return   new SecretKeySpec(Base64.getDecoder().decode(key) , "AES");
    }

    public static Key fromString(String s){
        byte[] byte_key = Utils.byteToHash(s.getBytes());
        return new SecretKeySpec(byte_key, "AES");
    }

}
