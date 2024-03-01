package omni.wlReader;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import omni.Main;
import omni.Query;
import omni.Record;
import omni.Old.TempDataset;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class WLReaderSNMP {
    TempDataset dataset;
    String condition;
    ArrayList<Query> queries = new ArrayList<Query>();
    public WLReaderSNMP(TempDataset dataset) {
        this.dataset = dataset;
    }
    public Workload reader(String condition) throws CsvValidationException, IOException {

        ArrayList<Query> queries = new ArrayList<Query>();
        Workload workload = new Workload("SNMP");
        if (Main.rangeQueries) {
            // check if wl already exists for this condition
            rangeWorkload();
        } else {
            pointWorkload();
        }
    }

    private void rangeWorkload() throws CsvValidationException, IOException {

        String range = "_range";
        String CSV_FILE_NAME;
        if (Main.sensitivityAnalysis) {
            CSV_FILE_NAME = Main.outputFolder + "queries_" + Main.datasetName + "_N=" + Main.sensitivityNumberOfRecords + range +".csv";
        } else {
            CSV_FILE_NAME = Main.outputFolder + "queries_" + Main.datasetName + "_N=" + this.dataset.records.size() + range+".csv";
        }
        File f = new File(CSV_FILE_NAME);
        if (!f.exists()) {
            // create new workload, but first check if we only have to recompute exact answers or also have to generate new queries
            int defaultN;
            if (Main.runOnODC) {
                defaultN =  8262313;}
            else {
                defaultN = 2767229;
            };
            CSV_FILE_NAME = Main.outputFolder + "queries_" + Main.datasetName + "_N=" + defaultN + range +".csv";
            f = new File(CSV_FILE_NAME);
            if (!f.exists()) {
                // create new workload
                genRangeWorkload();
            } else {
                // load existing workload
                loadRangeWorkload(CSV_FILE_NAME, true);
                return;
            }
        } else {
            // load existing workload
            loadRangeWorkload(CSV_FILE_NAME, false);
            return;
        }



    }

    private void loadRangeWorkload(String path, boolean fromDefault) throws IOException, CsvValidationException {

        CSVReader reader = new CSVReader(new FileReader(path));
        String[] nextLine;
        reader.readNext(); // skip header
        try {
            while ((nextLine = reader.readNext()) != null) {
                Query q = new Query(nextLine);
                queries.add(q);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        reader.close();
        boolean useQuery;
        if (fromDefault) {
            //d.queryWithPredicates[q.predAttrs.size() - 1] = true;
            if (Main.sensitivityAnalysis) {
                System.out.println("Computing ground truth for " + Main.sensitivityNumberOfRecords + " records");
            } else {
                System.out.println("Computing ground truth for " + dataset.records.size() + " records");
            }

            long[] rec;
            for (Query q: queries) {
                q.exactAnswer = 0;
            }
            int i = 0;
            for (Record r : dataset.records) {
                // for every record, check for every query whether it satisfies predicate
                rec = r.getRecord();
                dataset.batchExactRange(rec, queries);
                if (Main.sensitivityAnalysis) {
                    if (i >= Main.sensitivityNumberOfRecords) {
                        break;
                    }
                }
                i++;
            }
            if (Main.sensitivityAnalysis) {
                this.initWorkloadFileWriter(Main.sensitivityNumberOfRecords);
                for (Query q: queries) {
                    Main.h.writeQuery(q, Main.sensitivityNumberOfRecords);
                }
            } else {
                this.initWorkloadFileWriter(dataset.records.size());
                for (Query q: queries) {
                    Main.h.writeQuery(q, dataset.records.size());
                }
            }

        }
        for (Query q : queries) {
            useQuery = true;
            ArrayList<Integer> loopList = new ArrayList<>(q.predAttrs);
            for (int attr : loopList) {
                if (!dataset.attrsToRead[attr]) {
                    useQuery = false;
                    break;
                }
            }
            if (useQuery) {
                rangeQueries.add(q);
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

    private void initWorkloadFileWriter(int size) {
    }

    private void loadRangeWorkloadFromDefault() {
    }

    private void genRangeWorkload() {
    }
}
