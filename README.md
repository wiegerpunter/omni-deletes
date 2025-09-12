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

If you have any questions, don't hesitate to ask.

Wieger Punter


