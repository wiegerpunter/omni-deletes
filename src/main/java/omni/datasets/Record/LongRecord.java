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
    public Object getData() {
        return data;
    }
}
