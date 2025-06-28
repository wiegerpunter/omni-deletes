package omni.datasets;
import com.fasterxml.jackson.databind.ObjectMapper;
import omni.Config;

import java.io.*;
import java.util.*;

public class MixedStreamReader{


    public MixedStreamReader(Config config) throws IOException {
        BufferedReader residuReader = new BufferedReader(new FileReader(config.readFolder + "input/data/synth" + "residu_shuffled.csv"));
        BufferedReader insertReader = new BufferedReader(new FileReader(config.readFolder + "input/data/synth" + "noise_inserts_shuffled.csv"));
        BufferedReader deleteReader = new BufferedReader(new FileReader(config.readFolder + "input/data/synth" + "noise_deletes_shuffled.csv"));
        BufferedWriter writer = new BufferedWriter(new FileWriter(config.readFolder + "input/data/synth" + "final_stream.csv"));

        String residuLine = residuReader.readLine();
        String insertLine = insertReader.readLine();
        String deleteLine = deleteReader.readLine();

        Set<String> emittedNoiseIds = new HashSet<>();
        Random rand = new Random();

        while (residuLine != null || insertLine != null || deleteLine != null) {
            List<String> options = new ArrayList<>();
            if (residuLine != null) options.add("residu");
            if (insertLine != null) options.add("insert");
            if (deleteLine != null && emittedNoiseIds.contains(getId(deleteLine))) options.add("delete");

            if (options.isEmpty()) break;

            String choice = options.get(rand.nextInt(options.size()));

            switch (choice) {
                case "residu":
                    writer.write(residuLine + "\n");
                    residuLine = residuReader.readLine();
                    break;
                case "insert":
                    writer.write(insertLine + "\n");
                    emittedNoiseIds.add(getId(insertLine));
                    insertLine = insertReader.readLine();
                    break;
                case "delete":
                    writer.write(deleteLine + "\n");
                    deleteLine = deleteReader.readLine();
                    break;
            }
        }

        residuReader.close();
        insertReader.close();
        deleteReader.close();
        writer.close();
    }

    String getId(String line) {
        return line.split(",")[0]; // Assuming ID is the first column
    }

    public static void main(String[] args) throws IOException {
        String jsonFilePath = args[0];
        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(new File(jsonFilePath), Config.class);

        try {
            new MixedStreamReader(config);
            System.out.println("Mixed stream created successfully.");
        } catch (IOException e) {
            System.err.println("Error creating mixed stream: " + e.getMessage());
        }
    }
}
