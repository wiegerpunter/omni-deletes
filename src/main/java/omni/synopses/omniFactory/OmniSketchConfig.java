package omni.synopses.omniFactory;

public class OmniSketchConfig {
    private int seed;
    private long ram;
    private int dyadicRangeBits;
    private boolean useS0;
    private boolean useS0WithSampling;
    private boolean useTwoLHS;
    private boolean useOnlyBestRow;
    private boolean useAcrossRows;
    private boolean rangeQueries;
    private boolean useBetaKmin;
    private boolean dynamicResizing;
    private boolean dynamicSampleSizes;
    private double eps;
    private int sampleBufferSize;
    private int numStoredAttributes;
    private int[] params;
    private boolean useNMax;
    private String setting;
    private boolean useFastTWOLHS;

    // Getters and setters for all fields
    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public long getRam() {
        return ram;
    }

    public void setRam(long ram) {
        this.ram = ram;
    }

    public int getDyadicRangeBits() {
        return dyadicRangeBits;
    }

    public void setDyadicRangeBits(int dyadicRangeBits) {
        this.dyadicRangeBits = dyadicRangeBits;
    }

    public boolean isUseS0() {
        return useS0;
    }

    public void setUseS0(boolean useS0) {
        this.useS0 = useS0;
    }

    public boolean isUseS0WithSampling() {
        return useS0WithSampling;
    }

    public void setUseS0WithSampling(boolean useS0WithSampling) {
        this.useS0WithSampling = useS0WithSampling;
    }

    public boolean isUseTwoLHS() {
        return useTwoLHS;
    }

    public void setUseTwoLHS(boolean useTwoLHS) {
        this.useTwoLHS = useTwoLHS;
    }

    public boolean isUseOnlyBestRow() {
        return useOnlyBestRow;
    }

    public void setUseOnlyBestRow(boolean useOnlyBestRow) {
        this.useOnlyBestRow = useOnlyBestRow;
    }

    public boolean isUseAcrossRows() {
        return useAcrossRows;
    }

    public void setUseAcrossRows(boolean useAcrossRows) {
        this.useAcrossRows = useAcrossRows;
    }

    public boolean isRangeQueries() {
        return rangeQueries;
    }

    public void setRangeQueries(boolean rangeQueries) {
        this.rangeQueries = rangeQueries;
    }

    public boolean isUseBetaKmin() {
        return useBetaKmin;
    }

    public void setUseBetaKmin(boolean useBetaKmin) {
        this.useBetaKmin = useBetaKmin;
    }

    public boolean isDynamicResizing() {
        return dynamicResizing;
    }

    public void setDynamicResizing(boolean dynamicResizing) {
        this.dynamicResizing = dynamicResizing;
    }

    public boolean isDynamicSampleSizes() {
        return dynamicSampleSizes;
    }

    public void setDynamicSampleSizes(boolean dynamicSampleSizes) {
        this.dynamicSampleSizes = dynamicSampleSizes;
    }

    public double getEps() {
        return eps;
    }

    public void setEps(double eps) {
        this.eps = eps;
    }

    public int getSampleBufferSize() {
        return sampleBufferSize;
    }

    public void setSampleBufferSize(int sampleBufferSize) {
        this.sampleBufferSize = sampleBufferSize;
    }

    public int getNumStoredAttributes() {
        return numStoredAttributes;
    }

    public void setNumStoredAttributes(int numStoredAttributes) {
        this.numStoredAttributes = numStoredAttributes;
    }

    public void setUseFastTWOLHS(boolean useFastTWOLHS) {
       this.useFastTWOLHS = useFastTWOLHS;
    }

    public int[] getParams() {
        return params;
    }

    public void setParams(int[] params) {
        this.params = params;
    }

    public void setUseNMax(boolean useNMax) {
        this.useNMax = useNMax;
    }

    public boolean getUseFastTWOLHS() {
        return useFastTWOLHS;
    }


    public String getSetting() {
        return setting;
    }
}