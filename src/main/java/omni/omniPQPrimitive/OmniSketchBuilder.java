package omni.omniPQPrimitive;

public class OmniSketchBuilder {
    private int seed = -1;
    private long ram;
    private int dyadicRangeBits;
    private boolean useS0 = false;
    private boolean useS0WithSampling = false;
    private boolean useTwoLHS = false;
    private boolean useOnlyBestRow = false;
    private boolean useAcrossRows = false;
    private boolean rangeQueries = false;
    private boolean useBetaKmin = false;
    private boolean useFastTwoLHS = false;
    private boolean useInvDistPaper2LHS = false;
    private boolean useMinEstimate = false;
    private boolean checkExactUnion2LHS = false;
    private double BetaKmin = 1;
    private boolean dynamicResizing = false;
    private boolean dynamicSampleSizes = false;
    private boolean case1ReturnScap = false;
    private boolean useNmax = false;
    private int s0Setting = 0;
    private double eps = 0.1;
    private int sampleBufferSize = 0;
    private int numStoredAttributes;
    private int[] params;

    public OmniSketchBuilder setSeed(int seed) {
        this.seed = seed;
        return this;
    }

    public OmniSketchBuilder setParams(int[] params) {
        this.params = params;
        return this;
    }

    public OmniSketchBuilder setRam(long ram) {
        this.ram = ram;
        return this;
    }

    public OmniSketchBuilder setDyadicRangeBits(int dyadicRangeBits) {
        this.dyadicRangeBits = dyadicRangeBits;
        return this;
    }

    public OmniSketchBuilder setUseS0(boolean useS0) {
        this.useS0 = useS0;
        return this;
    }
    public OmniSketchBuilder setUseS0WithSampling(boolean useS0WithSampling) {
        this.useS0WithSampling = useS0WithSampling;
        return this;
    }
    public OmniSketchBuilder setUseTwoLHS(boolean useTwoLHS) {
        this.useTwoLHS = useTwoLHS;
        return this;
    }
    public OmniSketchBuilder setUseOnlyBestRow(boolean useOnlyBestRow) {
        this.useOnlyBestRow = useOnlyBestRow;
        return this;
    }
    public OmniSketchBuilder setUseAcrossRows(boolean useAcrossRows) {
        this.useAcrossRows = useAcrossRows;
        return this;
    }
    public OmniSketchBuilder setRangeQueries(boolean rangeQueries) {
        this.rangeQueries = rangeQueries;
        return this;
    }
    public OmniSketchBuilder setUseBetaKmin(boolean useBetaKmin) {
        this.useBetaKmin = useBetaKmin;
        return this;
    }
    public OmniSketchBuilder setUseFastTwoLHS(boolean useFastTwoLHS) {
        this.useFastTwoLHS = useFastTwoLHS;
        return this;
    }
    public OmniSketchBuilder setUseInvDistPaper2LHS(boolean useInvDistPaper2LHS) {
        this.useInvDistPaper2LHS = useInvDistPaper2LHS;
        return this;
    }
    public OmniSketchBuilder setUseMinEstimate(boolean useMinEstimate) {
        this.useMinEstimate = useMinEstimate;
        return this;
    }
    public OmniSketchBuilder setCheckExactUnion2LHS(boolean checkExactUnion2LHS) {
        this.checkExactUnion2LHS = checkExactUnion2LHS;
        return this;
    }
    public OmniSketchBuilder setBetaKmin(double betaKmin) {
        this.BetaKmin = betaKmin;
        return this;
    }
    public OmniSketchBuilder setDynamicResizing(boolean dynamicResizing) {
        this.dynamicResizing = dynamicResizing;
        return this;
    }
    public OmniSketchBuilder setDynamicSampleSizes(boolean dynamicSampleSizes) {
        this.dynamicSampleSizes = dynamicSampleSizes;
        return this;
    }
    public OmniSketchBuilder setCase1ReturnScap(boolean case1ReturnScap) {
        this.case1ReturnScap = case1ReturnScap;
        return this;
    }
    public OmniSketchBuilder setUseNmax(boolean useNmax) {
        this.useNmax = useNmax;
        return this;
    }
    public OmniSketchBuilder setS0Setting(int s0Setting) {
        this.s0Setting = s0Setting;
        return this;
    }
    public OmniSketchBuilder setEps(double eps) {
        this.eps = eps;
        return this;
    }
    public OmniSketchBuilder setSampleBufferSize(int sampleBufferSize) {
        this.sampleBufferSize = sampleBufferSize;
        return this;
    }

    public OmniSketchBuilder setNumStoredAttributes(int numStoredAttributes) {
        this.numStoredAttributes = numStoredAttributes;
        return this;
    }

    public OmniSketch build() {
        if (seed == -1) {
            throw new IllegalStateException("Seed must be set");
        }
        if (params == null) {
            throw new IllegalStateException("Params must be set");
        }

        return new OmniSketch(
                ram,
                numStoredAttributes,
                params,
                dyadicRangeBits,
                useS0,
                useS0WithSampling,
                useTwoLHS,
                useOnlyBestRow,
                useAcrossRows,
                rangeQueries,
                useBetaKmin,
                useFastTwoLHS,
                useInvDistPaper2LHS,
                useMinEstimate,
                checkExactUnion2LHS,
                BetaKmin,
                dynamicResizing,
                dynamicSampleSizes,
                case1ReturnScap,
                useNmax,
                s0Setting,
                eps,
                sampleBufferSize,
                seed
        );
    }

    // Example configurations

    public static OmniSketchBuilder sampleFirstQLater() {
        OmniSketchBuilder builder = new OmniSketchBuilder();
        return builder.setUseAcrossRows(true)
        .setUseS0(true)
        .setUseS0WithSampling(true)
        .setUseOnlyBestRow(true)
        .setUseBetaKmin(true)
        .setS0Setting(2);
    }

    public static OmniSketchBuilder QFirstSampleLater() {
        OmniSketchBuilder builder = new OmniSketchBuilder();
        return builder.setUseAcrossRows(true).setUseNmax(true);
    }

}