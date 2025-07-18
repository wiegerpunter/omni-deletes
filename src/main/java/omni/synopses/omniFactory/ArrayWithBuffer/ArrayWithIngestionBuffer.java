package omni.synopses.omniFactory.ArrayWithBuffer;

import java.util.Arrays;

public class ArrayWithIngestionBuffer {
    private final int K;
    public int[] arr;
    public int[] ingestionBuffer;
    int curSampleSize = 0;
    int bufferSize = 0;
    int deletesFromSample = 0;
    int deletesFromBuffer = 0;
    private int curTreeRoot = Integer.MAX_VALUE;

    private int minRejectedValue = Integer.MAX_VALUE;
    private boolean isSorted = false;
    public ArrayWithIngestionBuffer(int budget, int ingestionBufferSize) {
        this.K = budget;
        this.arr = new int[this.K];
        Arrays.fill(this.arr, Integer.MAX_VALUE);
        this.ingestionBuffer = new int[ingestionBufferSize];
    }



    private void insertFromBufferToDeleteSlot(int bufferIndex, int deleteIndex, int insertIndex) {

        if (insertIndex < deleteIndex) {
            // need to shift elements between arrIndexCloseToDelete and index, and then insert at arrIndexCloseToDelete
            System.arraycopy(arr, insertIndex, arr, insertIndex + 1, deleteIndex - insertIndex);
            arr[insertIndex] = ingestionBuffer[bufferIndex];
        } else {
            System.arraycopy(arr, deleteIndex + 1, arr, deleteIndex, insertIndex - deleteIndex);
            if (insertIndex == arr.length - 1) {
                arr[insertIndex] = ingestionBuffer[bufferIndex];
            } else {
                arr[insertIndex - 1] = ingestionBuffer[bufferIndex];
            }
        }

        if (bufferSize != bufferIndex) {
            System.arraycopy(ingestionBuffer, bufferIndex + 1, ingestionBuffer, bufferIndex, bufferSize - bufferIndex - 1);
        }
        bufferSize--;
    }

    public void insert(int val) {
        if (curSampleSize < K) {
            if (bufferSize >= ingestionBuffer.length) {
                flushBufferSampleNotFull(); // Flush the buffer if it exceeds its size
            }
            insertSorted(val, bufferSize);
//            curSampleSize++;
            if (curSampleSize == K) {
                flushBuffer();
                curTreeRoot = arr[K - 1]; // Array is now fully populated and sorted
            }
        } else {
            tryInsert(val);
        }
    }

    private void insertSorted(int val, int length) {
        int pos = Arrays.binarySearch(ingestionBuffer, 0, length, val);
        if (pos < 0) {
            pos = -pos - 1; // Find correct insertion point
        }

        // Shift elements to make room for new value
        System.arraycopy(ingestionBuffer, pos, ingestionBuffer, pos + 1, length - pos);
        ingestionBuffer[pos] = val;
        bufferSize++;
    }

    private void tryInsert(int val) {
        if (val < curTreeRoot) { // get tree root
            if (bufferSize >= ingestionBuffer.length) {
                flushBuffer();
            }
            insertSorted(val, bufferSize);
        } else {
            if (val < minRejectedValue) {
                minRejectedValue = val; // Update the minimum rejected value
            }
        }
    }

    void flushBufferSampleNotFull() {
        int i = 0;
        while (i < bufferSize) { // todo: make sure to include the i in the shifting st we only have to shift each number ONCE.
            if (curSampleSize < K) {
                int insertIndex = Arrays.binarySearch(arr, 0, curSampleSize, ingestionBuffer[i]);
                if (insertIndex >= 0) {
                    while (insertIndex > 0 && arr[insertIndex] == arr[insertIndex - 1]) {
                        insertIndex--;
                    }
                } else {
                    insertIndex = -insertIndex - 1;
                }
                System.arraycopy(arr, insertIndex, arr, insertIndex + 1, curSampleSize - insertIndex);
                arr[insertIndex] = ingestionBuffer[i];
                curSampleSize++;
                i++;
            } else {
                break; // array is full. Start sampling.
            }
        }

        // remove i items from buffer;
        System.arraycopy(ingestionBuffer, i , ingestionBuffer, 0, bufferSize - i);
        bufferSize -= i;

        // if we have buffer left, sample
        flushBuffer();

    }

