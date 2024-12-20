package omni.omniReservoir;

import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;

public class queryRes{
    public int numPreds = 1;
    public int[] n;
    public IntHeapPriorityQueue[] sketch;
    public int[] curSampleSizes;
    private int nmax;
    private int usedSampleSize;

    public queryRes(IntHeapPriorityQueue[] result, int[] n, int[] curSampleSizes) {
        this.sketch = result;
        this.n = n;
        this.curSampleSizes = curSampleSizes;
    }

    public queryRes(int size) {
        this.sketch = new IntHeapPriorityQueue[size];
        this.n = new int[size];
        this.curSampleSizes = new int[size];
    }


    public void compNmax() {
        nmax = 0;
        usedSampleSize = 0;
        for (int i = 0; i < n.length; i++) {
            if (nmax < n[i]) {
                nmax = n[i];
                usedSampleSize = curSampleSizes[i];
            }
        }
    }
    public int getNmax() {
        return nmax;
    }
    public int getUsedSampleSize() {
        return usedSampleSize;
    }

    public int getNumPreds() {
        return numPreds;
    }

    public void setNumPreds(int numPreds) {
        this.numPreds = numPreds;
    }
}
