package omni;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.*;


public class Helper {

    public Helper() throws IOException {

        if (!Main.useMultNumAttributes) {
            initFileWriter();
        } else {
            initFileWriter();
            initMultFileWriter();
        }
        //if (Objects.equals(Main.setting, "CompareEstimators") || Objects.equals(Main.setting, "CompareBaselines")) {
         //   initFWCompareBaseline();
        //}
    }

    public void addQueryResult(Query q) throws IOException {
        String[] querySpecificResult = q.getQueryResult();
        String[] result = new String[querySpecificResult.length + settingInfo.length];
        // append settingInfo to querySpecificResult
        System.arraycopy(settingInfo, 0, result, 0, settingInfo.length);
        System.arraycopy(querySpecificResult, 0, result, settingInfo.length, querySpecificResult.length);
        querySpecificResult = null;
        WriteResultToCSV(result);
    }

    public void addQueryResultRamBased(Query q) throws IOException {
        String[] querySpecificResult = q.getQueryResult();
        String[] result = new String[querySpecificResult.length + settingInfo.length];
        // append settingInfo to querySpecificResult
        System.arraycopy(settingInfo, 0, result, 0, settingInfo.length);
        System.arraycopy(querySpecificResult, 0, result, settingInfo.length, querySpecificResult.length);
        querySpecificResult = null;
        WriteResultToCSV(result);
    }

    String[] settingInfo;

    public void setRamSettingInfo(Dataset d, Synopsis s, double avg_update_time) throws IOException {

        settingInfo = new String[9];

        // Dataset specific info;
        settingInfo[0] = String.valueOf(d.size);
        int attrsUsed = 0;
        for (int i = 0; i < d.attributesInWorkload.length; i++) {
            if (d.attributesInWorkload[i]) {
                attrsUsed++;
            }
        }
        settingInfo[1] = String.valueOf(attrsUsed);
        System.out.println("Attrs used: " + attrsUsed + " out of " + d.attributesInWorkload.length + " total.");
        settingInfo[2] = String.valueOf(d.getMemoryUsage());

        // OmniSketch specific info;
        settingInfo[3] = String.valueOf(s.ram);
        settingInfo[4] = s.setting;
        settingInfo[5] = String.valueOf(avg_update_time);
        settingInfo[6] = String.valueOf(s.getMemoryUsage());
        settingInfo[7] = Arrays.toString(s.parameters);
        settingInfo[8] = String.valueOf(d.attributesInWorkload.length);
        if (Main.useMultNumAttributes) {
            WriteMultToCSV(settingInfo);
        }
    }

    public void setRamSettingInfo(CleanDataset d, SynopsisRefactor s, long update_time) throws IOException {

        settingInfo = new String[9];

        // Dataset specific info;
        settingInfo[0] = String.valueOf(d.size);
        int attrsUsed = 0;
        settingInfo[1] = String.valueOf(attrsUsed);
        //System.out.println("Attrs used: " + attrsUsed + " out of " + d.attributesInWorkload.length + " total.");
        settingInfo[2] = String.valueOf(d.getMemoryUsage());

        // OmniSketch specific info;
        settingInfo[3] = String.valueOf(s.ram);
        settingInfo[4] = s.setting;
        settingInfo[5] = String.valueOf(update_time);
        settingInfo[6] = String.valueOf(s.getMemoryUsage());
        settingInfo[7] = Arrays.toString(s.parameters);
        settingInfo[8] = String.valueOf(Main.numStoredAttributes);
        if (Main.useMultNumAttributes) {
            WriteMultToCSV(settingInfo);
        }
    }
    public void initFileWriter() throws IOException {
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String range = Main.rangeQueries ? "_range" : "";
        String CSV_FILE_NAME = Main.outputFolder + "results_" + Main.setting + "_" + Main.datasetName +
                "_" + Main.currentDate + range + "_Rep" + Main.repetition + ".csv";

        //File csvOutputFile = new File(CSV_FILE_NAME);
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        String[] header;
        if (Objects.equals(Main.setting, "Epsilon Gridsearch")) {
            header = new String[]{"streamSize", "numAttributes", "memUsageDataset", "RAM", "setting",
                    "Avg Update Time", "useNewEstimator", "eps", "delta", "epsCM", "epsDS",
                    "epsB", "deltaCM", "deltaDS", "deltaB", "width", "depth", "maxLevel", "maxSize",
                    "memUsageSynopsis", "queryID", "numPredicates", "exactAnswer", "estimate", "absError",
                    "relError", "epsError", "withinThreshold", "withinTightBound",
                    "estTime", "queryText", "exactTime", "sampleLevel", "intersectSize", "bound"};
        } else {
            header = new String[]{"streamSize", "numAttributes", "memUsageDataset", "RAM", "setting",
                "Avg Update Time", "memUsageSynopsis", "parameters", "numAttributesInWorkload",
                    "queryID", "numPredicates",
                    "exactAnswer", "estimate", "absError",
                "relError", "epsError", "withinThreshold",
                "estTime",  "queryText", "exactTime", "intersectSize", "bound", "thrm33case2", "case2Estimate", "zeroEstimate", "condition", "queryBin", "rangeSize"};
        }
        writer.writeNext(header);
        writer.close();
    }

