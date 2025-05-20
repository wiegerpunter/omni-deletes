package omni.datasets;

import omni.Config;
import omni.Main;
import omni.datasets.Record.Query;

import java.util.ArrayList;
import java.util.Arrays;

public class Parser {
    public static String[] getAttributeName;
    // Class processing queries to Query objects
    // Method mapping attribute name to attribute index
    public int queryId;
    public Parser(int queryId, Config config) {
        this.queryId = queryId;
    }
    static int[] categoricalAttributesSNMP = new int[]{1, 3, 5};

    static int[] categoricalAttributesCAIDA = new int[]{};

    public static int[] getCatAttr(Config config) {
        if (config.datasetName.equals("SNMP"))
            return categoricalAttributesSNMP;
        else if (config.datasetName.equals("CAIDA"))
            return categoricalAttributesCAIDA;
        else
            throw new RuntimeException("Unknown dataset name");
    }
    // Make generic for Main.datasetName == "CAIDA"
    // Make sure not used before initializing query

    public static ArrayList<String> ordinalAttributesSNMP = new ArrayList<String>(Arrays.asList("sysUpTime", "ifSpeed", "ifInOctets", "ifInUcastPkts", "ifInErrors",
            "ifInDiscards", "ifOutOctets", "ifOutUcastPkts", "ifOutErrors", "ifOutDiscards",
            "awcDot11AssociatedStationCount", "awcDot11ReassociatedStationCount", "awcDot11RoamedStationCount",
            "awcDot11DeauthenticateCount", "awcDot11DisassociateCount", "awcFtClientSTASelf", "awcFtBridgeSelf", "awcFtRepeaterSelf"));

    public static ArrayList<String> ordinalAttributesCAIDA = new ArrayList<String>(Arrays.asList("timestamp"));
    public static int attrMap(String attrName, String datasetName) {
        if (datasetName.equals("SNMP")) {

            return switch (attrName) {
                case "timestamp" -> 0;
                case "AP" -> 1;
                case "sysUpTime" -> 2;
                case "sysDescr" -> 3;
                case "ifIndex" -> 4;
                case "ifDescr" -> 5;
                case "ifType" -> 6;
                case "ifSpeed" -> 7;
                case "ifInOctets" -> 8;
                case "ifInUcastPkts" -> 9;
                case "ifInErrors" -> 10;
                case "ifInDiscards" -> 11;
                case "ifOutOctets" -> 12;
                case "ifOutUcastPkts" -> 13;
                case "ifOutErrors" -> 14;
                case "ifOutDiscards" -> 15;
                case "awcDot11AssociatedStationCount" -> 16;
                case "awcDot11ReassociatedStationCount" -> 17;
                case "awcDot11RoamedStationCount" -> 18;
                case "awcDot11DeauthenticateCount" -> 19;
                case "awcDot11DisassociateCount" -> 20;
                case "awcFtClientSTASelf" -> 21;
                case "awcFtBridgeSelf" -> 22;
                case "awcFtRepeaterSelf" -> 23;
                default -> throw new IllegalArgumentException("Unknown attribute name: " + attrName);
            };
        } else if (datasetName.equals("CAIDA")) {
            return switch (attrName) {
                case "timestamp" -> 0;
                case "frameNumber" -> 1;
                case "ethSrc" -> 2;
                case "ethDst" -> 3;
                case "ipSrc" -> 4;
                case "ipSrcNet" -> 5;
                case "ipSrcHost" -> 6;
                case "ipDst" -> 7;
                case "ipDstNet" -> 8;
                case "ipDstHost" -> 9;
                case "ipProto" -> 10;
                default -> throw new IllegalArgumentException("Unknown attribute name: " + attrName);
            };
        } else {
            return switch (attrName) {
                case "0" -> 0;
                case "1" -> 1;
                case "2" -> 2;
                case "3" -> 3;
                case "4" -> 4;
                case "5" -> 5;
                case "6" -> 6;
                case "7" -> 7;
                case "8" -> 8;
                case "9" -> 9;
                case "10" -> 10;
                case "11" -> 11;
                default -> throw new IllegalArgumentException("Unknown attribute name: " + attrName);
            };
        }
    }

    // Also dependent on datasetName
    static String[] attributeNamesSNMP = new String[]{"timestamp", "AP", "sysUpTime", "sysDescr",
            "ifIndex", "ifDescr", "ifType", "ifSpeed", "ifInOctets",
            "ifInUcastPkts", "ifInErrors", "ifInDiscards", "ifOutOctets",
            "ifOutUcastPkts", "ifOutErrors", "ifOutDiscards", "awcDot11AssociatedStationCount",
            "awcDot11ReassociatedStationCount", "awcDot11RoamedStationCount", "awcDot11DeauthenticateCount",
            "awcDot11DisassociateCount", "awcFtClientSTASelf", "awcFtBridgeSelf", "awcFtRepeaterSelf"};

    static String[] attributeNamesCAIDA = new String[]{"timestamp", "frameNumber", "ethSrc", "ethDst", "ipSrc",
            "ipSrcNet", "ipSrcHost", "ipDst", "ipDstNet", "ipDstHost", "ipProto"};
    static String[] attributeNamesSynth = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10","11","12",
    "13","14","15","16","17","18","19","20","21","22","23"};

    public static String getAttributeName(int i, String datasetName) {
        if (datasetName.equals("SNMP")) {
            return attributeNamesSNMP[i];
        } else if (datasetName.equals("CAIDA")) {
            return attributeNamesCAIDA[i];
        } else {
            return String.valueOf(i);
        }
    }


    public void setPredicate(Query q, String predicate, String datasetName) {
        // Parse query string into Query object
        // Query string format: "attrName op value"
        // Example: "ifInOctets > 1000"
        String[] queryParts = predicate.split(" ");
        String attrName = queryParts[0];
        String op = queryParts[1];
        String value = queryParts[2];
        int attrIndex = attrMap(attrName, datasetName);
        q.addPredicate(attrIndex, op, value);
    }

    public Query parse(String query, String datasetName) {
        // Example: "timestamp > 1000 AND ifInOctets > 1000"
        String[] queryParts = query.split(" (A|a)(N|n)(D|d) ");
        Query q = new Query(queryId);
        q.predicates = query;
        for (String queryPart : queryParts) {
            if (!queryPart.equals("AND")) {
                setPredicate(q, queryPart, datasetName);
            }
        }
        return q;
    }

}
