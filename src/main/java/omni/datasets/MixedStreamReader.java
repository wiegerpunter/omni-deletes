package omni.datasets;

import java.io.*;
import java.util.*;

public class MixedStreamReader {

    private final BufferedReader residuReader;
    private final BufferedReader insertNoiseReader;
    private final BufferedReader deleteNoiseReader;

    private int insertsEmitted = 0;
    private int deletesEmitted = 0;
    private final Random random = new Random();

    public MixedStreamReader(String residuFile, String noiseFile) throws IOException {
        residuReader = new BufferedReader(new FileReader(residuFile));
        insertNoiseReader = new BufferedReader(new FileReader(noiseFile));
        deleteNoiseReader = new BufferedReader(new FileReader(noiseFile));
    }

    public String nextEvent() throws IOException {
        List<String> availableStreams = new ArrayList<>();

        if (residuReader.ready()) availableStreams.add("residu");
        if (insertNoiseReader.ready()) availableStreams.add("insertNoise");
        if (deletesEmitted < insertsEmitted && deleteNoiseReader.ready()) availableStreams.add("deleteNoise");

        if (availableStreams.isEmpty()) return null; // All streams exhausted

        String choice = availableStreams.get(random.nextInt(availableStreams.size()));

        return switch (choice) {
            case "residu" -> residuReader.readLine();
            case "insertNoise" -> {
                insertsEmitted++;
                yield insertNoiseReader.readLine();
            }
            case "deleteNoise" -> {
                deletesEmitted++;
                yield deleteNoiseReader.readLine();
            }
            default -> throw new IllegalStateException("Unknown stream selected.");
        };
    }

    public void close() throws IOException {
        residuReader.close();
        insertNoiseReader.close();
        deleteNoiseReader.close();
    }
}
