package debt.store;

import java.io.InputStream;

public class StoredData {
    private byte[] extra;
    private InputStream input;

    public StoredData(byte[] extra, InputStream input) {
        this.extra = extra;
        this.input = input;
    }

    public byte[] getExtra() {
        return extra;
    }

    public void setExtra(byte[] extra) {
        this.extra = extra;
    }

    public InputStream getInput() {
        return input;
    }

    public void setInput(InputStream input) {
        this.input = input;
    }


}
