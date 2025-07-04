package omni.datasets.fromDisk;
import com.fasterxml.jackson.databind.ObjectMapper;
import omni.Config;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MixResiduNoise {


    public MixResiduNoise(Config config) throws IOException {
        File synthRootFolder = new File(config.readFolder + "input/data/synthFromDisk/20.0/");
        if (!synthRootFolder.exists() || !synthRootFolder.isDirectory()) {
            throw new IOException("Synth folder does not exist: " + synthRootFolder.getAbsolutePath());
        }

        // Pattern to detect shuffled residu file and extract suffix
        Pattern pattern = Pattern.compile("residu(.*)\\.csv");

        // Recursively process subfolders
        processFolderRecursively(synthRootFolder, pattern);
    }

    private void processFolderRecursively(File folder, Pattern pattern) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) return;

        boolean foundResidu = false;
        String suffix = null;
        String residuFile = null;
        String insertFile = null;
        String deleteFile = null;

        for (File file : files) {
            if (file.isDirectory()) {
                processFolderRecursively(file, pattern);  // Recurse into subfolders
            } else {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    suffix = matcher.group(1);  // Includes leading underscores
                    residuFile = file.getAbsolutePath();
                    insertFile = new File(folder, "noise_inserts" + suffix + ".csv").getAbsolutePath();
                    deleteFile = new File(folder, "noise_deletes" + suffix + ".csv").getAbsolutePath();
                    foundResidu = true;
                    break;
                }
            }
        }

        if (foundResidu) {
            if (!new File(residuFile).exists()) {
                throw new FileNotFoundException("Residu file missing in: " + folder.getAbsolutePath());
            }

            boolean hasInserts = new File(insertFile).exists();
            boolean hasDeletes = new File(deleteFile).exists();

            System.out.printf("\rProcessing folder: " + folder.getAbsolutePath());
            System.out.printf("\rResidu: " + residuFile);
            if (hasInserts) System.out.println("Inserts: " + insertFile);
            if (hasDeletes) System.out.println("Deletes: " + deleteFile);

            // Create final stream file in parent folder
            //String finalStreamFile = new File(folder, "final_stream" + suffix + ".csv").getAbsolutePath();
            String finalStreamFile = new File(folder.getParent(), "final_stream" + suffix + ".csv").getAbsolutePath();

            mixFiles(residuFile, hasInserts ? insertFile : null, hasDeletes ? deleteFile : null, finalStreamFile);
        }
    }

    private void mixFiles(String residuFile, String insertFile, String deleteFile, String finalStreamFile) throws IOException {
        BufferedReader residuReader = new BufferedReader(new FileReader(residuFile));
        BufferedReader insertReader = insertFile != null ? new BufferedReader(new FileReader(insertFile)) : null;
        BufferedReader deleteReader = deleteFile != null ? new BufferedReader(new FileReader(deleteFile)) : null;
        BufferedWriter writer = new BufferedWriter(new FileWriter(finalStreamFile));

        String residuLine = residuReader.readLine();
        String insertLine = insertReader != null ? insertReader.readLine() : null;
        String deleteLine = deleteReader != null ? deleteReader.readLine() : null;

        Set<Integer> emittedNoiseIds = new HashSet<>();
        Random rand = new Random();
        int differenceInsertsAndDeletes = 0;

        //change shuffling, do it in memory
        int threshold = 100; // Threshold for noise mixing
        while (residuLine != null || insertLine != null || deleteLine != null) {
            List<String> options = new ArrayList<>();
            if (residuLine != null) options.add("residu");
            if (insertLine != null) options.add("insert");
            if (deleteLine != null) {
                if (differenceInsertsAndDeletes > threshold ) {
                    int id = getId(deleteLine);
                    if (emittedNoiseIds.contains(id)) {
                        options.add("delete");
                    }
                }
            }
            if (options.isEmpty()) break;

            String choice = options.get(rand.nextInt(options.size()));

            switch (choice) {
                case "residu":
                    writer.write(residuLine + "\n");
                    residuLine = residuReader.readLine();
                    break;
                case "insert":
                    writer.write(insertLine + "\n");
                    assert insertLine != null;
                    emittedNoiseIds.add(getId(insertLine));
                    insertLine = insertReader.readLine();
                    differenceInsertsAndDeletes++;
                    break;
                case "delete":
                    writer.write(deleteLine + "\n");
                    assert deleteReader != null;
                    deleteLine = deleteReader.readLine();
                    differenceInsertsAndDeletes--;
                    break;
            }
        }

        // Phase 2: Output remaining deletes unconditionally
        while (deleteLine != null) {
            writer.write(deleteLine + "\n");
            deleteLine = deleteReader.readLine();
        }

        residuReader.close();
        if (insertReader != null) insertReader.close();
        if (deleteReader != null) deleteReader.close();
        writer.close();
    }


    private int getId(String line) {
        return Integer.parseInt(line.split(",")[0]); // Assuming ID is the first column
    }


    public static void main(String[] args) throws IOException {
        String jsonFilePath = args[0];
        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(new File(jsonFilePath), Config.class);

        try {
            new MixResiduNoise(config);
            System.out.println("Mixed stream created successfully.");
        } catch (IOException e) {
            System.err.println("Error creating mixed stream: " + e.getMessage());
        }
    }
}