    void flushBuffer() {
        int pointerArr = K - 1;
        int pointerBuffer = bufferSize - 1;
        for (int i = 0; i < bufferSize; i++) {
            if (arr[pointerArr] <= ingestionBuffer[pointerBuffer]) {
                if (ingestionBuffer[pointerBuffer] < minRejectedValue) {
                    minRejectedValue = ingestionBuffer[pointerBuffer]; // Update the minimum rejected value
                }
                pointerBuffer--;

            } else {
                pointerArr--;
            }
        }

        int shiftRight = K - 1 - pointerArr;
        for (int i = pointerBuffer; i >= 0; i--) {
            int insertIndex=Arrays.binarySearch(arr, 0, pointerArr + 1, ingestionBuffer[i]);
            if (insertIndex >= 0) {
                while (insertIndex > 0 && arr[insertIndex] == arr[insertIndex -1]) {
                    insertIndex--; // Find the first occurrence of the value
                }
            } else {
                insertIndex = -insertIndex - 1;
            }

            System.arraycopy(arr, insertIndex, arr, insertIndex + shiftRight, pointerArr - insertIndex + 1);

            arr[insertIndex + shiftRight - 1] = ingestionBuffer[i];
            pointerArr = insertIndex - 1; // Update pointerArr to reflect the shift
            shiftRight -= 1;
        }
        curTreeRoot = arr[K- 1];
        bufferSize = 0;
    }

    public void removeAlt(int hx) {
        if (curSampleSize == 0) {
            return; // Nothing to remove
        }

        if (hx > curTreeRoot) {
            // Element is larger than the largest in the sample, ignore
            return;
        }

        for (int i = 0; i < bufferSize; i++) {
            if (ingestionBuffer[i] == hx) {
                // delete buffer[i]
                System.arraycopy(ingestionBuffer, i + 1,ingestionBuffer, i, bufferSize - i - 1);
                bufferSize--;
                deletesFromBuffer++;
                return;
            }
        }

        if (!isSorted) {
            Arrays.sort(arr, 0, Math.min(curSampleSize + 1, K)); // Sort the array if not sorted
            isSorted = true;
        }

        int index = Arrays.binarySearch(arr, 0, Math.min(curSampleSize + 1, K), hx);
        if (index >= 0) {
            // Element found, shift elements to the left
            System.arraycopy(arr, index + 1, arr, index, Math.min(curSampleSize + 1, K) - index -1);
            arr[Math.min(curSampleSize, K - 1)] = Integer.MAX_VALUE; // Set the last element to a large value
            curSampleSize--;
            deletesFromSample++;
        } else {
            // Element not found in neither buffer or sample, do nothing
            return;
        }

        // If buffer is not empty, we can try to fill the gap
        if (bufferSize > 0) {
            int insertIndex = Arrays.binarySearch(arr, 0, Math.min(curSampleSize + 1, K), ingestionBuffer[0]);
            if (insertIndex >= 0) {
                while (insertIndex > 0 && arr[insertIndex] == arr[insertIndex -1]) {
                    insertIndex--; // Find the first occurrence of the value
                }
            } else {
                insertIndex = -insertIndex - 1;
            }

            // Shift elements to the right to make space for the buffer element
            System.arraycopy(arr, insertIndex, arr, insertIndex + 1, Math.min(curSampleSize + 1, K) - insertIndex - 1);
            // Insert the last element from the buffer into the sample
            arr[insertIndex] = ingestionBuffer[0];
            bufferSize--;
            curSampleSize++;
            // Shift the remaining buffer elements to the left
            System.arraycopy(ingestionBuffer, 1, ingestionBuffer, 0, bufferSize);
        }

        // Update the current tree root if necessary
        if (curSampleSize > 0) {
            curTreeRoot = arr[Math.min(curSampleSize, K-1)];
        } else {
            curTreeRoot = Integer.MAX_VALUE; // Reset if the sample is empty
        }
    }

