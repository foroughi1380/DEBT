package debt.utils;

import java.io.*;

public class FilePartInputStream extends InputStream {
    private int partCount , currentPart , readBytes , partSize;
    private RandomAccessFile file;
    public FilePartInputStream(File file , int partCount) throws FileNotFoundException {
        this.file = new RandomAccessFile(file , "r");
        this.partCount = partCount;
        this.currentPart = 0;
        this.readBytes = 0;
        this.partSize = (int) (file.length() / partCount);
    }

    @Override
    public int read() throws IOException {
        if (readBytes++ < partSize || currentPart == partCount - 1){
            return this.file.read();
        }
        else{
            return -1;
        }
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    public void nextPart() throws IOException {
        setPart(this.currentPart + 1);
    }

    public void setPart(int part) throws IOException {
        this.currentPart = part;
        this.readBytes = 0;
        this.file.seek(currentPart * partSize);
    }

    public int getPart() {
        return currentPart;
    }

}
