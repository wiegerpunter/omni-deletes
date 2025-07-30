package omni.synopses.omniFactory.ArrayWithBuffer;

import java.util.Arrays;


public class ArrayWithBufferOptimizedBatchDeletes {
    private final int K;
    public int[] arr;
    public int[] ingestionBuffer;
    public int[] deletionBuffer;
    int curSampleSize = 0;
    int bufferSize = 0;
    int deletionBufferSize=0;
    int deletesFromSample = 0;
    int deletesFromBuffer = 0;
    private int curTreeRoot = Integer.MAX_VALUE;
    int deletionBufferOriginalSize=0;
    private int minRejectedValue = Integer.MAX_VALUE;
    public ArrayWithBufferOptimizedBatchDeletes(int budget, int ingestionBufferSize, int deleteBufferSize) {
        this.K = budget;
        this.arr = new int[this.K];
        Arrays.fill(this.arr, Integer.MAX_VALUE);
        this.ingestionBuffer = new int[ingestionBufferSize];
        this.deletionBufferOriginalSize = deleteBufferSize;
        this.deletionBuffer = new int[deletionBufferOriginalSize];
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
        if (val <= curTreeRoot || curSampleSize < K) {
            if (bufferSize == ingestionBuffer.length) {
                flushDeletes();
                mergeInPlaceOptimal();
                curSampleSize = Math.min(curSampleSize+bufferSize, K);
                bufferSize = 0;
                curTreeRoot = arr[curSampleSize-1];
                setSampleSizeBasedOnMinRejectedValue();
            }
            insertSorted(val);
        } else {
            if (val < minRejectedValue) {
                minRejectedValue = val; // Update the minimum rejected value
            }
        }
    }

    private void insertSorted(int val) {
        int pos = binarySearch(ingestionBuffer, 0, bufferSize, val);

        // Shift elements to make room for new value
        System.arraycopy(ingestionBuffer, pos, ingestionBuffer, pos + 1, bufferSize - pos);
        ingestionBuffer[pos] = val;
        bufferSize++;
    }

    public void mergeInPlaceOptimal() {
        // Process ingestionBuffer from largest to smallest
        int bufferElementsToProcess = bufferSize;
//        if (curSampleSize >= K && ingestionBuffer[bufferSize - 1] > arr[curSampleSize - 1]) {
//            System.out.println("Let's see if this breaks anything");
//        }
        int bufferPos = bufferElementsToProcess - 1;
        int lastShift = curSampleSize;
        while (bufferPos >= 0) {
            int val = ingestionBuffer[bufferPos];
            int insertPosition = binarySearch(arr, 0, lastShift, val); // returns the exact insert position, whatever follows needs to be shifted to the right
            if (insertPosition + bufferElementsToProcess > arr.length) { // I can add it but it will anyway be expired, so no need to add it
                bufferPos--;
                bufferElementsToProcess--;
                minRejectedValue = Math.min(minRejectedValue, val);
                continue;
            }
            // else i need to make space
            int elementsToShift = Math.min(lastShift - insertPosition, arr.length - insertPosition - bufferElementsToProcess);
            System.arraycopy(arr, insertPosition, arr, insertPosition + bufferElementsToProcess, elementsToShift);
            arr[insertPosition + bufferElementsToProcess - 1] = val;
            lastShift = insertPosition;
            bufferPos--;
            bufferElementsToProcess--;
        }
    }


    int binarySearch(int[] arr, int from, int to, int key) {
        int pos = binarySearch1(arr, from, to, key);
        if (pos > 0) {
            while (pos > 0 && arr[pos] == arr[pos - 1]) {
                pos--; // Find the first occurrence of the value
            }
        }
        if (pos < 0) {
            pos = -pos - 1; // If not found, convert to insertion point
        }
        return pos; // Return the position of the first occurrence or insertion point
    }

    final int binarySearch1(int[] arr, int from, int to, int key) {
        return Arrays.binarySearch(arr, from, to, key);
    }

    public final void flushDeletes() {
        if (deletionBufferSize>1)
            Arrays.sort(deletionBuffer, 0, deletionBufferSize);
        int startPosForBinSearch=0;

        int lastMaxPosition=Math.min(curSampleSize-1, K)+1;
        while (deletionBufferSize>0) {
            // Search in arr
            int hx = deletionBuffer[deletionBufferSize-1];
            if (hx > curTreeRoot) {
                // Element is larger than largest in sample, ignore
                minRejectedValue = Math.min(minRejectedValue, hx);
                deletionBufferSize--;
                continue;
            }
            int deleteIndex=0;

            deleteIndex = binarySearch1(arr, startPosForBinSearch, lastMaxPosition, hx);

            if (deleteIndex<0) {
                // Element not found in arr, do nothing
                deletionBufferSize--;
                lastMaxPosition = -deleteIndex-1;
                continue;
            } else
                lastMaxPosition = deleteIndex;

            int closestBufferIndex = findClosestInBuffer(hx);

            if (closestBufferIndex >= 0) {
                int bufferValue = ingestionBuffer[closestBufferIndex];
                int insertIndex;
                if (bufferValue < arr[deleteIndex]) {
                    insertIndex = Arrays.binarySearch(arr, 0, deleteIndex, bufferValue);
                    lastMaxPosition++;
                } else {
                    insertIndex = Arrays.binarySearch(arr, deleteIndex, Math.min(curSampleSize + 1, K), bufferValue);
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
//                arr[Math.min(curSampleSize - 1, K - 1)] = Integer.MAX_VALUE; // Set the last element to a large value
                curSampleSize--;
            }
            deletesFromSample++;

            // Update curTreeRoot
            if (curSampleSize > 0) {
                curTreeRoot = arr[Math.min(curSampleSize-1, K - 1)];
                setSampleSizeBasedOnMinRejectedValue();
            } else {
                curTreeRoot = Integer.MAX_VALUE;
            }
            deletionBufferSize--;
        }
        // Update curTreeRoot
        if (curSampleSize > 0) {
            curTreeRoot = arr[Math.min(curSampleSize-1, K - 1)];
            setSampleSizeBasedOnMinRejectedValue();
        } else {
            curTreeRoot = Integer.MAX_VALUE;
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

        // add in the delete buffer
        deletionBuffer[deletionBufferSize++]=hx;
        if (deletionBufferSize>=deletionBuffer.length || deletionBufferSize>=bufferSize) {
            // need to flush deletes
            flushDeletes();
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

    public void setSampleSizeBasedOnMinRejectedValue() {
        if (minRejectedValue == Integer.MAX_VALUE || minRejectedValue > curTreeRoot) {
            return; // No valid min rejected value, return current sample size
        }
        int index = Arrays.binarySearch(arr, 0, curSampleSize, minRejectedValue);
        if (index < 0) {
            index = -index - 1; // Convert to insertion point
        }
        curSampleSize = Math.min(index, K);
        curTreeRoot = minRejectedValue;
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
        if (deletionBufferSize>0) flushDeletes();
        if (bufferSize > 0) {
            mergeInPlaceOptimal();
            curSampleSize = Math.min(curSampleSize+bufferSize, K);
            bufferSize = 0;
            curTreeRoot = arr[curSampleSize-1];
            setSampleSizeBasedOnMinRejectedValue();
        }
    }
}

