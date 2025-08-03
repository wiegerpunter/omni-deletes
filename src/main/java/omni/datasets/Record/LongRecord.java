package omni.datasets.Record;

public class LongRecord implements Record {
    private final long[] data;

    public  LongRecord(long[] data) {
        this.data = data;
    }

    public Object getValue(int index) {
        return data[index];
    }

    @Override
    public void setValue(int index, int i) {
        data[index] = i;
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public Object getData(int numAttrs) {
        if (numAttrs > data.length) {
            throw new IllegalArgumentException("Requested number of attributes exceeds the length of the data array.");
        }
        long[] data = new long[numAttrs + 2];
        System.arraycopy(this.data, 0, data, 0, numAttrs + 1);
        data[numAttrs + 2] = this.data[this.data.length - 1];
        return data;
    }
}
