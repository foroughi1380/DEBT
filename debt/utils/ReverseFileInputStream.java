package debt.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class ReverseFileInputStream extends InputStream {
    private long cursor;
    private RandomAccessFile input;

    public ReverseFileInputStream(RandomAccessFile input) throws IOException {
        this.input = input;
        this.cursor = input.length();
    }

    @Override
    public int read() throws IOException {
        if (this.cursor == -1){
            return -1;
        }
        this.input.seek(--this.cursor);
        return input.read();
    }

    @Override
    public synchronized void reset() throws IOException {
        cursor = input.length();
        input.seek(cursor);
    }

    @Override
    public long skip(long n) throws IOException {
        cursor -= n;
        input.seek(cursor);
        return n;
    }

    @Override
    public void close() throws IOException {
        super.close();
        input.close();
    }

}
