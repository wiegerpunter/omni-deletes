package omni.omniTwoLHS;

import omni.AnalysisBaselinesRefactor;
import omni.Main;
import omni.SynopsisRefactor;

import java.util.Random;

public class ReservoirSample extends SynopsisRefactor {
    private long[][] reservoir;
    private int count;
    private final int size;
    Random rn;

    public ReservoirSample(long ram, int numAttributes, int[] parameters, int repetition) {
        setting = "ReservoirSample";
        System.out.println("numAttributes: " + numAttributes);
        this.size = parameters[0];
        this.parameters = parameters;
        this.ram = ram;
        this.reservoir = new long[size][numAttributes];
        this.count = 0;
        this.rn = new Random(repetition);
        this.memUsageSynopsis = (long) size * numAttributes * 32;

    }

    @Override
    public void add(long[] record) {
        long[] attr = new long[record.length - 1];
        for (int i = 1; i < record.length; i++) {
            attr[i - 1] = record[i];
        }
        if (count < size) {
            long[] newAttr = new long[attr.length - 1];
            System.arraycopy(attr, 1, newAttr, 0, attr.length - 1);
            if (Main.countUniqueSamples) {
                Main.uniqueSamplesReservoir.add(newAttr);
            }
            reservoir[count] = attr;
        } else {
            int replace = rn.nextInt(count + 1);
            if (replace < size) {
                if (Main.countUniqueSamples) {
                    Main.uniqueSamplesReservoir.remove(reservoir[replace]);
                    Main.uniqueSamplesReservoir.add(attr);
                }
                reservoir[replace] = attr;
            }
        }
        count++;

    }

    @Override
    public int query(long[] query, int numPreds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int query(long[] query, int numPreds, AnalysisBaselinesRefactor.QueryInfo queryInfo) {
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
    public int query(long[] query, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
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
    public void delete(long[] r) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFilledKSamples() {
        return 0;
    }

    public void printParams() {
        System.out.println("Reservoir Sample with size: " + size);

    }
}
