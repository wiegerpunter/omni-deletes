package omni.datasets;

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

        SyntheticDataset residu = new SyntheticDataset(config, "residu");
        residu.synthDevDataGenerator(0.5, 5, 1.3);
        residu.synthDevQueryGenerator(0.5, 5, 1.3);

    }
}