    public void removeSimple(int hx) {
        if (curSampleSize == 0) {
            return; // Nothing to remove
        }

        if (hx > curTreeRoot) {
            return; // Element is larger than largest in sample, ignore
        }

        // Binary search in sorted buffer
        int bufferIndex = Arrays.binarySearch(ingestionBuffer, 0, bufferSize, hx);
        if (bufferIndex >= 0) {
            // Found in buffer, remove it
            System.arraycopy(ingestionBuffer, bufferIndex + 1, ingestionBuffer, bufferIndex, bufferSize - bufferIndex - 1);
            bufferSize--;
            deletesFromBuffer++;
            return;
        }

        // Search in arr
        int deleteIndex = Arrays.binarySearch(arr, 0, Math.min(curSampleSize + 1, K), hx);
        if (deleteIndex<0) {
            // Element not found in arr, do nothing
            return;
        }

        int closestBufferIndex = findClosestInBuffer(hx);

        if (closestBufferIndex >= 0) {
            int bufferValue = ingestionBuffer[closestBufferIndex];
            int insertIndex;
            if (bufferValue < arr[deleteIndex]) {
                insertIndex = Arrays.binarySearch(arr, 0, deleteIndex, bufferValue);
            } else {
                insertIndex = Arrays.binarySearch(arr, deleteIndex + 1, Math.min(curSampleSize + 1, K), bufferValue);
            }

            if (insertIndex < 0) {
                insertIndex = -insertIndex - 1;
            }
            insertIndex = Math.min(insertIndex, curSampleSize);

            if (insertIndex > deleteIndex) {
                int shiftLength = insertIndex - deleteIndex - 1;
                if (shiftLength > 0) {
                    System.arraycopy(arr, deleteIndex + 1, arr, deleteIndex, shiftLength);
                }
                arr[insertIndex - 1] = bufferValue;

            } else if (insertIndex < deleteIndex) {
                int shiftLength = deleteIndex - insertIndex;
                if (shiftLength > 0) {
                    // Shift elements to the right to make space for the buffer element
                    System.arraycopy(arr, insertIndex, arr, insertIndex + 1, shiftLength);
                }
                arr[insertIndex] = bufferValue;

            } else {
                // insertIndex == deleteIndex, directly replace
                arr[deleteIndex] = bufferValue;
            }

            // Remove buffer element
            System.arraycopy(ingestionBuffer, closestBufferIndex + 1, ingestionBuffer, closestBufferIndex, bufferSize - closestBufferIndex - 1);
            bufferSize--;
        } else {
            System.arraycopy(arr, deleteIndex + 1, arr, deleteIndex, Math.min(curSampleSize + 1, K) - deleteIndex -1);
            arr[Math.min(curSampleSize, K - 1)] = Integer.MAX_VALUE; // Set the last element to a large value
            curSampleSize--;
        }
        deletesFromSample++;

        // Update curTreeRoot
        if (curSampleSize > 0) {
            curTreeRoot = arr[Math.min(curSampleSize, K - 1)];
        } else {
            curTreeRoot = Integer.MAX_VALUE;
        }
    }

