package omni.synopses.baselines.hydraRefactor;

import omni.Config;
import omni.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class SubPopMap {
    boolean[] attrs;
    boolean[] attrsInWorkload;
    ArrayList<Integer> attrsIdx;
    int numAttrs;
    private AttributeStats[] dstats;


    public SubPopMap(int numAttributes, Config config) {
        if (config.datasetName.equals("SNMP")) {
            this.attrsIdx = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ,12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
        } else if (config.datasetName.equals("CAIDA")) {
            this.attrsIdx = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        } else {
            this.attrsIdx = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
        }
        this.numAttrs= numAttributes;
        initSubPop();
    }
    public SubPopMap(ArrayList<Integer> attrsIdx) {
        this.attrsIdx = attrsIdx;
        this.numAttrs= attrsIdx.size();
        initSubPop();
    }

    public LinkedHashMap<boolean[], Integer> subPopulationsReverse = new LinkedHashMap<>();
    public LinkedHashMap<Integer, ArrayList<Integer>> subPopulations2 = new LinkedHashMap<>();

    LinkedHashMap<Integer, Boolean> subPopOneAttr = new LinkedHashMap<>();

    public HashMap<Integer, Boolean> validSubPopMap = new HashMap<>();
    public int cnt = 0;
    public LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> perPredMap = new LinkedHashMap<>();
    public void initSubPop() {

        generateCombinations(attrsIdx, new ArrayList<>(), 0);
        subPopulations2.remove(0);


         perPredMap = splitLinkedHashMap(subPopulations2, attrsIdx.size());
    }
    public void generateCombinations(ArrayList<Integer> attrsIdx, ArrayList<Integer> currentCombination, int start) {
        // Print the current combination
        subPopulations2.put(cnt, currentCombination);
        cnt++;

        ArrayList<Integer> newCombination = new ArrayList<Integer>(currentCombination);
        // Recursively generate combinations with the remaining elements
        for (int i = start; i < attrsIdx.size(); i++) {
            newCombination.add(attrsIdx.get(i));
            generateCombinations(attrsIdx, new ArrayList<>(newCombination), i + 1);
            newCombination.remove(newCombination.size() - 1);
        }
    }

    public int getSubPop(ArrayList<Integer> attrsList) {

        for (int subPop : subPopulations2.keySet()) {
            if (attrsList.equals(subPopulations2.get(subPop))) { // Equals works as they are all sorted.
                return subPop;
            }
        }
        throw new RuntimeException("subPop not found");
    }

    public void dstats(AttributeStats[] dStats) {
        this.dstats = dStats;
    }

    public LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> splitLinkedHashMap(LinkedHashMap<Integer, ArrayList<Integer>> originalMap, int numAttributes) {
        LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> result = new LinkedHashMap<>();

        for (int length = 1; length < numAttributes; length++) {
            LinkedHashMap<Integer, ArrayList<Integer>> newLinkedHashMap = new LinkedHashMap<>();
            result.put(length, newLinkedHashMap);
        }

        for (Integer entry : originalMap.keySet()) {
            int length = originalMap.get(entry).size();
            if (length < numAttributes) {
                result.get(length).put(entry, originalMap.get(entry));
            }
        }

        return result;
    }


}
