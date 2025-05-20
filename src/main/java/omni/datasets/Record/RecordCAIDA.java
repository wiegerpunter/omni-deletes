package omni.datasets.Record;

import omni.Config;

import java.io.Externalizable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecordCAIDA extends Record implements Externalizable {

    public RecordCAIDA() {
        super();
    }

    public RecordCAIDA(Record r) {
        super();
        this.id = r.id;
        this.record = r.record;
        this.timestamp = r.timestamp;
        this.assembled = r.assembled;
        this.ph = r.ph;
        this.phl = r.phl;
    }

    Pattern dot;
    Config config;
    public RecordCAIDA(Record r, Pattern dot, Config config) {
        super();
        this.dot = dot;
        this.config = config;
    }

    int frameNumber;
    //long frameTime;
    int ethSrc = ph;
    int ethDst = ph;
    String ipSrc = "";
    String ipDst = "";
    int ipSrcNet;
    int ipSrcHost;
    int ipDstNet;
    int ipDstHost;
    int ipProto = ph;

    private Integer convStringToInt(String s) {
        if (s.isEmpty()) {
            return ph;
        }
        return Integer.parseInt(s);
    }
    private Integer convStringToInt(StringBuilder s) {
        if (s.toString().isEmpty()) {
            return ph;
        }
        return Integer.parseInt(String.valueOf(s));
    }
//
//    @Override public void add(String[] a) {
//        //TODO: get rid of replaceAll or make it more memory efficient.
//        // String frameNumberString = a[0].replaceAll("\"", "");
//        frameNumber = convStringToInt(a[0]);
//        timestamp = convertFrameTime(a[1], a[2]);
//
//        //timestamp = convertFrameTime(a[1].replaceAll("\"", ""), a[2].replaceAll("\"", ""));
//        if (a.length < 4) {
//            return;
//        }
//        if (a[3].equals("")) {
//            ethSrc = ph;
//        } else {
//            ethSrc = convStringToInt(a[3]);
//        }
//        if (a[4].equals("")) {
//            ethDst = ph;
//        } else {
//            ethDst = convStringToInt(a[4]);
//        }
//        ipSrc = a[5];//.replaceAll("\"", "");
//        //int secDot = (ipSrc.substring(ipSrc.indexOf("."))).indexOf(".");
//        if (ipSrc != "") {
//
//            int firstDot = ipSrc.indexOf(".");
//            int secondDot = ipSrc.indexOf(".", firstDot + 1);
//            ipSrcNet = Integer.parseInt(ipSrc.substring(0, secondDot).replaceAll("\\.", ""));
//            ipSrcHost = Integer.parseInt(ipSrc.substring(secondDot).replaceAll("\\.", ""));
//        }
//        ipDst = a[6];//.replaceAll("\"", "");
//        if (ipDst != "") {
//            int firstDot = ipDst.indexOf(".");
//            int secondDot = ipDst.indexOf(".", firstDot + 1);
//            ipDstNet = Integer.parseInt(ipDst.substring(0, secondDot).replaceAll("\\.", ""));
//            ipDstHost = Integer.parseInt(ipDst.substring(secondDot).replaceAll("\\.", ""));
//        }
//        ipProto = convStringToInt(a[7]);
//    }


    public String replaceDot(String s) {
        Matcher m = dot.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return sb.toString();
    }
    @Override public void add(String[] a) {
        frameNumber = convStringToInt(a[0]);
        timestamp = convertFrameTime(String.valueOf(a[1]), String.valueOf(a[2]));

        //timestamp = convertFrameTime(a[1].replaceAll("\"", ""), a[2].replaceAll("\"", ""));
        if (a.length < 4) {
            return;
        }
        if (a[3].isEmpty()) {
            ethSrc = ph;
        } else {
            ethSrc = convStringToInt(a[3]);
        }
        if (a[4].isEmpty()) {
            ethDst = ph;
        } else {
            ethDst = convStringToInt(a[4]);
        }
        ipSrc = String.valueOf(a[5]);//.replaceAll("\"", "");
        //int secDot = (ipSrc.substring(ipSrc.indexOf("."))).indexOf(".");
        if (!Objects.equals(ipSrc, "")) {

            int firstDot = ipSrc.indexOf(".");
            int secondDot = ipSrc.indexOf(".", firstDot + 1);
            ipSrcNet = Integer.parseInt(replaceDot(ipSrc.substring(0, secondDot)));//.replaceAll("\\.", ""));
            ipSrcHost = Integer.parseInt(replaceDot(ipSrc.substring(secondDot)));//.replaceAll("\\.", ""));
        }
        ipDst = String.valueOf(a[6]);//.replaceAll("\"", "");
        if (!Objects.equals(ipDst, "")) {
            int firstDot = ipDst.indexOf(".");
            int secondDot = ipDst.indexOf(".", firstDot + 1);
            ipDstNet = Integer.parseInt(replaceDot(ipDst.substring(0, secondDot)));
            ipDstHost = Integer.parseInt(replaceDot(ipDst.substring(secondDot)));
        }
        ipProto = convStringToInt(a[7]);
    }

    @Override public void assemble(int id) {
        this.id = id;
        this.record = new long[config.numAttributes];
        record[0] = timestamp;
        long[] attributeValues= {
                frameNumber, ethSrc, ethDst,
                ipSrc.isEmpty() ? (long) ph :
                        (long) Long.parseLong(ipSrc.replaceAll("\\.", "")),
                ipSrcNet, ipSrcHost,
                ipDst.isEmpty() ? phl :
                        (long) Long.parseLong(ipDst.replaceAll("\\.",
                        "")), ipDstNet, ipDstHost,
                ipProto
        };
        for (int i = 0; i < attributeValues.length && i + 1 < config.numAttributes; i++) {
            record[i + 1] = attributeValues[i];
        }
        assembled = true;
    }

    public long convertFrameTime(String a1, String a2) {
        // Converts string in format feb 17 2011 13:59:04.112376000 CET" to timestamp
        String[] b = a1.split(" ");
        String month = b[0];
        int day = Integer.parseInt(b[1]);
        String[] c = a2.split(" ");
        int year = Integer.parseInt(c[1]);
        String[] d = c[2].split(":");
        int hour = Integer.parseInt(d[0]);
        int minute = Integer.parseInt(d[1]);
        String[] e = d[2].split("\\.");
        int second = Integer.parseInt(e[0]);
        int millisecond = Integer.parseInt(e[1]);
        String zone = c[3];

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int monthInt = 0;
        for (int x = 0; x < months.length; x++) {
            if (months[x].equals(month)) {
                monthInt = x + 1;
                break;
            }
        }
        return (year * 10000000000L) + (monthInt * 100000000L) + (day * 1000000L) + (hour * 10000L) + (minute * 100L) + second;
    }


}