    private int findClosestInBuffer(int target) {
        if (bufferSize == 0) {
            return -1; // No elements in buffer
        }
        int low = 0, high = bufferSize - 1;

        // If target <= smallest
        if (target <= ingestionBuffer[low]) return low;

        // If target >= largest
        if (target >= ingestionBuffer[high]) return high;

        // Binary search to find closest
        while (low <= high) {
            int mid = (low + high) >>> 1;

            if (ingestionBuffer[mid] == target) {
                return mid;
            } else if (ingestionBuffer[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        // Now low is the insertion point
        // Compare neighbors to find closest
        if (low >= bufferSize) low = bufferSize - 1;
        if (low == 0) return 0;

        int before = ingestionBuffer[low - 1];
        int after = ingestionBuffer[low];

        return (Math.abs(before - target) <= Math.abs(after - target)) ? (low - 1) : low;
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
        int bufferIndex = Arrays.binarySearch(ingestionBuffer, 0, bufferSize, hx);
        if (bufferIndex >= 0) {
            // Element found in buffer, remove it
            System.arraycopy(ingestionBuffer, bufferIndex + 1, ingestionBuffer, bufferIndex, bufferSize - bufferIndex - 1);
            bufferSize--;
            deletesFromBuffer++;
            return;
        }

        int deleteIndex = Arrays.binarySearch(arr, 0, Math.min(curSampleSize + 1, K), hx);
        if (deleteIndex >= 0) {
            // check if buffer has element close to this rec:
            if (bufferSize > 0) {
                bufferIndex = Arrays.binarySearch(ingestionBuffer, 0, bufferSize, hx);
                if (bufferIndex >= 0) {
                    while (bufferIndex > 0 && ingestionBuffer[bufferIndex] == ingestionBuffer[bufferIndex - 1]) {
                        bufferIndex--; // Find the first occurrence of the value
                    }
                } else {
                        bufferIndex = -bufferIndex - 1;
                }
                if (bufferIndex >= bufferSize) {
                    bufferIndex = bufferSize - 1;
                }

                int insertIndex = getInsertIndex(bufferIndex);
                insertFromBufferToDeleteSlot(bufferIndex, deleteIndex, insertIndex);
            } else {
                System.arraycopy(arr, deleteIndex + 1, arr, deleteIndex, Math.min(curSampleSize + 1, K) - deleteIndex -1);
                arr[Math.min(curSampleSize, K - 1)] = Integer.MAX_VALUE; // Set the last element to a large value
                curSampleSize--;
                deletesFromSample++;
            }
        } else {
            // Element not found in neither buffer or sample, do nothing
            return;
        }


        // Update the current tree root if necessary
        if (curSampleSize > 0) {
            curTreeRoot = arr[Math.min(curSampleSize, K- 1)];
        } else {
            curTreeRoot = Integer.MAX_VALUE; // Reset if the sample is empty
        }
    }

    private int getInsertIndex(int bufferIndex) {
        int insertIndex = Arrays.binarySearch(arr, 0, Math.min(curSampleSize + 1, K), ingestionBuffer[bufferIndex]);
        if (insertIndex >= 0) {
            while (insertIndex > 0 && arr[insertIndex] == arr[insertIndex - 1]) {
                insertIndex--; // Find the first occurrence of the value
            }
        } else {
            insertIndex = -insertIndex - 1;
        }
        if (insertIndex >= Math.min(curSampleSize + 1, K) & Math.min(curSampleSize + 1, K) == 1) {
            insertIndex = 0;
        } else if (insertIndex >= Math.min(curSampleSize + 1, K)) {
            insertIndex = Math.min(curSampleSize + 1, K) - 1;
        }
        return insertIndex;
    }


    public int[] getK() {
        flushBeforeQuery();
        return arr;
    }

    public int getCurSampleSize() {
        flushBeforeQuery();
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

    public int getMinRejectedValue() {
        flushBeforeQuery();
        return minRejectedValue;
    }

    private void flushBeforeQuery() {
        if (bufferSize > 0) {
            if (curSampleSize < K) {
                flushBufferSampleNotFull();
            } else {
                flushBuffer();
            }
        }
    }

    private boolean isSorted (int[] array, int length) {
        for (int i = 1; i < length; i++) {
            if (array[i] < array[i - 1]) {
                return false; // Found an element that is smaller than the previous one
            }
        }
        return true; // The array is sorted
    }
}

