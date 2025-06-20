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
    private int curTreeRoot = Integer.MAX_VALUE;

    public ArraySimpleBuffer(int budget) {
        int buffer = Math.max(1, budget / 80);
        this.K = budget - buffer;
        this.arr = new int[this.K];
        this.buffer = new int[buffer];
    }

    public void insert(int val) {
        if (curSampleSize < K) {
            arr[curSampleSize++] = val;
            if (curSampleSize == K) {
                Arrays.sort(arr, 0, K);
                curTreeRoot = arr[K - 1];
            }
        } else if (val < curTreeRoot) {
            buffer[bufferSize++] = val;
            if (bufferSize == buffer.length) {
                flushBuffer();
            }
        }
    }
    public void remove(int hx) {
        if (curSampleSize == 0 || hx > curTreeRoot) return;

        int bufIndex = Arrays.binarySearch(buffer, 0, bufferSize, hx);
        if (bufIndex >= 0) {
            System.arraycopy(buffer, bufIndex + 1, buffer, bufIndex, bufferSize - bufIndex - 1);
            bufferSize--;
            deletesFromBuffer++;
        } else {
            int index = Arrays.binarySearch(arr, 0, curSampleSize, hx);
            if (index >= 0) {
                System.arraycopy(arr, index + 1, arr, index, curSampleSize - index - 1);
                arr[--curSampleSize] = Integer.MAX_VALUE;
                deletesFromSample++;
                updateTreeRoot();
            }
        }
        if (bufferSize > 0) {
            flushBuffer();
        }
    }

    private void flushBuffer() {
        Arrays.sort(buffer, 0, bufferSize);
        int ptrA = K - 1;
        int ptrB = bufferSize - 1;

        while (ptrB >= 0 && ptrA >= 0 && buffer[ptrB] < arr[ptrA]) {
            ptrA--;
            ptrB--;
        }

        int toReplace = bufferSize - ptrB - 1;
        if (toReplace <= 0) {
            bufferSize = 0;
            return;
        }

        for (int i = 0; i < toReplace; i++) {
            int bVal = buffer[i];
            int insertIndex = Arrays.binarySearch(arr, 0, K - toReplace + i, bVal);
            if (insertIndex < 0) insertIndex = -insertIndex - 1;

            System.arraycopy(arr, insertIndex, arr, insertIndex + 1, K - toReplace + i - insertIndex);
            arr[insertIndex] = bVal;
        }

        bufferSize = 0;
        updateTreeRoot();
    }

    private void updateTreeRoot() {
        curTreeRoot = (curSampleSize > 0) ? arr[Math.min(curSampleSize - 1, K - 1)] : Integer.MAX_VALUE;
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

    // Corrected sorting check
    public boolean isSorted(int[] array, int length) {
        for (int i = 1; i < length; i++) {
            if (array[i] < array[i - 1]) return false;
        }
        return true;
    }

}

