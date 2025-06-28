package omni.datasets;

import com.fasterxml.jackson.databind.ObjectMapper;
import omni.Config;

import java.io.File;
import java.io.IOException;

public class testReader {

    public static void main(String[] args) throws IOException {
        // This is a placeholder for the main method.
        // You can implement dataset reading logic here.
        System.out.println("Test Reader is running!");
        // Implement your test logic here
        String jsonFilePath = args[0];
        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(new File(jsonFilePath), Config.class);

        SyntheticDataset residu = new SyntheticDataset(config, "residu");
        residu.synthDevDataGenerator(0.901, 5, 1.3);
        residu.synthDevQueryGenerator(0.901, 5, 1.3);
        SyntheticDataset noise = new SyntheticDataset(config, "noise");
        noise.synthDevDataGenerator(0.901, 5, 1.3);

        MixedStreamReader mixedStreamReader = new MixedStreamReader(
                config.readFolder + "input/residu.csv",
                config.readFolder + "input/noise.csv"
        );

        String event;
        while ((event = mixedStreamReader.nextEvent()) != null) {
            System.out.println("Next event: " + event);
        }
    }
}
