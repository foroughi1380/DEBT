package debt;

import debt.encryptor.DigitalSignature;
import debt.utils.FilePartInputStream;
import debt.utils.Utils;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DebtHelper {
    private Debt debt;
    private KeyPair keys;
    public DebtHelper(KeyPair selfKeys){
        this.debt = new Debt();
        this.keys = selfKeys;
    }

    public boolean dataEncryptionBehindTheFiles(File target , File directory , OutputStream output) throws DirectoryIsEmptyException, IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        return dataEncryptionBehindTheFiles(target , directory , output ,null);
    }

    public boolean dataEncryptionBehindTheFiles(File target , File directory , OutputStream fileOutput , String pass) throws DirectoryIsEmptyException, IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        File[] files = directory.listFiles(File::isFile);
        if (files.length == 0) throw new DirectoryIsEmptyException(directory);

        ZipOutputStream output = new ZipOutputStream(fileOutput);
        FilePartInputStream partInputStream = new FilePartInputStream(target , files.length);


        HashMap<String , Object> extras = new HashMap<>();
        extras.put("filename" , target.getName());
        extras.put("signature" , DigitalSignature.create(this.keys));
        extras.put("totalparts" , files.length);
        extras.put("filetype" , Utils.getFileType(target));

        for (int i = 0 ; i < files.length ; i++) {
            ZipEntry entry = new ZipEntry(files[i].getName());
            output.putNextEntry(entry);
            extras.put("part" , i);
            if (pass == null){
                debt.encryptRSA(files[i] , partInputStream , output , this.keys , extras);
            }else{
                debt.encryptAES(files[i] , partInputStream , output , pass , extras);
            }

            partInputStream.nextPart();
        }

        output.close();
        partInputStream.close();
        return true;
    }

    public Debt.DecryptedData dataDecryptionBehindTheFiles(File directory) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, DirectoryIsEmptyException, IOException {
        return dataDecryptionBehindTheFiles(directory , null);
    }

    public Debt.DecryptedData dataDecryptionBehindTheFiles(File directory , String pass) throws DirectoryIsEmptyException, IOException, InvalidKeyException {
        File[] files = directory.listFiles(File::isFile);
        if (files.length == 0) throw new DirectoryIsEmptyException(directory);
        Map<String , Object> extra = null;
        InputStream[] inputs = null;
        for (File file : files) {
            try{
                Debt.DecryptedData data = debt.decrypt(new RandomAccessFile(file , "r") , this.keys , ()->pass);

                extra = data.extra;
                if (inputs == null){
                    inputs = new InputStream[(int) extra.get("totalparts")];
                }
                inputs[(int) extra.get("part")] = data.getData();

            }catch (IOException e){e.printStackTrace();}
        }

        Debt.DecryptedData ret = new Debt.DecryptedData();
        ret.setExtra(extra);

        final InputStream[] finalInputs = inputs;
        ret.setData(new InputStream() {
            int point = 0;
            @Override
            public int read() throws IOException {
                int r = finalInputs[point].read();
                if (r == -1 && point < finalInputs.length -1){
                    point++;
                    return read();
                }
                return r;
            }

            @Override
            public void close() throws IOException {
                for (InputStream input : finalInputs) {
                    input.close();
                }
            }

            @Override
            public int available() throws IOException {
                return finalInputs[this.point].available();
            }
        });

        return ret;
    }


    // Exceptions
    public static class DirectoryIsEmptyException extends Exception{
        public DirectoryIsEmptyException(File directory){
            super(directory.getAbsolutePath() + "is an empty directory");
        }
    }
}

