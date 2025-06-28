package omni.datasets;

import com.fasterxml.jackson.databind.ObjectMapper;
import omni.Config;

import java.io.File;
import java.io.IOException;

public class createDataset {

    public static void main(String[] args) throws IOException {
        // This is a placeholder for the main method.
        // You can implement dataset creation logic here.
        String jsonFilePath = args[0];
        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(new File(jsonFilePath), Config.class);


        SyntheticDataset datasetGenerator = new SyntheticDataset(config);
//        datasetGenerator.synthDevDataGenerator(0.901,20,1.3);
//        datasetGenerator.synthDevQueryGenerator(0.901,28,1.3);
        datasetGenerator.synthDevLoader(0.901,28,1.3);
        System.out.println("Dataset created successfully!");
        //todo:
        // generate a query on the dataset and compute exact answer over data when reading from buffer.
    }
}
