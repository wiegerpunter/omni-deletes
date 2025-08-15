# OmniSketch VLDBJ

Repository with code to run experiments in the VLDBJ version of OmniSketch: using deletes in the stream.

First, prepare the datasets, following the steps described in https://github.com/wiegerpunter/dataset-pipeline.git.

Second, make sure data is in input folder, and that output folder for results is created.

Experiments can be condigured in the json files under Configurations. Here, parameters such as the available RAM, dataset, number of predicates, etc can be set.
Also, this file contains booleans to express which settings should be run. To run aSH for instance, set expASH to true.
The OmniSketch setting used as S2 in the paper, so the final setting, can be used by setting expOmniSketchVLDBArraySampleLaterBatchDeletes.

If you have any questions, don't hesitate to ask.


