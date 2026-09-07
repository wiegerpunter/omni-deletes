# OmniSketch VLDBJ


Repository with code to run experiments in the VLDBJ version of OmniSketch: using deletes in the stream.

# Step 1: Prepare the datasets
Three datasets are used throughout these experiments: CAIDA, TPC-DS and synthetic data with Zipf distributions.
The setup is described in detail in https://github.com/wiegerpunter/dataset-pipeline.git.

# Step 2: Set configurations
The configurations for the experiments can be set in the json files under Configurations. Here, parameters such as the available RAM, dataset, number of predicates, etc can be set.
Below, some important settings are highlighted.

# Step 3: Build
The project can be built using maven. From the root folder, run:
```mvn clean package
```
This will create a jar file named 'DSCM-5.0-SNAPSHOT.jar' in the target folder.
# Step 4: Run experiments
To run the experiments, use the following command:
```java -Xmx{RAM} -jar target/DSCM-5.0-SNAPSHOT.jar {configuration file}```

# Details on configurations
An example json configuration file is 'Configurations/StrictTurnstile/SynthBoundedHPC.json'.

Here, a table with the parameters that you might want to adjust is given:

| Parameter | Description | Example                    |
| --- | ---|----------------------------|
| datasetName | The dataset to use. Options are 'CAIDA', 'TPCDS' and 'synthFromDisk'. | "synthFromDisk"            |
| seed | The random seed for reproducibility. | 42                         |
| readFolder | The folder where the repository is located | "/home/omni-deletes/"      |
| withDeletes | Boolean to indicate if deletes are used in the stream. | true                       |
| ramVals | The amount of RAM to use in MB. | [50, 100]                  |
| dGridSearch | List of rows for the sketch | [3]                        |
| wGridSearch | List of columns for the sketch | [28, 64]                   |
| bGridSearch | List of signature sizes for OmniSketch | [31]                       |
| BGridSearch | List of sample sizes for OmniSketch. Currently, this is determined by ram, w, d, b, numAttributes | [10000]                    |
| numPredicates | The number of predicates in the queries. | 11                         |
| sizeFactorOptions | The size factors of the datasets. | [23]                       |
| experimentName | Give name that will appear in output file name | "Deletes_v_1.2"            
| useMultNumAttributes | Boolean to indicate if we use max number of attributes, or vary with increasing number of attributes | false                      |
| numSynthAttributes | If using synthetic data, this says the number of attributes | 11                         |
| numZipfAttributes | Indicating the number of synth attributes with a zipf distribution | 11                         |
| zipfAlphas | List of zipf parameters | [1.3]                      |
| expOmniSketchVLDB | Boolean to indicate if OmniSketch across rows from VLDB 2024 needs to be used | false                      |
| expOmniSketchVLDBS0 | Boolean to indicate if S0 from VLDB 2024 needs to be used. | false                      |
| expOmniSketchVLDBArrayWithBufferOptBatchDeletes | Boolean to indicate if S2 of the paper needs to be used. | true                       |
| expASH | Boolean to indicate if adaptive Sampling & Hold needs to be used. | true                       |
| expHydra | Boolean to indicate if Hydra needs to be used. | false                      |
| parameterSettingType | Indicating the method of parameter setting, "GridSearch" means sample size B is decided by the other parameters, "Custom" means any parameter can be set, but ram is not limited. | "GridSearch"               |
| percs | Delete ratios to include in experiment. '9.0' means # deletes is 9 * residue stream size. '0.0' means no deletes. | [0.0, 0.33, 1.0, 3.0, 9.0] |
| expReadFile | Boolean to indicate whether time for reading data needs to be measured | true                       |




If you have any questions, don't hesitate to ask.

Wieger Punter


