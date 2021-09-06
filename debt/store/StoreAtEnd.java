package debt.store;

import debt.utils.ReverseFileInputStream;
import debt.utils.Utils;
import sun.security.util.ArrayUtil;

import java.io.*;

public class StoreAtEnd {

    private static final String DEBT = "DEBT";

    /**
     * This method reverse the data and write to output
     * / file data / file data length / extra / extra length / DEBT;
     */
    public static void append(InputStream target , OutputStream output , byte[] data , byte[] extra) throws IOException{
        append(target , output , new ByteArrayInputStream(data) , extra);
    }

    public static void append(InputStream target , OutputStream output , InputStream data , byte[] extra) throws IOException {
        byte[] temp = null;

        //write target file
        Utils.copyStreamToStream(target , output);

        //write file byte
        int fileSize = Utils.copyStreamToStream(data , output);


        //write file byte size
        temp = Utils.intToBytes(fileSize);
        ArrayUtil.reverse(temp);
        output.write(temp);

        //write extra
        temp = extra;
        ArrayUtil.reverse(temp);
        output.write(temp);

        // write extra size
        temp = Utils.intToBytes(extra.length);
        ArrayUtil.reverse(temp);
        output.write(temp);

        //write DEBT
        temp = DEBT.getBytes();
        ArrayUtil.reverse(temp);
        output.write(temp);

    }

    public static StoredData read(RandomAccessFile target) throws IOException {
        ReverseFileInputStream input = new ReverseFileInputStream(target);
        //read EMI
        byte[] temp = new byte[DEBT.length()];
        input.read(temp);

        String s = new String(temp);

        // check DEBT file
        if (! Utils.equalByteArrays(temp, DEBT.getBytes())){
            input.close();
            throw new IOException("not valid file");
        }

        // read data size
        temp = new byte[Integer.BYTES];
        if (input.read(temp) == -1){
            input.close();
            throw new IOException("not valid file");
        }
        int el = Utils.bytesToInt(temp);

        // read data
        temp = new byte[el];
        input.read(temp);
        byte[] extra = temp.clone();

        //read file byte size
        temp = new byte[Integer.BYTES];
        input.read(temp);
        int fl = Utils.bytesToInt(temp);


//        InputStream i = new InputStream() {
//            int readied = 0;
//            final int max = fl;
//            @Override
//            public int read() throws IOException {
//                if (readied++ > max - 1){
//                    return -1;
//                }
//
//                return input.read();
//            }
//
////            @Override
////            public int read(byte[] b) throws IOException {
////                return read(b, 0, b.length);
////            }
////
////            @Override
////            public int read(byte[] b, int off, int len) throws IOException {
////                if (b == null) {
////                    throw new NullPointerException();
////                } else if (off < 0 || len < 0 || len > b.length - off) {
////                    throw new IndexOutOfBoundsException();
////                } else if (len == 0) {
////                    return 0;
////                }
////
////                int c = this.read();
////                if (c == -1) {
////                    return -1;
////                }
////                b[off] = (byte)c;
////
////                int i = 1;
////                try {
////                    for (; i < len ; i++) {
////                        c = this.read();
////                        if (c == -1) {
////                            break;
////                        }
////                        b[off + i] = (byte)c;
////                    }
////                } catch (IOException ee) {
////                }
////                return i;
////            }
//
//            @Override
//            public synchronized void reset() throws IOException {
//                input.reset();
//                input.skip(3 + dl + 8);
//                this.readied = 0;
//            }
//
//            @Override
//            public void close() throws IOException {
//                input.close();
//            }
//
//            @Override
//            public int available() throws IOException {
//                return max - readied;
//            }
//
//            @Override
//            public long skip(long n) throws IOException {
//                return input.skip(n);
//            }
//
//            @Override
//            public synchronized void mark(int readlimit) {
//                input.mark(readlimit);
//            }
//
//            @Override
//            public boolean markSupported() {
//                return input.markSupported();
//            }
//
//
//        };

        int footerSize = el + DEBT.length() + Integer.BYTES + Integer.BYTES; // extra length + DEBT char len + extra data size + data data Size
        long zeroPoint = target.length() - (footerSize + fl);

        target.seek(zeroPoint);
        InputStream i = new InputStream(){
            int pos = 0;
            @Override
            public int read() throws IOException {
                if (++pos > fl){
                    return -1;
                }
                return target.read();
            }

            @Override
            public int available() throws IOException {
                return (int) (target.length() - (footerSize + fl) - pos);
            }

            @Override
            public synchronized void reset() throws IOException {
                target.seek(zeroPoint);
            }
        };

        return new StoredData(extra, i);
    }

}
