package omni.synopses.omniFactory.CustomPriorityQueue;

// Java code to implement
// priority-queue using
// array implementation of
// binary heap

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class PriorityQueue {

    int[] H;
    public int size = -1;
    public int K;

    public PriorityQueue(int size) {
        this.K = size + 1;
        H = new int[size + 1]; // sample size + 1 buffer
        for (int i = 0; i < size; i++) {
            H[i] = -1;
        }
    }

    public int size() {
        return size;
    }

    public PriorityQueue(PriorityQueue pq) {
        this.size = pq.size;
        this.K = pq.size;
        H = new int[pq.size];
        // we know that arr is already coming from a priority queue, leverage that
        System.arraycopy(pq.H, 0, H, 0, pq.size);
//        H = Arrays.copyOf(pq.H, pq.size);
        //shiftUp(this.size);
//        this.removeBufferSize = pq.removeBufferSize;
//        this.toRemove = new ArrayList<>(pq.toRemove);

    }

    public PriorityQueue(TreeSet<Long> s0Bucket) {
        this.K = s0Bucket.size();
        H = new int[s0Bucket.size()];
        Arrays.fill(H, -1);

        for (long l : s0Bucket) {
            this.insert((int) l);
        }

//
//        H = new int[K];
//        int i = 0;
//        for (long l : s0Bucket) {
//            H[i] = (int) l;
//            i++;
//        }
//        this.size = K - 1;
//        shiftUp(this.size);
//    }
    }

//    public PriorityQueue query() {
//        if (!toRemove.isEmpty()) {
//            removeBatch(toRemove);
//            toRemove.clear();
//        }
//        return this;
//    }

//    public PriorityQueue(PriorityQueue priorityQueue) {
//        this.H = priorityQueue.H;
//        this.size = priorityQueue.size;
//        this.K = priorityQueue.K;
//    }

    // Function to return the index of the
    // parent node of a given node
    static int parent(int i) {
        return (i - 1) / 2;
    }

    public static PriorityQueue getCopy(PriorityQueue priorityQueue) {
        PriorityQueue copy = new PriorityQueue(priorityQueue.K);
        copy.size = priorityQueue.size;
        if (priorityQueue.size >= 0) System.arraycopy(priorityQueue.H, 0, copy.H, 0, priorityQueue.size);
        return copy;
    }

    // Function to return the index of the
    // left child of the given node
    static int leftChild(int i) {
        return ((2 * i) + 1);
    }

    // Function to return the index of the
    // right child of the given node
    static int rightChild(int i) {
        return ((2 * i) + 2);
    }

    // Function to shift up the
    // node in order to maintain
    // the heap property
    void shiftUp(int i) {
        while (i > 0 &&
                this.H[parent(i)] < this.H[i]) {
            // Swap parent and current node
            swap(parent(i), i);

            // Update i to parent of i
            i = parent(i);
        }
    }

    // Function to shift down the node in
    // order to maintain the heap property
    void shiftDown(int i) {
        int maxIndex = i;

        // Left Child
        int l = leftChild(i);

        if (l <= size &&
                H[l] > H[maxIndex]) {
            maxIndex = l;
        }

        // Right Child
        int r = rightChild(i);

        if (r <= size &&
                H[r] > H[maxIndex]) {
            maxIndex = r;
        }

        // If i not same as maxIndex
        if (i != maxIndex) {
            swap(i, maxIndex);
            shiftDown(maxIndex);
        }
    }

    // Function to insert a
    // new element in
    // the Binary Heap
    void insert(int p) {
        size = size + 1;
        if (size >= K) {
            // We want to grow the queue, but we are out of space
            // new size is 2x the current size
            int[] newH = new int[K * 2];
            K = K * 2;
            System.arraycopy(H, 0, newH, 0, H.length);
            H = newH;
//
//            throw new RuntimeException("Size exceeded");
        }
        H[size] = p;

        // Shift Up to maintain
        // heap property
        shiftUp(size);
    }

    // Function to extract
    // the element with
    // maximum priority
    int extractMax() {
        int result = H[0];

        // Replace the value
        // at the root with
        // the last leaf
        H[0] = H[size];
        size = size - 1;
//        H[size + 1] = -1;

        // Shift down the replaced
        // element to maintain the
        // heap property
        shiftDown(0);
        return result;
    }

    // Function to change the priority
// of an element
    void changePriority(int i,
                               int p) {
        int oldp = H[i];
        H[i] = p;

        if (p > oldp) {
            shiftUp(i);
        } else {
            shiftDown(i);
        }
    }

    // Function to get value of
// the current maximum element
    int getMax() {
        return H[0];
    }

    // Function to remove the element
// located at given index
//    int removeBufferSize = 1000;
    public void removeAtIndex(int i) {
        H[i] = getMax() + 1;
//
        //        // Shift the node to the root
        //        // of the heap
        shiftUp(i);
        //
        //        // Extract the node
        extractMax();
    }

    public void remove(int value) {
        int i = getIndexOf(value);
        if (i == -1) {
            return;
        }
        removeAtIndex(i);
    }

    private int getIndexOf(int value) {
        for (int i = 0; i < size; i++) {
            if (H[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public void removeBatch(ArrayList<Integer> removeList) {
        // newIndex will track the next position for a kept element.
        int newIndex = 0;

        // Iterate over all elements (from 0 to size, inclusive, because size is the last index).
        for (int i = 0; i <= size; i++) {
            // If the element should NOT be removed, copy it to the new position.
            if (!removeList.contains(H[i])) {
                H[newIndex++] = H[i];
            }
        }

        // Update size: note that if newIndex==0, then no element remains, so size should be -1.
        size = newIndex - 1;

        // Rebuild the heap in one pass.
        // Start at the last parent and shift down every node.
        for (int i = parent(size); i >= 0; i--) {
            shiftDown(i);
        }
    }

    void swap(int i, int j) {
        int temp = H[i];
        H[i] = H[j];
        H[j] = temp;
    }

    public boolean isEmpty() {
        // check if the priority queue is empty
        return size == -1;
    }

    public int peek() {
        // return the element with the highest priority
        return getMax();
    }

    public int poll() {
        // remove the element with the highest priority
        return extractMax();
    }

    public boolean add(int hx) {
        // add an element to the priority queue
        insert(hx);
        return true;
    }

    public void clear() {
        // clear the priority queue
        size = -1;
        Arrays.fill(H, -1);
    }

}
