package omni.synopses.baselines.readFile;

import omni.Experiments.utils.QueryInfo;
import omni.datasets.Record.Record;
import omni.synopses.SynopsisRefactor;
public class readFile extends SynopsisRefactor {
    public readFile() {

        setting = "readFile";

    }

    @Override
    public void add(long[] record) {
        int id = (int) record[0]; // Assuming the first element is the ID
    }

    @Override
    public void add(Record record) {
        int id = Integer.parseInt((String) record.getValue(0)); // Assuming the first value is the ID
    }

    public void ingest(long[] record, int sign) {

    }

    public void ingest(Record record, int sign) {

    }

    @Override
    public int query(long[] query, int numPreds) {
        throw new UnsupportedOperationException("Not supported anymore.");
    }

    @Override
    public int query(Record query, int numPreds, QueryInfo queryInfo) {
        return 0;
    }

    @Override
    public int query(long[] query, int numPreds, QueryInfo queryInfo) {
        return 0;
    }

    @Override
    public int query(long[] query, int numPreds, int unionSize, QueryInfo CMRow) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int rangeQuery(long[] minrange, long[] maxrange) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {

    }

    @Override
    public void delete(long[] r) {
        int id = (int) r[0]; // Assuming the first element is the ID
    }

    @Override
    public void delete(Record r) {
        int id = Integer.parseInt((String) r.getValue(0)); // Assuming the first value is the ID
    }

    @Override
    public int getFilledKSamples() {
        return 0;
    }

    public long getMemoryUsage() {
        //double v = 1 / (1 - bufferFactor);
        return 0;
//        return (long) maxSize * 32 * (numAttrs + 5); // size * (size of record + size of Double array + Tis array)
    }

    public void printParams() {
        System.out.println();
    }
}