    public void initWorkloadFileWriter(int N) throws IOException {
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String range = Main.rangeQueries ? "_range" : "";

        String CSV_FILE_NAME = Main.inputFolder + "/pointQueries/" + Main.datasetName +
                "/queries_" + Main.datasetName + "_N=" + N + range +".csv";

        //File csvOutputFile = new File(CSV_FILE_NAME);
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        String[] header;
        header = new String[]{"ID", "numPredicates", "predicates", "predAttrs","pointAttrs",
        "rangeQuery", "rangeAttrs", "idxHighestAttr", "exactAnswer", "exactTime", "queryBin","record"};
        writer.writeNext(header);
        writer.close();
    }
    public void writeQuery(Query q, int N) throws IOException {
        String[] query = q.getQueryInfo();
        String range = Main.rangeQueries ? "_range" : "";
        String CSV_FILE_NAME = Main.inputFolder + "/pointQueries/" + Main.datasetName +
                "/queries_" + Main.datasetName + "_N=" + N + range +".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        writer.writeNext(query);
        writer.close();
    }

    public void writeRangeQuery(Query q, int N) throws IOException {
        String[] query = q.getQueryInfoRange();
        String range = Main.rangeQueries ? "_range" : "";
        String CSV_FILE_NAME = Main.inputFolder + "/rangeQueries/" + Main.datasetName +
                "/queries_" + Main.datasetName + "_N=" + N + range +".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        writer.writeNext(query);
        writer.close();
    }

