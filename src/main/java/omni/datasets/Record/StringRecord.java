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
    public String[] getData(int numAttrs) {
        if (numAttrs > data.length) {
            throw new IllegalArgumentException("Requested number of attributes exceeds the length of the data array.");
        }
        String[] data = new String[numAttrs + 2];
        System.arraycopy(this.data, 0, data, 0, numAttrs + 1);
        data[numAttrs + 1] = this.data[this.data.length - 1];
        return data;
    }

    @Override
    public String[] getQueryData(int numAttrs) {
        if (numAttrs > data.length) {
            throw new IllegalArgumentException("Requested number of attributes exceeds the length of the data array.");
        }
        //String[] data = new String[numAttrs];
        //System.arraycopy(this.data, 0, data, 0, numAttrs);
        return data;
    }
}
