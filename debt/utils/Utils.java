package debt.utils;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.KeyRep;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public class Utils {
    public static final int AES_KEY_SIZE = 256;
    public static final String AES = "AES";

    public static byte[] shortToBytes(short n){
        byte[] ret= new byte[2];
        ret[0] = (byte) (n >>>8);
        ret[1] = (byte) (n & 0xff);
        return ret;
    }
    public static short bytesToShort(byte[] n){
        return  (short) ((n[0] << 8) | (0xff & n[1]));
    }

    public static byte[] readBuff(InputStream stream , int size){
        int count = 0;
        byte[] buff= new byte[size];
        for (; count < size ; count++){
            try{
                int r = stream.read();
                if (r == -1) break;
                buff[count] = (byte) r;
            }catch (Exception e){
                break;
            }
        }

        if (count != size){
            buff = Arrays.copyOfRange(buff , 0 , count);
        }

        return buff;
    }



    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }


    public static byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(x);
        return buffer.array();
    }

    public static int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getInt();
    }

    public static boolean equalByteArrays(byte[] a , byte[] b){
        if (a.length != b.length)
            return false;

        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]){
                return false;
            }
        }

        return true;
    }


    public static final String PREFIX = "debttemp";
    public static final String SUFFIX = ".tmp";

    public static File streamToFile (InputStream in) throws IOException {
        final File tempFile = File.createTempFile(PREFIX, SUFFIX);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            int i;
            while ((i = in.read()) != -1){
                out.write(i);
            }
        }
        return tempFile;
    }


    public static String byteToHashString(byte[] bytes ){

        try{
            return new String(Base64.getEncoder().encode(byteToHash(bytes)));
        }catch (Exception e){
            return null;
        }

    }
    public static byte[] byteToHash(byte[] bytes ){

        try{
            return MessageDigest.getInstance("MD5").digest(bytes);
        }catch (Exception e){
            return null;
        }

    }

    public static void writeInputToOutput(InputStream input , OutputStream output){
        try{
            int i;
            while ((i = input.read()) != -1){
                output.write(i);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String inputStreamToString(InputStream stream) throws IOException {
        return new String(inputStreamToBytes(stream));
    }

    public static byte[] inputStreamToBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int i = stream.read() ; i != -1 ; i = stream.read()){
            output.write(i);
        }
        return output.toByteArray();
    }

    public static int copyStreamToStream(InputStream inp , OutputStream out) throws IOException{
        int ret = 0;
        for (int i = inp.read() ; i  != -1 ; i = inp.read() , ret++){
            out.write(i);
        }
        return ret;
    }

    public static String getFileType(File file){
        try{
            return Files.probeContentType(file.toPath());
        }catch (Exception e){
            return null;
        }
    }

    public static FileTypes getFileType(String type){
        switch (type){
            case "image/png":
                return FileTypes.png;
            case "image/jpg":
            case "image/jpeg":
                return FileTypes.jpg;

            case "video/mp4":
                return FileTypes.mp4;

            default:
                return FileTypes.other;
        }
    }

//
//    public static <T> ArrayList<T> mergeArrays(T... arrays){
//        int count = 0;
//
//        for (T[] array : arrays) {
//            count += array.length;
//        }
//
//        T[] ret = (T[]) new Objects[count];
//
//        int i = 0;
//        for (T[] array : arrays) {
//            for (T a : array) {
//                ret[i++] = a;
//            }
//        }
//
//        return ret;
//    }
}
