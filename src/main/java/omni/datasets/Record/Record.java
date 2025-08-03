package omni.datasets.Record;

public interface Record {
    Object getValue(int index);
    int length();
    Object getData(int numAttrs);

    void setValue(int index, int i);
}
