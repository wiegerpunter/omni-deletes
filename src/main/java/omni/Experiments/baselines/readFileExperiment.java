package omni.Experiments.baselines;

import omni.Config;
import omni.Experiments.Experiment;
import omni.Experiments.RunExperiments;
import omni.Experiments.parameterSetting.RamToPar;
import omni.synopses.baselines.aSH.aSH;
import omni.synopses.baselines.readFile.readFile;

import java.io.IOException;
import java.util.Arrays;

public class readFileExperiment implements Experiment {
    private RunExperiments runExperiments;
    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {

        readFile readFile = new readFile();
        runExperiments.runSynopsisRamBased(readFile, repetition);
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}