    public void readWorkload(int N, Dataset d) throws IOException, CsvValidationException {
        ArrayList<Query> workload = new ArrayList<>();
        String CSV_FILE_NAME = Main.inputFolder + "/pointQueries/" + Main.datasetName + "/queries_" +
                Main.datasetName + "_N=";
        if (Main.sensitivityAnalysis) {
             CSV_FILE_NAME = CSV_FILE_NAME + Main.sensitivityNumberOfRecords + ".csv";
        } else {
            CSV_FILE_NAME = CSV_FILE_NAME + N + ".csv";
        }
        File f = new File(CSV_FILE_NAME);
        if (!f.exists()) {
            int dsSpecN = switch (Main.datasetName) {
                case "CAIDA" -> 5077343;
                case "SNMP" -> {
                    if (Main.runOnODC) {
                        yield 8262313;
                    } else {
                        yield 2767229;
                    }
                }
                default -> Main.sensitivityNumberOfRecords;
            };

            CSV_FILE_NAME = Main.inputFolder + "/pointQueries/" + Main.datasetName + "/queries_" +
                    Main.datasetName + "_N=" + dsSpecN + ".csv";
        }
        CSVReader reader = new CSVReader(new FileReader(CSV_FILE_NAME));
        String[] nextLine;
        reader.readNext(); // skip header
        try {
            while ((nextLine = reader.readNext()) != null) {
                Query q = new Query(nextLine);
                workload.add(q);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        reader.close();
        boolean useQuery;
        if (!f.exists()) {
            //d.queryWithPredicates[q.predAttrs.size() - 1] = true;
            if (Main.sensitivityAnalysis) {
                System.out.println("Computing ground truth for " + Main.sensitivityNumberOfRecords + " records");
            } else {
                System.out.println("Computing ground truth for " + N + " records");
            }

            long[] rec;
            for (Query q: workload) {
                q.exactAnswer = 0;
            }
            int i = 0;
            for (Record r : d.dataset) {
                // for every record, check for every query whether it satisfies predicate
                rec = r.getRecord();
                d.batchExact(rec, workload);
                if (Main.sensitivityAnalysis) {
                    if (i >= Main.sensitivityNumberOfRecords) {
                        break;
                    }
                }
                i++;

            }// Step 1: Calculate the quartiles
            List<Double> exactAnswers = new ArrayList<>();
            for (Query q : workload) {
                exactAnswers.add(q.exactAnswer);
            }
            Collections.sort(exactAnswers);
            int n = exactAnswers.size();
            double Q1 = exactAnswers.get(n / 4);
            double Q2 = exactAnswers.get(n / 2);
            double Q3 = exactAnswers.get((3 * n) / 4);
            System.out.println("Q1: " + Q1);
            System.out.println("Q2: " + Q2);
            System.out.println("Q3: " + Q3);
            //System.out.println("Max exact answer: " + maxExactAnswer);

// Step 2: Assign queries to their corresponding quartile bin
            //List<Query> queries = new ArrayList<>();
            int[] queryBins = new int[4];
            int droppedQueriesQ1 = 0;
            int droppedQueriesQ2 = 0;
            int droppedQueriesQ3 = 0;
            int droppedQueriesQ4 = 0;
            for (Query q : workload) {
                if (q.exactAnswer < Q1) {
                    q.queryBin = 0;

                } else if (q.exactAnswer >= Q1 && q.exactAnswer < Q2) {
                    q.queryBin = 1;
                } else if (q.exactAnswer >= Q2 && q.exactAnswer < Q3) {
                    q.queryBin = 2;
                } else {
                    q.queryBin = 3;

                }
            }

            if (Main.sensitivityAnalysis) {
                this.initWorkloadFileWriter(Main.sensitivityNumberOfRecords);
            } else {
                this.initWorkloadFileWriter(N);
            }

            // Write queries to file
            for (Query q: workload) {
                Main.h.writeQuery(q, N);
            }
        }
        for (Query q : workload) {
            useQuery = true;
            ArrayList<Integer> loopList = new ArrayList<>(q.predAttrs);
            for (int attr : loopList) {
                if (!d.attrsToRead[attr]) {
                    useQuery = false;
                    break;
                }
            }
            if (useQuery) {
                d.queries.add(q);
                d.queryWithPredicates[q.predAttrs.size() - 1] = true;
            }

        }
        for (Query q: d.queries) {
            ArrayList<Integer> loopList = new ArrayList<>(q.predAttrs);
            for (int attr : loopList) {
                q.pointAttrs[attr] = true;
                d.attributesInWorkload[attr] = true;
            }
        }

        Main.logger.info("Loaded " + d.queries.size() + " queries from workload");
        System.out.println("Loaded " + d.queries.size() + " queries from workload");

        for (int i = 0; i <= Main.numAttributes - 1; i++) {
            if (d.queryWithPredicates[i]) {
                d.distinctPredSize++;
            }
        }
    }


    public void readRangeWorkload(int N, Dataset d) throws IOException, CsvValidationException {
        ArrayList<Query> workload = new ArrayList<>();
        String range = "_range";
        String CSV_FILE_NAME = Main.inputFolder + "/rangeQueries/" + Main.datasetName + "/queries_" +
                Main.datasetName + "_N=";
        if (Main.sensitivityAnalysis) {
            CSV_FILE_NAME = CSV_FILE_NAME + Main.sensitivityNumberOfRecords + range +".csv";
        } else {
            CSV_FILE_NAME = CSV_FILE_NAME + N + range +".csv";
        }
        File f = new File(CSV_FILE_NAME);
        if (!f.exists()) {
            int dsSpecN = switch (Main.datasetName) {
                case "CAIDA" -> 5077343;
                case "SNMP" -> {
                    if (Main.runOnODC) {
                        yield 8262313;
                    } else {
                        yield 2767229;
                    }
                }
                default -> Main.sensitivityNumberOfRecords;
            };

            CSV_FILE_NAME = Main.inputFolder + "/rangeQueries/" + Main.datasetName + "/queries_" +
                    Main.datasetName + "_N=" + dsSpecN + range +".csv";
        }
        CSVReader reader = new CSVReader(new FileReader(CSV_FILE_NAME));
        String[] nextLine;
        reader.readNext(); // skip header
        try {
            while ((nextLine = reader.readNext()) != null) {
                Query q = new Query(nextLine);
                workload.add(q);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        reader.close();
        boolean useQuery;
        if (!f.exists()) {
            //d.queryWithPredicates[q.predAttrs.size() - 1] = true;
            if (Main.sensitivityAnalysis) {
                System.out.println("Computing ground truth for " + Main.sensitivityNumberOfRecords + " records");
            } else {
                System.out.println("Computing ground truth for " + N + " records");
            }

            long[] rec;
            for (Query q: workload) {
                q.exactAnswer = 0;
            }
            int i = 0;
            for (Record r : d.dataset) {
                // for every record, check for every query whether it satisfies predicate
                rec = r.getRecord();
                d.batchExactRange(rec, workload);
                if (Main.sensitivityAnalysis) {
                    if (i >= Main.sensitivityNumberOfRecords) {
                        break;
                    }
                }
                i++;

            }
            if (Main.sensitivityAnalysis) {
                this.initWorkloadFileWriter(Main.sensitivityNumberOfRecords);
            } else {
                this.initWorkloadFileWriter(N);
            }

            // Write queries to file
            for (Query q: workload) {
                Main.h.writeQuery(q, N);
            }
        }
        for (Query q : workload) {
            useQuery = true;
            ArrayList<Integer> loopList = new ArrayList<>(q.predAttrs);
            for (int attr : loopList) {
                if (!d.attrsToRead[attr]) {
                    useQuery = false;
                    break;
                }
            }
            if (useQuery) {
                d.rangeQueries.add(q);
                d.queryWithPredicates[q.predAttrs.size() - 1] = true;
            }

        }
        for (Query q: d.rangeQueries) {
            ArrayList<Integer> loopList = new ArrayList<>(q.predAttrs);
            for (int attr : loopList) {
                //q.rangeAttrs.set(attr, true);
                d.attributesInWorkload[attr] = true;
            }
        }

        Main.logger.info("Loaded " + d.queries.size() + " queries from workload");
        System.out.println("Loaded " + d.queries.size() + " queries from workload");

        for (int i = 0; i <= Main.numAttributes - 1; i++) {
            if (d.queryWithPredicates[i]) {
                d.distinctPredSize++;
            }
        }
    }

    public void initMultFileWriter() throws IOException {
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String CSV_FILE_NAME = Main.outputFolder + "mult_" + Main.datasetName + "_" + Main.currentDate + ".csv";

        //File csvOutputFile = new File(CSV_FILE_NAME);
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        String[] header;
        header = new String[]{"streamSize", "numAttributes", "memUsageDataset", "RAM", "setting",
                    "Avg Update Time", "memUsageSynopsis", "parameters"};
        writer.writeNext(header);
        writer.close();
    }

    public void WriteResultToCSV(String[] result) throws IOException {
        // write string[] result to csvOutputFile using BufferedWriter
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String range = Main.rangeQueries ? "_range" : "";
        String CSV_FILE_NAME = Main.outputFolder + "results_" + Main.setting + "_" + Main.datasetName +
                "_" + Main.currentDate + range +  "_Rep" + Main.repetition + ".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        writer.writeNext(result);
        writer.close();
    }

    public void WriteMultToCSV(String[] result) throws IOException {
        // write string[] result to csvOutputFile using BufferedWriter
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String CSV_FILE_NAME = Main.outputFolder + "mult_" + Main.datasetName + "_" + Main.currentDate + ".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        writer.writeNext(result);
        writer.close();
    }

    public void addMemSetting() throws IOException {
        WriteMemSettingToCSV(settingInfo);
    }

    private void WriteMemSettingToCSV(String[] settingInfo) throws IOException {
        // write string[] result to csvOutputFile using BufferedWriter
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String CSV_FILE_NAME = Main.outputFolder + "memCons_" + Main.datasetName + "_"+ Main.currentDate + ".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        writer.writeNext(settingInfo);
        writer.close();
    }
    public void initSynthDataset(String[] data, int N, int zipfAttrs, double zipfAlpha, boolean differAlphas) throws IOException {
        //initialize csv file
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String CSV_FILE_NAME = Main.outputFolder + "synthDataset_" + Main.datasetName + "_N="+ N +
                "_ZipfAttrs="+ zipfAttrs + "_zipfAlpha="+ zipfAlpha + "_differAlphas="+ differAlphas + ".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        String[] header;
        header = new String[]{"streamSize", "numAttributes", "zipfDistrAttr", "zipfAlpha", "ID", "Record"};
        writer.writeNext(header);
    }

    public void writeSynthDataset(String[] data, int N, int zipfAttrs, double zipfAlpha, boolean differAlphas) throws IOException {
        // write string[] result to csvOutputFile using BufferedWriter
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String CSV_FILE_NAME = Main.outputFolder + "synthDataset_" + Main.datasetName + "_N="+ N +
                "_ZipfAttrs="+ zipfAttrs + "_zipfAlpha="+ zipfAlpha + "_differAlphas="+ differAlphas + ".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        writer.writeNext(data);
        writer.close();
    }

    public ArrayList<Record> readSynthDataset(int N, int zipfAttrs, double zipfAlpha, boolean differAlphas) throws IOException, CsvValidationException {
        ArrayList<Record> dataset = new ArrayList<>();
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String CSV_FILE_NAME = Main.outputFolder + "synthDataset_" + Main.datasetName + "_N="+ N +
                "_ZipfAttrs="+ zipfAttrs + "_zipfAlpha="+ zipfAlpha + "_differAlphas="+ differAlphas + ".csv";
        CSVReader reader = new CSVReader(new FileReader(CSV_FILE_NAME));
        String[] nextLine;
        //reader.readNext(); // skip header
        //System.out.println("Header: " + Arrays.toString(reader.readNext()));
        nextLine = reader.readNext();
        try {
            while ((nextLine) != null) {
//                int size = Integer.parseInt(nextLine[0]);
//                int numAttr = Integer.parseInt(nextLine[1]);
//                double zipfDistrAttr = Double.parseDouble(nextLine[2]);
//                double zipfAlpha = Double.parseDouble(nextLine[3]);
                int ID = Integer.parseInt(nextLine[4]);
                long[] record = parseLongArr(nextLine[5]);

                Record r = new RecordSynth(ID, record);
                dataset.add(r);
                nextLine = reader.readNext();
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return dataset;
    }


    private long[] parseLongArr(String infoString) {
        infoString = infoString.replaceAll("\\[|\\]", ""); // Remove square brackets
        String[] values = infoString.split(",");
        long[] info = new long[values.length];

        for (int i = 0; i < values.length; i++) {
            info[i] = Long.parseLong(values[i].trim());
        }
        return info;
    }


    public String convertToCSV(String[] data) {
        return String.join(",", data);
    }

    public void initSNMPDataset(Dataset d) throws IOException {
        //initialize csv file
        String CSV_FILE_NAME = Main.inputFolder + "/data/" + Main.datasetName +
                "/SNMPDataset_" + Main.datasetName + "_"+ Main.fileStartCondition + ".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        String[] header;
        header = new String[]{"id", "timestamp", "AP", "sysUpTime", "sysDescr", "ifIndex", "ifDescr", "ifType", "ifSpeed",
        "ifInOctets", "ifInUcastPkts", "ifInErrors", "ifInDiscards", "ifOutOctets", "ifOutUcastPkts", "ifOutErrors", "ifOutDiscards",
        "awcDot11AssociatedStationCount", "awcDot11ReassociatedStationCount", "awcDot11RoamedStationCount", "awcDot11DeauthenicateCount",
        "awcDot11DisassociateCount", "awcFtClientSTASelf", "awcFtBridgeSelf", "awcFtRepeaterSelf"};

        writer.writeNext(header);
    }
    public void initSNMPDataset(DatasetRefactor d) throws IOException {
        //initialize csv file
        String CSV_FILE_NAME = Main.inputFolder + "/data/" + Main.datasetName +
                "/SNMPDataset_" + Main.datasetName + "_"+ Main.fileStartCondition + ".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        String[] header;
        header = new String[]{"id", "timestamp", "AP", "sysUpTime", "sysDescr", "ifIndex", "ifDescr", "ifType", "ifSpeed",
                "ifInOctets", "ifInUcastPkts", "ifInErrors", "ifInDiscards", "ifOutOctets", "ifOutUcastPkts", "ifOutErrors", "ifOutDiscards",
                "awcDot11AssociatedStationCount", "awcDot11ReassociatedStationCount", "awcDot11RoamedStationCount", "awcDot11DeauthenicateCount",
                "awcDot11DisassociateCount", "awcFtClientSTASelf", "awcFtBridgeSelf", "awcFtRepeaterSelf"};

        writer.writeNext(header);
    }

    public void writeSNMPDataset(String[] data) throws IOException {
        // write string[] result to csvOutputFile using BufferedWriter
        String CSV_FILE_NAME = Main.inputFolder + "/data/" + Main.datasetName +
                "/SNMPDataset_" + Main.datasetName + "_"+ Main.fileStartCondition + ".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        writer.writeNext(data);
        writer.close();
    }
    public ArrayList<Record> readSNMPDataset() throws IOException, CsvValidationException {
        ArrayList<Record> dataset = new ArrayList<>();
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String CSV_FILE_NAME = Main.inputFolder + "/data/" + Main.datasetName +
                "/SNMPDataset_" + Main.datasetName + "_"+ Main.fileStartCondition + ".csv";
        System.out.println("Reading from file: " + CSV_FILE_NAME);
        CSVReader reader = new CSVReader(new FileReader(CSV_FILE_NAME));
        String[] nextLine;
        //reader.readNext(); // skip header
        //System.out.println("Header: " + Arrays.toString(reader.readNext()));
        long largestValue = 0;
        nextLine = reader.readNext();
        try {
            while ((nextLine) != null) {
                long ID = Long.parseLong(nextLine[0]);
                long timestamp = Long.parseLong(nextLine[1]);
                long[] record = new long[]{Long.parseLong(nextLine[2]), Long.parseLong(nextLine[3]), Long.parseLong(nextLine[4]),
                        Long.parseLong(nextLine[5]), Long.parseLong(nextLine[6]), Long.parseLong(nextLine[7]), Long.parseLong(nextLine[8]),
                        Long.parseLong(nextLine[9]), Long.parseLong(nextLine[10]), Long.parseLong(nextLine[11]), Long.parseLong(nextLine[12]),
                        Long.parseLong(nextLine[13]), Long.parseLong(nextLine[14]), Long.parseLong(nextLine[15]), Long.parseLong(nextLine[16]),
                        Long.parseLong(nextLine[17]), Long.parseLong(nextLine[18]), Long.parseLong(nextLine[19]), Long.parseLong(nextLine[20]),
                        Long.parseLong(nextLine[21]), Long.parseLong(nextLine[22]), Long.parseLong(nextLine[23]), Long.parseLong(nextLine[24])};

                Record r = new RecordSNMP(ID,timestamp,record);
                for (long l : record) {
                    if (l > largestValue) {
                        largestValue = l;
                    }
                }
                dataset.add(r);
                nextLine = reader.readNext();
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }

        System.out.println("Largest value in dataset: " + largestValue + " So we need " + (int) Math.ceil(Math.log(largestValue)/Math.log(2)) + " bits to represent it");
        return dataset;
    }

    public ArrayList<Record> readWCDataset() throws IOException, CsvValidationException {
        ArrayList<Record> dataset = new ArrayList<>();
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String CSV_FILE_NAME = Main.outputFolder + "SNMPDataset_" + Main.datasetName + "_"+ Main.fileStartCondition + ".csv";
        System.out.println("Reading from file: " + CSV_FILE_NAME);
        CSVReader reader = new CSVReader(new FileReader(CSV_FILE_NAME));
        String[] nextLine;
        //reader.readNext(); // skip header
        //System.out.println("Header: " + Arrays.toString(reader.readNext()));
        long largestValue = 0;
        nextLine = reader.readNext();
        try {
            while ((nextLine) != null) {
                long ID = Long.parseLong(nextLine[0]);
                long timestamp = Long.parseLong(nextLine[1]);
                long[] record = new long[]{Long.parseLong(nextLine[2]), Long.parseLong(nextLine[3]), Long.parseLong(nextLine[4]),
                        Long.parseLong(nextLine[5]), Long.parseLong(nextLine[6]), Long.parseLong(nextLine[7]), Long.parseLong(nextLine[8]),
                        Long.parseLong(nextLine[9]), Long.parseLong(nextLine[10]), Long.parseLong(nextLine[11]), Long.parseLong(nextLine[12]),
                        Long.parseLong(nextLine[13]), Long.parseLong(nextLine[14]), Long.parseLong(nextLine[15]), Long.parseLong(nextLine[16]),
                        Long.parseLong(nextLine[17]), Long.parseLong(nextLine[18]), Long.parseLong(nextLine[19]), Long.parseLong(nextLine[20]),
                        Long.parseLong(nextLine[21]), Long.parseLong(nextLine[22]), Long.parseLong(nextLine[23]), Long.parseLong(nextLine[24])};

                Record r = new RecordSNMP(ID,timestamp,record);
                for (long l : record) {
                    if (l > largestValue) {
                        largestValue = l;
                    }
                }
                dataset.add(r);
                nextLine = reader.readNext();
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }

        System.out.println("Largest value in dataset: " + largestValue + " So we need " + (int) Math.ceil(Math.log(largestValue)/Math.log(2)) + " bits to represent it");
        return dataset;
    }

    public void initRangeWorkloadFileWriter(int N) throws IOException {
        String range = Main.rangeQueries ? "_range" : "";


        String CSV_FILE_NAME = Main.inputFolder + "/rangeQueries/" + Main.datasetName + "/queries_" +
                Main.datasetName + "_N=" + N + range +".csv";

        //File csvOutputFile = new File(CSV_FILE_NAME);
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        String[] header;
        header = new String[]{"ID", "numPredicates", "predicates", "predAttrs","pointAttrs",
                "rangeQuery", "rangeAttrs", "idxHighestAttr", "exactAnswer", "exactTime", "queryBin","record", "lower", "upper"};
        writer.writeNext(header);
        writer.close();
    }

    public void writePointQueriesToFile(CleanDataset d) {
        String CSV_FILE_NAME = Main.inputFolder + "/pointQueries/" + Main.datasetName +
                "/queries_" + Main.datasetName + "_N=" + Main.sensitivityNumberOfRecords +".csv";
        File f = new File(CSV_FILE_NAME);
        if (!f.exists()) {
            try {
                this.initWorkloadFileWriter(Main.sensitivityNumberOfRecords);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i< d.pointQueries.length; i++) {
            // TODO: store point queries in a different file
            // store query itself, exact answer, number of attributes in query, number of predicates in query
            System.out.println("Not implemented");
        }
    }

    public void writeRangeQueriesToFile() {
        String range = Main.rangeQueries ? "_range" : "";
        String CSV_FILE_NAME = Main.inputFolder + "/rangeQueries/" + Main.datasetName +
                "/queries_" + Main.datasetName + "_N=" + Main.sensitivityNumberOfRecords + range +".csv";
        File f = new File(CSV_FILE_NAME);
        if (!f.exists()) {
            try {
                this.initWorkloadFileWriter(Main.sensitivityNumberOfRecords);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Not implemented");
    }


    public void writeResultsToFileRangeQuery(SynopsisRefactor s, CleanDataset d, long timePassed,
                                             int[] estimatedAnswersRangeQuery, long[] queryExecutionTime) {
    }

    public void writeResultsToFilePointQuery(SynopsisRefactor s, CleanDataset d,
                                             long timePassed, int[] estimatedAnswersPointQuery,
                                             int[] SCap, int[] NMax, double[] jaccardEstimates2LHS,
                                             double[] unionEstimates2LHS, int[] witness2LHS, long[] queryExecutionTime,
                                             long totalQueryExecutionTime) throws IOException {
       // write string[] result to csvOutputFile using BufferedWriter
        boolean init = false;
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String CSV_FILE_NAME = Main.outputFolder + "/pointQueries/" + Main.datasetName + "/results_"
                + Main.setting + "_" + Main.datasetName +
                "_" + Main.currentDate +  "_Rep" + Main.repetition + ".csv";
        if (!new File(CSV_FILE_NAME).exists()) {
            init = true;
        }
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));

        if (init) {
            String[] header = new String[]{"repetition","totalStreamSize", "noiseUpdates", "streamSizeAfterDeletes",
                    "numAttributes", "memUsageDataset", "RAM", "setting", "sampleType",
                    "Avg Update Time", "memUsageSynopsis", "parameters", "numAttributesInWorkload",
                    "queryID", "numPredicates","binNumber",
                    "exactAnswer", "estimate", "absError",
                    "relError", "epsError", "withinThreshold",
                    "estTime",  "queryText", "SCap", "NMax","jacEstimate2LHS", "unionEstimate2LHS","witness2LHS",
                    "intersectionSizeOfR","unionSizeOfR",
                    "meanQueryTime","TwoKmin","numKSamples","KminDeletes","exactInDeletes","exactUnionDeletes"};
            //TODO: add more info to header
            //, "exactTime", "intersectSize"};
            //"bound", "thrm33case2", "case2Estimate", "zeroEstimate",
            //"condition", "queryBin", "rangeSize"};
            writer.writeNext(header);
        }



        for (int i = 0; i < Main.numQueries; i++) {
            String[] result = new String[37];
            // Dataset specific info;
            result[0] = String.valueOf(Main.repetition);
            result[1] = String.valueOf(d.dataset.length);
            if (d.noiseUpdates == null) {
                result[2] = String.valueOf(d.datasetNegUpdates.length);
            } else {
                result[2] = String.valueOf(d.noiseUpdates.length);
            }
            result[3] = String.valueOf(d.datasetResidu.length);
            result[4] = String.valueOf(Main.numAttributes);

            result[5] = String.valueOf(d.getMemoryUsage());
            result[6] = String.valueOf(s.ram);
            result[7] = s.setting;
            result[8] = s.sampleType;
            result[9] = String.valueOf(timePassed);
            result[10] = String.valueOf(s.getMemoryUsage());
            result[11] = Arrays.toString(s.parameters);
            result[12] = String.valueOf(Main.numStoredAttributes);
            result[13] = String.valueOf(i);
            result[14] = String.valueOf(d.pointQueriesNumAttrs[i]);
            result[15] = String.valueOf(d.pointQueryBinNumber[i]);
            result[16] = String.valueOf(d.pointQueryAnswers[i]);
            result[17] = String.valueOf(estimatedAnswersPointQuery[i]);
            result[18] = String.valueOf(Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]));
            result[19] = String.valueOf((double) Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) / d.pointQueryAnswers[i]);
            result[20] = String.valueOf((double) Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) / d.datasetResidu.length);
            result[21] = String.valueOf(Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) <= Main.eps * d.datasetResidu.length);
            result[22] = String.valueOf(queryExecutionTime[i]);
            result[23] = d.parsePointQueryToString(d.pointQueries[i]);
            result[24] = String.valueOf(SCap[i]);
            result[25] = String.valueOf(NMax[i]);
            result[26] = String.valueOf(jaccardEstimates2LHS[i]);
            result[27] = String.valueOf(unionEstimates2LHS[i]);
            result[28] = String.valueOf(witness2LHS[i]);
            if (Main.checkConditions) {
                result[29] = String.valueOf(intersectionOfR[i]);
                result[30] = String.valueOf(unionOfR[i]);
            };
            result[31] = String.valueOf(totalQueryExecutionTime);
            result[32] = String.valueOf(s.useTwoKmin);
            int numKSamples = 0;
            if (!s.useTwoLHS) {
                numKSamples = s.getFilledKSamples();
            }
            result[33] = String.valueOf(numKSamples);
            result[34] = String.valueOf(Main.kminDeletes);
            result[35] = String.valueOf(d.pointQueryAnswersDeletes[i]);
            result[36] = String.valueOf(d.pointQueryUnionDeletes[i]);
            writer.writeNext(result);
        }

        writer.close();
    }


    public void writeResultsToFilePointQuery(SynopsisRefactor s, CleanDataset d,
                                             long timePassed, int[] estimatedAnswersPointQuery,
                                             int[] SCap, long[] queryExecutionTime,
                                             int[] estimatedAnswersPointQueryResidu) throws IOException {
        // write string[] result to csvOutputFile using BufferedWriter
        boolean init = false;
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String CSV_FILE_NAME = Main.outputFolder + "/pointQueries/" + Main.datasetName
                + "/results_" + Main.setting + "_" + Main.datasetName +
                "_" + Main.currentDate +  "_Rep" + Main.repetition + ".csv";
        if (!new File(CSV_FILE_NAME).exists()) {
            init = true;
        }
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));



        if (init) {
            String[] header = new String[]{"streamSize", "numAttributes", "memUsageDataset", "RAM",
                    "setting",
                    "Avg Update Time", "memUsageSynopsis", "parameters", "numAttributesInWorkload",
                    "queryID", "numPredicates",
                    "exactAnswer", "estimate", "absError",
                    "relError", "epsError", "withinThreshold",
                    "estTime",  "queryText", "estimateResidu", "absErrorResidu",
                    "relErrorResidu", "epsErrorResidu", "withinThresholdResidu",
                    "numNoiseRecords", "numCleanRecords","exactInDeletes","exactUnionDeletes"};
            //TODO: add more info to header
            //, "exactTime", "intersectSize"};
            //"bound", "thrm33case2", "case2Estimate", "zeroEstimate",
            //"condition", "queryBin", "rangeSize"};
            writer.writeNext(header);
        }



        for (int i = 0; i < Main.numQueries; i++) {
            String[] result = new String[28];
            // Dataset specific info;
            result[0] = String.valueOf(d.numDeletes + d.datasetResidu.length);
            result[1] = String.valueOf(Main.numAttributes);
            result[2] = String.valueOf(d.getMemoryUsage());
            result[3] = String.valueOf(s.ram);
            result[4] = s.setting;
            result[5] = String.valueOf(timePassed);
            result[6] = String.valueOf(s.getMemoryUsage());
            result[7] = Arrays.toString(s.parameters);
            result[8] = String.valueOf(Main.numStoredAttributes);
            result[9] = String.valueOf(i);
            result[10] = String.valueOf(d.pointQueriesNumAttrs[i]);
            result[11] = String.valueOf(d.pointQueryAnswers[i]);
            result[12] = String.valueOf(estimatedAnswersPointQuery[i]);
            result[13] = String.valueOf(Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]));
            result[14] = String.valueOf((double) Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) / d.pointQueryAnswers[i]);
            result[15] = String.valueOf((double) Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) / d.datasetResidu.length);
            result[16] = String.valueOf(Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) <= Main.eps * d.datasetResidu.length);
            result[17] = String.valueOf(queryExecutionTime[i]);
            result[18] = d.parsePointQueryToString(d.pointQueries[i]);
            result[19] = String.valueOf(estimatedAnswersPointQueryResidu[i]);
            result[20] = String.valueOf(Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQueryResidu[i]));
            result[21] = String.valueOf((double) Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQueryResidu[i]) / d.pointQueryAnswers[i]);
            result[22] = String.valueOf((double) Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQueryResidu[i]) / d.datasetResidu.length);
            result[23] = String.valueOf(Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQueryResidu[i]) <= Main.eps * d.datasetResidu.length);
            result[24] = String.valueOf(d.numDeletes);
            result[25] = String.valueOf(d.datasetResidu.length);
            result[26] = String.valueOf(d.pointQueryAnswersDeletes[i]);
            result[27] = String.valueOf(d.pointQueryUnionDeletes[i]);
            //result[19] = String.valueOf(d.avgExactExecutionTime);
            //result[20] = String.valueOf(SCap[i]);
            writer.writeNext(result);
        }

        writer.close();
    }
    int[] intersectionOfR;
    int[] unionOfR;
    public void setConditionInfo(int[] intersectionOfR, int[] unionOfR) {
        this.intersectionOfR = intersectionOfR;
        this.unionOfR = unionOfR;
        System.out.println("Set condition info");
        System.out.println("Intersection of R: " + intersectionOfR[0]);
    }
}
