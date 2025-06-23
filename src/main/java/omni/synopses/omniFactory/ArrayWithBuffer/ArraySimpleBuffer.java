package omni.synopses.omniFactory.ArrayWithBuffer;

import java.util.Arrays;

public class ArraySimpleBuffer {
    private final int K;
    private final int[] arr;
    private final int[] buffer;

    private int curSampleSize = 0;
    private int bufferSize = 0;
    private int deletesFromSample = 0;
    private int deletesFromBuffer = 0;

    public ArraySimpleBuffer(int budget) {
        int bufferBudget = Math.max(1, budget / 80);
        this.K = budget - bufferBudget;
        this.arr = new int[K];
        this.buffer = new int[bufferBudget];
    }

    public void insert(int val) {
        if (curSampleSize < K) {
            arr[curSampleSize++] = val;
            if (curSampleSize == K) {
                Arrays.sort(arr, 0, K);
            }
        } else {
            buffer[bufferSize++] = val;
            if (bufferSize == buffer.length) {
                flushBuffer();
            }
        }
    }

    public void remove(int val) {
        // Try to remove from arr
        int i = Arrays.binarySearch(arr, 0, curSampleSize, val);
        if (i >= 0) {
            System.arraycopy(arr, i + 1, arr, i, curSampleSize - i - 1);
            curSampleSize--;
            deletesFromSample++;
        } else {
            // Try to remove from buffer
            i = Arrays.binarySearch(buffer, 0, bufferSize, val);
            if (i >= 0) {
                System.arraycopy(buffer, i + 1, buffer, i, bufferSize - i - 1);
                bufferSize--;
                deletesFromBuffer++;
            }
        }
    }

    private void flushBuffer() {
        int total = curSampleSize + bufferSize;
        int[] merged = new int[total];
        System.arraycopy(arr, 0, merged, 0, curSampleSize);
        System.arraycopy(buffer, 0, merged, curSampleSize, bufferSize);
        Arrays.sort(merged, 0, total);

        // Keep only the smallest K
        System.arraycopy(merged, 0, arr, 0, K);
        curSampleSize = K;
        bufferSize = 0;
    }

    public int[] getK() {
        if (bufferSize > 0) flushBuffer();
        return Arrays.copyOf(arr, curSampleSize);
    }

    public int getCurSampleSize() {
        if (bufferSize > 0) flushBuffer();
        return curSampleSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getDeletesFromSample() {
        return deletesFromSample;
    }

    public int getDeletesFromBuffer() {
        return deletesFromBuffer;
    }

    public int getTotalDeletes() {
        return deletesFromSample + deletesFromBuffer;
    }
}
