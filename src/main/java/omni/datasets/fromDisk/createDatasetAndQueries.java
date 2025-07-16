package omni.datasets.fromDisk;

import com.fasterxml.jackson.databind.ObjectMapper;
import omni.Config;

import java.io.File;
import java.io.IOException;

public class createDatasetAndQueries {

    public static void main(String[] args) throws IOException {
        // This is a placeholder for the main method.
        // You can implement dataset reading logic here.
        // Implement your test logic here
        String jsonFilePath = args[0];
        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(new File(jsonFilePath), Config.class);

        SyntheticDataset residu = new SyntheticDataset(config);
        double[] perc = {0,99};
        for (double p : perc) {
            System.out.printf("\rGenerating dataset with %.1f%% noise", p * 100);
            residu.synthDevDataGenerator(p, 23, 1.3);
            if (p == 0) {
                residu.synthDevQueryGenerator(p, 23, 1.3);
            }
        }


    }
}
