package omni.synopses.omniFactory.ArrayWithBuffer;

import java.util.Arrays;

import static java.lang.Math.abs;

public class ArrayWithBuffer {
    private final int K;
    public int[] arr;
    public int[] buffer;
    int size = 0;
    int bufferSize = 0;
    double beta;
    boolean useBeta = false;

    public ArrayWithBuffer(int budget) {
        int buffer = Math.max(1, budget / 80);
        this.K = budget - buffer;
        this.arr = new int[this.K];
        this.buffer = new int[buffer];
    }

    public ArrayWithBuffer(int budget, double beta) {
        int buffer = Math.max(1, budget / 80);
        this.K = budget - buffer;
        this.arr = new int[this.K];
        this.buffer = new int[buffer];
        this.beta = beta;
        this.useBeta = true;
    }

    public void insert(int val) {
        if (size < K - 1) {
            arr[size] = val;
            size++;
        } else if (size == K - 1) {
            // sort the array
            arr[size] = val;
            Arrays.sort(arr);
            size++;
        } else { // size == K
            if (val < arr[K-1]) {
                buffer[bufferSize] = val;
                bufferSize++;
                if (bufferSize == buffer.length) {
                    flushBuffer();
                }
            }
        }
    }

    void flushBuffer() {
        Arrays.sort(buffer, 0, bufferSize);
        int pointerArr = K - 1;
        int pointerBuffer = bufferSize - 1;

        for (int i = 0; i < bufferSize; i++) {
            if (arr[pointerArr] <= buffer[pointerBuffer]) {
                pointerBuffer--;
            } else {
                pointerArr--;
            }
        }

        int shiftRight = K - 1 - pointerArr;
        for (int i = pointerBuffer; i >= 0; i--) {
            int insertIndex=Arrays.binarySearch(arr, 0, pointerArr + 1, buffer[i]);
            if (insertIndex >= 0) {
                while (insertIndex > 0 && arr[insertIndex] == arr[insertIndex -1]) {
                    insertIndex--; // Find the first occurrence of the value
                }
            } else {
                insertIndex = -insertIndex - 1;
            }

            System.arraycopy(arr, insertIndex, arr, insertIndex + shiftRight, pointerArr - insertIndex + 1);

            arr[insertIndex + shiftRight - 1] = buffer[i];
            pointerArr = insertIndex - 1; // Update pointerArr to reflect the shift
            shiftRight -= 1;
        }
        bufferSize = 0;
    }

    public int[] getK() {
        if (bufferSize > 0) {
            flushBuffer();
        }

        if (useBeta) {
            return Arrays.copyOfRange(arr, 0, (int) (arr.length / beta));
        }
        return arr;
    }

    public int getSize() {
        return size;
    }
}

