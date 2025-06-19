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
    int deletesFromSample = 0;
    int deletesFromBuffer = 0;

    public ArrayWithBuffer(int budget) {
        int buffer = Math.max(1, budget / 80);
        this.K = budget - buffer;
        this.arr = new int[this.K];
        this.buffer = new int[buffer];
    }

    public ArrayWithBuffer(int budget, double beta) {
        budget = (int) (budget * beta);
        int bufferBudget = Math.max(1, budget / 80);
        this.K = budget - bufferBudget;
        this.arr = new int[this.K];
        this.buffer = new int[bufferBudget];
        this.beta = beta;
        this.useBeta = true;
    }


    public void remove(int hx) {
        if (size == 0) {
            return; // Nothing to remove
        }

        if (hx > arr[K - 1]) {
            // Element is larger than the largest in the sample, ignore
            return;
        }

        // check if present in buffer
        for (int i = 0; i < bufferSize; i++) {
            if (buffer[i] == hx) {
                // delete buffer[i]
                System.arraycopy(buffer, i + 1, buffer, i, bufferSize - i - 1);
                bufferSize--;
                deletesFromBuffer++;
                return;
            }
        }


        int index = Arrays.binarySearch(arr, 0, size, hx);
        if (index >= 0) {
            // Element found, shift elements to the left
            System.arraycopy(arr, index + 1, arr, index, size - index -1);
            size--;
            deletesFromSample++;
        } else {
            // Element not found, do nothing
            return;
        }

        // If buffer is not empty, we can try to fill the gap
        if (bufferSize > 0) {
            int insertIndex = Arrays.binarySearch(arr, 0, size, buffer[bufferSize - 1]);
            if (insertIndex < 0) {
                insertIndex = -insertIndex - 1; // Find the insertion point
            }
            // Shift elements to the right to make space for the buffer element
            System.arraycopy(arr, insertIndex, arr, insertIndex + 1, size - insertIndex);
            // Insert the last element from the buffer into the sample
            arr[size] = buffer[bufferSize - 1];
            bufferSize--;
            size++;
        }
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
        } else if (val < arr[K-1]) {
                if (bufferSize >= buffer.length) {
                    flushBuffer();
                }
                buffer[bufferSize++] = val;
//                buffer[bufferSize] = val;
//                bufferSize++;
//                if (bufferSize == buffer.length) {
//                    flushBuffer();
//                }
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
            return Arrays.copyOfRange(arr, 0, (int) (size / beta));
        }
        return arr;
    }

    public int getSize() {
        if (bufferSize > 0) {
            flushBuffer();
        }
        if (useBeta) {
            return (int) (size / beta);
        }
        return size;
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

