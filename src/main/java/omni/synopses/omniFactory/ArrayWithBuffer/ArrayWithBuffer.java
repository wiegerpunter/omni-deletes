package omni.synopses.omniFactory.ArrayWithBuffer;

import java.util.Arrays;

import static java.lang.Math.abs;

public class ArrayWithBuffer {
    private final int K;
    public int[] arr;
    public int[] buffer;
    int curSampleSize = 0;
    int bufferSize = 0;
    int deletesFromSample = 0;
    int deletesFromBuffer = 0;
    private int curTreeRoot = Integer.MAX_VALUE;
    private boolean isSorted = false;

    public ArrayWithBuffer(int budget) {
        int buffer = Math.max(1, budget / 80);
        this.K = budget - buffer;
        this.arr = new int[this.K];
        this.buffer = new int[buffer];
    }

    public void remove(int hx) {
        if (curSampleSize == 0) {
            return; // Nothing to remove
        }

        if (hx > curTreeRoot) {
            // Element is larger than the largest in the sample, ignore
            return;
        }

        // check if present in buffer
        int bufferIndex = Arrays.binarySearch(buffer, 0, bufferSize, hx);
        if (bufferIndex >= 0) {
            // Element found in buffer, remove it
            System.arraycopy(buffer, bufferIndex + 1, buffer, bufferIndex, bufferSize - bufferIndex - 1);
            bufferSize--;
            deletesFromBuffer++;
            return;
        }
//
//        for (int i = 0; i < bufferSize; i++) {
//            if (buffer[i] == hx) {
//                // delete buffer[i]
//                System.arraycopy(buffer, i + 1, buffer, i, bufferSize - i - 1);
//                bufferSize--;
//                deletesFromBuffer++;
//                return;
//            }
//        }

        int index = Arrays.binarySearch(arr, 0, Math.min(curSampleSize, K), hx);
        if (index >= 0) {
            // Element found, shift elements to the left
            System.arraycopy(arr, index + 1, arr, index, Math.min(curSampleSize, K) - index -1);
            arr[Math.min(curSampleSize, K - 1)] = Integer.MAX_VALUE; // Set the last element to a large value
            curSampleSize--;
            deletesFromSample++;
        } else {
            // Element not found, do nothing
            return;
        }

        // If buffer is not empty, we can try to fill the gap
        if (bufferSize > 0) {
            int insertIndex = Arrays.binarySearch(arr, 0, Math.min(curSampleSize, K), buffer[0]);
            if (insertIndex < 0) {
                insertIndex = -insertIndex - 1; // Find the insertion point
            }
            // Shift elements to the right to make space for the buffer element
            System.arraycopy(arr, insertIndex, arr, insertIndex + 1, Math.min(curSampleSize, K) - insertIndex - 1);
            // Insert the last element from the buffer into the sample
            arr[insertIndex] = buffer[0];
            bufferSize--;
            curSampleSize++;
            // Shift the remaining buffer elements to the left
            System.arraycopy(buffer, 1, buffer, 0, bufferSize);
        }

        // Update the current tree root if necessary
        if (curSampleSize > 0) {
            curTreeRoot = arr[Math.min(curSampleSize, K-1)];
        } else {
            curTreeRoot = Integer.MAX_VALUE; // Reset if the sample is empty
        }
    }

    public void insert(int val) {
        if (curSampleSize < K) {
            arr[curSampleSize] = val;
            isSorted = false;
            curSampleSize++;
//            Arrays.sort(arr, 0, curSampleSize); // Sort the array after insertion
        } else {
            if (curSampleSize == K) {
                curSampleSize++;
                Arrays.sort(arr, 0, K); // Ensure the array is sorted before insertion
                isSorted = true;
                curTreeRoot = arr[K - 1]; // Update the tree root
            }
            tryInsert(val);
        }
    }

    private void tryInsert(int val) {
        if (val < curTreeRoot) { // get tree root
            if (bufferSize >= buffer.length) {
                flushBuffer();
            }
            buffer[bufferSize] = val;
            Arrays.sort(buffer, 0, bufferSize + 1); // Sort the buffer after insertion
            bufferSize++;
//                buffer[bufferSize] = val;
//                bufferSize++;
//                if (bufferSize == buffer.length) {
//                    flushBuffer();
//                }
        }
    }

    void flushBuffer() {
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
        curTreeRoot = arr[K - 1];
        bufferSize = 0;
    }

    public int[] getK() {
        if (bufferSize > 0) {
            flushBuffer();
        }
        return arr;
    }

    public int getCurSampleSize() {
        if (bufferSize > 0) {
            flushBuffer();
        }
        return Math.min(curSampleSize, K);
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

    public boolean isSorted(int[] array, int length) {
        for (int i = 1; i < length - 1; i++) {
            if (array[i] < array[i - 1]) return true;
        }
        return false;
    }

}

