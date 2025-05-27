package omni.Experiments;

public class ExpSetting {
    private int intersectionSize;
    private int numSets;
    private int[] setSizes;
    private int[] B;

    public ExpSetting(){
        intersectionSize = 0;
        numSets = 0;
        setSizes = new int[0];
        B = new int[0];
    };

    public ExpSetting(int intersectionSize, int[] B, int[] setSizes) {
        this.intersectionSize = intersectionSize;
        this.B = B;
        this.numSets = setSizes.length;
        this.setSizes = setSizes;
    }

    public int getIntersectionSize() {
        return intersectionSize;
    }
    public int getNumSets() {
        return numSets;
    }
    public int getSetSize(int index) {
        if (index < 0 || index >= numSets) {
            throw new IndexOutOfBoundsException("Invalid set index: " + index);
        }
        return setSizes[index];
    }

    public int[] getB() {
        return B;
    }
    public int[] getSetSizes() {
        return setSizes.clone(); // Return a copy to prevent external modification
    }

    public void addIntersectionSize(int exactAnswer) {
        if (exactAnswer < 0) {
            throw new IllegalArgumentException("Intersection size must be non-negative.");
        }
        this.intersectionSize = exactAnswer;
    }

    public void setNumSets(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Number of sets must be positive.");
        }
        this.numSets = length;
        this.setSizes = new int[length];
        this.B = new int[length];
    }

    public void setSetInfo(int setNumber, int setSize, int B) {
        if (setNumber < 0 || setNumber >= numSets) {
            throw new IndexOutOfBoundsException("Invalid set number: " + setNumber);
        }
        if (setSize < 0) {
            throw new IllegalArgumentException("Set size must be non-negative.");
        }
        if (setSize < intersectionSize) {
            throw new IllegalArgumentException("Set size " + setSize + " must be greater than or equal to intersection size "+ intersectionSize + ".");
        }

        this.setSizes[setNumber] = setSize;
        this.B[setNumber] = B;
    }

}

