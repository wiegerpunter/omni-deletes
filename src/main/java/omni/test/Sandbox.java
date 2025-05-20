package omni.test;// Executable to test omnisketch on a dataset


import java.util.Arrays;

public class Sandbox {
    int s;
    public Sandbox(int s) {
        this.s = s;
    }
    public void fromNumStoredAttrToSubPops(long[] record) {
        int storedAttrs = record.length - 1;

        for (int k = 1; k < Math.pow(2, storedAttrs); k++) {
            long[] recToAdd = new long[storedAttrs];
            // Convert k to binary string.
            StringBuilder binaryString = new StringBuilder(Integer.toBinaryString(k));
            // Pad with zeros.
            while (binaryString.length() < storedAttrs) {
                binaryString.insert(0, "0");
            }
            // Make query.
            for (int l = 0; l < storedAttrs; l++) {
                if (binaryString.charAt(l) == '1') {
                    recToAdd[l] = record[l + 1];
                } else {
                    recToAdd[l] = -1;
                }
            }
            // Check if query is already in list of queries.
            System.out.println(binaryString);
            System.out.println(Arrays.toString(recToAdd));
            System.out.println("-----");
        }
    }

    public void fromLongArrayToSubpop(long[] query, int numPreds) {
        // Convert query to binary string.
        StringBuilder binaryString = new StringBuilder();
        for (long l : query) {
            if (l == -1) {
                binaryString.append("0");
            } else {
                binaryString.append("1");
            }
        }
        System.out.println(binaryString);
    }


public static void main(String[] args) {
        Sandbox s = new Sandbox(2);
        s.fromNumStoredAttrToSubPops(new long[]{156, 1, 1, 1, 1});

        System.out.println("-- Query would be ---");

        s.fromLongArrayToSubpop(new long[]{-1, 1, -1, -1}, 1);
}
}
