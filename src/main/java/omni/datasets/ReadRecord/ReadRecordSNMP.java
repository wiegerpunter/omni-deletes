package omni.datasets.ReadRecord;

import omni.Config;
import omni.datasets.Parser;

import java.io.*;
import java.util.Arrays;

import static java.lang.Integer.parseInt;

public class ReadRecordSNMP extends ReadRecord implements Externalizable {
    Config config;

    public ReadRecordSNMP() {
    }

    public ReadRecordSNMP(ReadRecord r, Config config) {
        super();
        this.config = config;
    }

    long timestamp = phl;
    transient int AP = ph;
    transient long sysUpTime = phl;
    transient int sysDescr = ph;


    public boolean sysAdd(String[] a) {
        if (a.length > 5 || a.length < 4) {
            System.err.println("Warning: sysAdd() in Record.java");
            System.err.println(Arrays.toString(a));
            return false;
            //System.exit(1);
        }
        if (a.length == 5) {
            this.sysDescr = a[4].hashCode();
        }
        for (String s : a) {
            if (s.equals(Integer.toString(ph))) {
                System.err.println("Error found for " + Arrays.toString(a));
                throw new IllegalArgumentException("Warning: c1Add() in Record.java");
            }
        }
        this.timestamp = Long.parseLong(a[1]);
        this.AP = a[2].hashCode();
        this.sysUpTime = Long.parseLong(a[3]);
        return true;
    }

    transient int ifIndex = ph; //keep
    transient int ifDescr = ph; //keep
    transient int ifType = ph; //keep
    transient int ifSpeed = ph;
    transient long ifInOctets = phl; // keep
    transient int ifInUcastPkts = ph;
    transient int ifInErrors = ph;
    transient int ifInDiscards = ph;
    transient long ifOutOctets = phl;
    transient int ifOutUcastPkts = ph;
    transient int ifOutErrors = ph;
    transient int ifOutDiscards = ph;
    transient int awcDot11AssociatedStationCount = ph;
    transient int awcDot11ReassociatedStationCount = ph;
    transient int awcDot11RoamedStationCount = ph;
    transient int awcDot11DeauthenticateCount = ph;
    transient int awcDot11DisassociateCount = ph;
    transient int awcFtClientSTASelf = ph;
    transient int awcFtBridgeSelf = ph;
    transient int awcFtRepeaterSelf = ph;
    public boolean  ifAdd(String[] a) {
        if (a.length != 23) {
            //System.err.println("Error: ifAdd() in Record.java");
            //System.err.println(Arrays.toString(a));
            return false;
            //System.exit(1);
        }
        for (String s : a) {
            if (s.equals(Integer.toString(ph))) {
                System.err.println("Error found for " + Arrays.toString(a));
                throw new IllegalArgumentException("Error: c1Add() in Record.java");
            }
        }
        this.ifIndex = parseInt(a[3]);
        this.ifDescr = a[4].hashCode();
        this.ifType = parseInt(a[5]);
        this.ifSpeed = parseInt(a[6]);
        this.ifInOctets = Long.parseLong(a[7]);
        this.ifInUcastPkts = parseInt(a[8]);
        this.ifInErrors = parseInt(a[9]);
        this.ifInDiscards = parseInt(a[10]);
        this.ifOutOctets = Long.parseLong(a[11]);
        this.ifOutUcastPkts = parseInt(a[12]);
        this.ifOutErrors = parseInt(a[13]);
        this.ifOutDiscards = parseInt(a[14]);
        this.awcDot11AssociatedStationCount = nullHandler(a[15]);
        this.awcDot11ReassociatedStationCount = nullHandler(a[16]);
        this.awcDot11RoamedStationCount = nullHandler(a[17]);
        this.awcDot11DeauthenticateCount = nullHandler(a[18]);
        this.awcDot11DisassociateCount = nullHandler(a[19]);
        this.awcFtClientSTASelf = nullHandler(a[20]);
        this.awcFtBridgeSelf = nullHandler(a[21]);
        this.awcFtRepeaterSelf = parseInt(a[22]);
        return true;
    }

    public int nullHandler(String a) {
        if (a.equals(""))
            return ph;
        else
            return parseInt(a);
    }
    @Override
    public void assemble(int id) {
        this.id = id;
        this.record = new long[config.numAttributes];
        String datasetName = "SNMP";
        this.record[Parser.attrMap("timestamp", datasetName)] = timestamp;

        String[] attributeNames = {
            "AP", "sysUpTime", "sysDescr", "ifIndex", "ifDescr", "ifType", "ifSpeed",
            "ifInOctets", "ifInUcastPkts", "ifInErrors", "ifInDiscards", "ifOutOctets",
            "ifOutUcastPkts", "ifOutErrors", "ifOutDiscards", "awcDot11AssociatedStationCount",
            "awcDot11ReassociatedStationCount", "awcDot11RoamedStationCount",
            "awcDot11DeauthenticateCount", "awcDot11DisassociateCount",
            "awcFtClientSTASelf", "awcFtBridgeSelf", "awcFtRepeaterSelf"
        };

        long[] attributeValues = {
            AP, sysUpTime, sysDescr, ifIndex, ifDescr,
            ifType, ifSpeed, ifInOctets, ifInUcastPkts,
            ifInErrors, ifInDiscards, ifOutOctets, ifOutUcastPkts,
            ifOutErrors, ifOutDiscards, awcDot11AssociatedStationCount,
            awcDot11ReassociatedStationCount, awcDot11RoamedStationCount,
            awcDot11DeauthenticateCount, awcDot11DisassociateCount,
            awcFtClientSTASelf, awcFtBridgeSelf, awcFtRepeaterSelf
        };
        for (int i = 0; i < attributeNames.length && i + 1 < config.numAttributes; i++) {
            this.record[Parser.attrMap(attributeNames[i], datasetName)] = (long) attributeValues[i];
        }
        this.assembled = true;
    }

    boolean seenSys = true;
    boolean seenIf = true;

    @Override public void add(String[] a) {
        //System.out.println("Record.add() " + a[0]);
        if (a[0].equals("sys")) {
            seenSys =  sysAdd(a);
        } else if (a[0].equals("if") && seenSys) {
            seenIf = ifAdd(a);
        } else if (!('#' == a[0].charAt(0) || 's' == a[0].charAt(0) || 'i' == a[0].charAt(0) || 'c' == a[0].charAt(0))) {
            System.err.println("Unknown record type " + a[0]);
        }
        if (seenSys && seenIf) {
            assemble(id);
        }
    }

    public String[] writeRecord() {
        String[] info = new String[config.numAttributes + 2];
        info[0] = String.valueOf(id);
        info[1] = String.valueOf(timestamp);
        for (int i = 0; i < config.numAttributes; i++) {
            info[i + 2] = String.valueOf(record[i]);
        }
        return info;
    }

}
