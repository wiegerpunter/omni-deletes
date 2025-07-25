package omni.synopses.baselines.resSample;
import omni.Experiments.utils.QueryInfo;
import omni.datasets.Record.Record;
import omni.synopses.SynopsisRefactor;

import java.util.Arrays;
import java.util.Random;

public class ReservoirSample extends SynopsisRefactor {
    private long[][] reservoir;
    private int count;
    private final int size;
    Random rn;

    public ReservoirSample(long ram, int numAttributes, int[] parameters, int repetition) {
        setting = "ReservoirSample";
        this.size = parameters[0]; // denotes the size of the reservoir
        this.parameters = parameters;
        this.ram = ram;
        this.reservoir = new long[size][numAttributes];
        this.count = 0;
        this.rn = new Random(repetition);
        //this.memUsageSynopsis = (long) size * numAttributes * 32;
    }

    @Override
    public void add(Record record) {

    }

    @Override
    public void add(long[] record) {
        long[] attr;
        if (count < size) {
//            attr = new long[record.length - 1];
//            System.arraycopy(record, 1, attr, 0, record.length - 1); // ignore RID
//            reservoir[count] = attr;
            System.arraycopy(record, 1, reservoir[count], 0, record.length - 1);

        } else {
            int replace = rn.nextInt(count + 1);
            if (replace < size) {
                System.arraycopy(record, 1, reservoir[replace], 0, record.length - 1);  // Reuse existing row
            }
        }
        count++;
    }

    @Override
    public void ingest(Record record, int i) {

    }

    @Override
    public void delete(long[] r) {
        long[] attr;
        if (count < size) {
            attr = new long[r.length - 1];
            System.arraycopy(r, 1, attr, 0, r.length - 1); // ignore RID
            for (int i = 0; i < count; i++) {
                // go over all attributes and see if match
                if (delete(r, attr, i)) break;
            }
        } else {
            int replace = rn.nextInt(count + 1);
            if (replace < size) {
                attr = new long[r.length - 1];
                System.arraycopy(r, 1, attr, 0, r.length - 1); // ignore RID
                for (int i = 0; i < reservoir.length; i++) {
                    // go over all attributes and see if match
                    if (delete(r, attr, i)) break;
                }
            }
        }
        count--;
    }

    @Override
    public void delete(Record r) {

    }

    private boolean delete(long[] r, long[] attr, int i) {
        boolean match = true;
        for (int j = 0; j < reservoir[i].length; j++) {
            if (reservoir[i][j] != attr[j]) {
                match = false;
                break;
            }
        }
        if (match) {
            Arrays.fill(reservoir[i], Long.MIN_VALUE);  // Or any "tombstone" value you recognize
            return true;
        }

        return false;
    }


    @Override
    public int query(long[] query, int numPreds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int query(Record query, int numPreds, QueryInfo queryInfo) {
        return 0;
    }

    @Override
    public int query(long[] query, int numPreds, QueryInfo queryInfo) {
       // go over all records in the reservoir and check if they match the query
        int queryResultCount = 0;
        for (int i = 0; i < size; i++) {
            boolean match = true;
            for (int j = 0; j < query.length; j++) {
                if (query[j] != -1 && query[j] != reservoir[i][j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                queryResultCount++;
            }
        }
        // scale result by the size of the reservoir
        return (int) (queryResultCount * (Math.max((double) count /size, 1)));

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
        reservoir = new long[size][reservoir[0].length];
    }


    @Override
    public int getFilledKSamples() {
        return 0;
    }

    public void printParams() {
        System.out.println("Reservoir Sample with size: " + size);

    }
    public long getMemoryUsage() {
        return ((long) size * reservoir[0].length * 255);
    }
}
