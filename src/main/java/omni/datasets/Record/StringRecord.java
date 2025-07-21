package omni.datasets.Record;

public class StringRecord implements Record {
    private final String[] data;
    private int sign;

    public StringRecord(String[] data) {
        this.data = data;
    }

    @Override
    public Object getValue(int index) {
        return data[index];
    }

    @Override
    public void setValue(int index, int i) {
        data[index] = String.valueOf(i);
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public String[] getData() {
        return data;
    }
}
