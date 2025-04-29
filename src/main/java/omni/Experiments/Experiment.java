package omni.Experiments;

import omni.Config;
import omni.parameterSetting.RamToPar;

import java.io.IOException;

public interface Experiment {
    void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException;
    void setRunExperiments(RunExperiments runExperiments);
}
