package omni.synopses.omniFactory;

public class OmniSketchBuilder {
    private String sketchType;
    private final OmniSketchConfig config = new OmniSketchConfig();

    public OmniSketchBuilder setSeed(int seed) {
        config.setSeed(seed);
        return this;
    }

    public OmniSketchBuilder setParams(int[] params) {
        config.setParams(params);
        return this;
    }

    public OmniSketchBuilder setRam(long ram) {
        config.setRam(ram);
        return this;
    }

    public OmniSketchBuilder setDyadicRangeBits(int dyadicRangeBits) {
        config.setDyadicRangeBits(dyadicRangeBits);
        return this;
    }

    public OmniSketchBuilder setUseS0(boolean useS0) {
        config.setUseS0(useS0);
        return this;
    }

    public OmniSketchBuilder setUseS0WithSampling(boolean useS0WithSampling) {
        config.setUseS0WithSampling(useS0WithSampling);
        return this;
    }

    public OmniSketchBuilder setUseTwoLHS(boolean useTwoLHS) {
        config.setUseTwoLHS(useTwoLHS);
        return this;
    }

    public OmniSketchBuilder setUseOnlyBestRow(boolean useOnlyBestRow) {
        config.setUseOnlyBestRow(useOnlyBestRow);
        return this;
    }

    public OmniSketchBuilder setUseAcrossRows(boolean useAcrossRows) {
        config.setUseAcrossRows(useAcrossRows);
        return this;
    }

    public OmniSketchBuilder setRangeQueries(boolean rangeQueries) {
        config.setRangeQueries(rangeQueries);
        return this;
    }

    public OmniSketchBuilder setDynamicResizing(boolean dynamicResizing) {
        config.setDynamicResizing(dynamicResizing);
        return this;
    }

    public OmniSketchBuilder setDynamicSampleSizes(boolean dynamicSampleSizes) {
        config.setDynamicSampleSizes(dynamicSampleSizes);
        return this;
    }

    public OmniSketchBuilder setEps(double eps) {
        config.setEps(eps);
        return this;
    }

    public OmniSketchBuilder setSampleBufferSize(int sampleBufferSize) {
        config.setSampleBufferSize(sampleBufferSize);
        return this;
    }

    public OmniSketchBuilder setNumStoredAttributes(int numStoredAttributes) {
        config.setNumStoredAttributes(numStoredAttributes);
        return this;
    }

    public OmniSketchBuilder setUseFastTWOLHS(boolean useFastTWOLHS) {
        config.setUseFastTWOLHS(useFastTWOLHS);
        return this;
    }

    public OmniSketchBuilder setSketchType(String sketchType) {
        this.sketchType = sketchType;
        return this;
    }

    public OmniSketchBuilder setUseNMax(boolean useNMax) {
        config.setUseNMax(useNMax);
        return this;
    }

    public OmniSketchBuilder setBufferBeta(double bufferBeta) {
        config.setBufferBeta(bufferBeta);
        return this;
    }

    public OmniSketch build() {
        return new OmniSketch(config, sketchType);
    }

    public static OmniSketchBuilder sampleFirstQLater() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseS0(true)
                .setUseS0WithSampling(true)
                .setUseOnlyBestRow(true)
                .setSketchType("SampleFirst");
    }

    public static OmniSketchBuilder sampleFirstQLaterPerRow() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(false)
                .setUseS0(true)
                .setUseS0WithSampling(true)
                .setUseOnlyBestRow(true)
                .setSketchType("SampleFirstPerRow");
    }

    public static OmniSketchBuilder sampleFirstQLaterTest() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseS0(true)
                .setUseS0WithSampling(true)
                .setUseOnlyBestRow(true)
                .setSketchType("SampleFirstTest");
    }

    public static OmniSketchBuilder QFirstSampleLater() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseNMax(true)
                .setSketchType("SampleLater");
    }

    public static OmniSketchBuilder QFirstSampleLaterArray() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseNMax(true)
                .setSketchType("SampleLaterArray");
    }


    public static OmniSketchBuilder QFirstSampleLaterArrayWithBuffer() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseNMax(true)
                .setSketchType("SampleLaterArrayWithBuffer");
    }

    public static OmniSketchBuilder QFirstSampleLaterTreeSet() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseNMax(true)
                .setSketchType("SampleLaterTreeSet");
    }

    public static OmniSketchBuilder QFirstSampleLaterTreeSetWithoutBuffer() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseNMax(true)
                .setSketchType("SampleLaterTreeSetWithoutBuffer");
    }

    public static OmniSketchBuilder QFirstSampleLaterArrayWithoutBuffer() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseNMax(true)
                .setSketchType("SampleLaterArrayWithoutBuffer");
    }


    public static OmniSketchBuilder TWOLHS() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseNMax(true)
                .setUseFastTWOLHS(false)
                .setSketchType("TWOLHS");
    }

    public static OmniSketchBuilder FastTWOLHS() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseNMax(true)
                .setUseFastTWOLHS(true)
                .setSketchType("TWOLHS");
    }

    public static OmniSketchBuilder QFirstSampleLaterOptimized() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseNMax(true)
                .setSketchType("SampleLaterOptimized");
    }

    public static OmniSketchBuilder QFirstSampleLaterOptimizedOnlyNew() {
        return new OmniSketchBuilder()
                .setUseAcrossRows(true)
                .setUseNMax(true)
                .setSketchType("SampleLaterOptimizedOnlyNew");
    }

}