package omni.Record;

import omni.Main;

import java.io.Externalizable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecordCAIDA extends Record implements Externalizable {

    public RecordCAIDA() {
        super();
    }

    public RecordCAIDA(Record r) {
        super();
    }
    Pattern dot;
    public RecordCAIDA(Record r, Pattern dot) {
        super();
        this.dot = dot;
    }


    // frame.number	frame.time_epoch	frame.len	frame.protocols	eth.src	eth.dst	ip.version	ip.hdr_len	ip.ttl	ip.proto
    // ip.flags	ip.len	ip.src	ip.dst	ipv6.src	ipv6.dst

    int frameNumber;
    String frameTimeEpoch = "";
    int frameLen = ph;
    String frameProtocols = "";

    int ethSrc = ph;
    int ethDst = ph;
    int ipVersion = ph;
    int ipHdrLen = ph;
    int ipTtl = ph;
    int ipProto = ph;
    String ipFlags = "";
    int ipLen = ph;
    String ipSrc = "";
    String ipDst = "";
    int ipSrcNet;
    int ipSrcHost;
    int ipDstNet;
    int ipDstHost;
    int ipv6Src = ph;
    int ipv6Dst = ph;
//    tcp.srcport	tcp.dstport	tcp.seq	tcp.ack	tcp.len
    // tcp.window_size_value	tcp.flags	tcp.flags.syn	tcp.flags.ack	tcp.flags.push	tcp.flags.fin
    // udp.srcport	udp.dstport	udp.length	icmp.type	icmp.code	dns.qry.name	dns.resp.name
    // http.request.method	http.host	http.user_agent	http.request.uri	tls.record.version
    // tls.handshake.version	arp.src.proto_ipv4	arp.dst.proto_ipv4
    int tcpSrcPort = ph;
    int tcpDstPort = ph;
    int tcpSeq = ph;
    int tcpAck = ph;
    int tcpLen = ph;
    int tcpWindowSizeValue = ph;
    int tcpFlags = ph;
    int tcpFlagsSyn = ph;
    int tcpFlagsAck = ph;
    int tcpFlagsPush = ph;
    int tcpFlagsFin = ph;
    int udpSrcPort = ph;
    int udpDstPort = ph;
    int udpLength = ph;
    int icmpType = ph;
    int icmpCode = ph;
    int dnsQryName = ph;
    int dnsRespName = ph;
    int httpRequestMethod = ph;
    int httpHost = ph;
    int httpUserAgent = ph;
    int httpRequestUri = ph;
    int tlsRecordVersion = ph;
    int tlsHandshakeVersion = ph;
    int arpSrcProtoIpv4 = ph;
    int arpDstProtoIpv4 = ph;

    private Integer convStringToInt(String s) {
        if (s.equals("")) {
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

//    @Override public void add(String[] a) {
//        //TODO: get rid of replaceAll or make it more memory efficient.
//        // String frameNumberString = a[0].replaceAll("\"", "");
//        frameNumber = convStringToInt(a[0]);
//        frameTimeEpoch = String.valueOf(a[1]);
//        frameLen = convStringToInt(a[2]);
//        frameProtocols = convStringToInt(a[3]);
////        timestamp = convertFrameTime(a[1], a[2]);
//        //timestamp = convertFrameTime(a[1].replaceAll("\"", ""), a[2].replaceAll("\"", ""));
//        if (a.length < 4) {
//            return;
//        }
//        if (a[4].isEmpty()) {
//            ethSrc = ph;
//        } else {
//            ethSrc = convStringToInt(a[3]);
//        }
//        if (a[5].isEmpty()) {
//            ethDst = ph;
//        } else {
//            ethDst = convStringToInt(a[4]);
//        }
//        ipVersion = a[6];
//        ipHdrLen = convStringToInt(a[7]);
//        ipTtl = convStringToInt(a[8]);
//        ipProto = convStringToInt(a[9]);
//        ipFlags = convStringToInt(a[10]);
//        ipLen = convStringToInt(a[11]);
//        ipSrc = a[12];//.replaceAll("\"", "");
//        //int secDot = (ipSrc.substring(ipSrc.indexOf("."))).indexOf(".");
//        if (ipSrc.isEmpty()) {
//            int firstDot = ipSrc.indexOf(".");
//            int secondDot = ipSrc.indexOf(".", firstDot + 1);
//            ipSrcNet = Integer.parseInt(ipSrc.substring(0, secondDot).replaceAll("\\.", ""));
//            ipSrcHost = Integer.parseInt(ipSrc.substring(secondDot).replaceAll("\\.", ""));
//        }
//        ipDst = a[13];//.replaceAll("\"", "");
//        if (ipDst.isEmpty()) {
//            int firstDot = ipDst.indexOf(".");
//            int secondDot = ipDst.indexOf(".", firstDot + 1);
//            ipDstNet = Integer.parseInt(ipDst.substring(0, secondDot).replaceAll("\\.", ""));
//            ipDstHost = Integer.parseInt(ipDst.substring(secondDot).replaceAll("\\.", ""));
//        }
//        ipv6Src = convStringToInt(a[14]);
//        ipv6Dst = convStringToInt(a[15]);
//        tcpSrcPort = convStringToInt(a[16]);
//        tcpDstPort = convStringToInt(a[17]);
//        tcpSeq = convStringToInt(a[18]);
//        tcpAck = convStringToInt(a[19]);
//        tcpLen = convStringToInt(a[20]);
//        tcpWindowSizeValue = convStringToInt(a[21]);
//        tcpFlags = convStringToInt(a[22]);
//        tcpFlagsSyn = convStringToInt(a[23]);
//        tcpFlagsAck = convStringToInt(a[24]);
//        tcpFlagsPush = convStringToInt(a[25]);
//        tcpFlagsFin = convStringToInt(a[26]);
//        udpSrcPort = convStringToInt(a[27]);
//        udpDstPort = convStringToInt(a[28]);
//        udpLength = convStringToInt(a[29]);
//        icmpType = convStringToInt(a[30]);
//        icmpCode = convStringToInt(a[31]);
//        dnsQryName = convStringToInt(a[32]);
//        dnsRespName = convStringToInt(a[33]);
//        httpRequestMethod = convStringToInt(a[34]);
//        httpHost = convStringToInt(a[35]);
//        httpUserAgent = convStringToInt(a[36]);
//        httpRequestUri = convStringToInt(a[37]);
//        tlsRecordVersion = convStringToInt(a[38]);
//        tlsHandshakeVersion = convStringToInt(a[39]);
//        arpSrcProtoIpv4 = convStringToInt(a[40]);
//        arpDstProtoIpv4 = convStringToInt(a[41]);
//
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
    @Override public void add(StringBuilder[] a) {
        //TODO: get rid of replaceAll or make it more memory efficient.
        // String frameNumberString = a[0].replaceAll("\"", "");
//        frameNumber = convStringToInt(a[0]);
//        timestamp = convertFrameTime(String.valueOf(a[1]), String.valueOf(a[2]));


        frameNumber = convStringToInt(a[0]);
        frameTimeEpoch = String.valueOf(a[1]);
        frameLen = convStringToInt(a[2]);
        frameProtocols = String.valueOf(a[3]);
//        timestamp = convertFrameTime(a[1], a[2]);
        //timestamp = convertFrameTime(a[1].replaceAll("\"", ""), a[2].replaceAll("\"", ""));
        if (a.length < 4) {
            return;
        }
        if (a[4].isEmpty()) {
            ethSrc = ph;
        } else {
            ethSrc = convStringToInt(a[3]);
        }
        if (a[5].isEmpty()) {
            ethDst = ph;
        } else {
            ethDst = convStringToInt(a[4]);
        }
        ipVersion = convStringToInt(a[6]);
        ipHdrLen = convStringToInt(a[7]);
        ipTtl = convStringToInt(a[8]);
        ipProto = convStringToInt(a[9]);
        ipFlags = String.valueOf(a[10]);
        ipLen = convStringToInt(a[11]);
        ipSrc = String.valueOf(a[12]);//.replaceAll("\"", "");
        //int secDot = (ipSrc.substring(ipSrc.indexOf("."))).indexOf(".");
        if (ipSrc.isEmpty()) {
            int firstDot = ipSrc.indexOf(".");
            int secondDot = ipSrc.indexOf(".", firstDot + 1);
            ipSrcNet = Integer.parseInt(ipSrc.substring(0, secondDot).replaceAll("\\.", ""));
            ipSrcHost = Integer.parseInt(ipSrc.substring(secondDot).replaceAll("\\.", ""));
        }
        ipDst = String.valueOf(a[13]);//.replaceAll("\"", "");
        if (ipDst.isEmpty()) {
            int firstDot = ipDst.indexOf(".");
            int secondDot = ipDst.indexOf(".", firstDot + 1);
            ipDstNet = Integer.parseInt(ipDst.substring(0, secondDot).replaceAll("\\.", ""));
            ipDstHost = Integer.parseInt(ipDst.substring(secondDot).replaceAll("\\.", ""));
        }
        ipv6Src = convStringToInt(a[14]);
        ipv6Dst = convStringToInt(a[15]);
        tcpSrcPort = convStringToInt(a[16]);
        tcpDstPort = convStringToInt(a[17]);
        tcpSeq = convStringToInt(a[18]);
        tcpAck = convStringToInt(a[19]);
        tcpLen = convStringToInt(a[20]);
        tcpWindowSizeValue = convStringToInt(a[21]);
        tcpFlags = convStringToInt(a[22]);
        tcpFlagsSyn = convStringToInt(a[23]);
        tcpFlagsAck = convStringToInt(a[24]);
        tcpFlagsPush = convStringToInt(a[25]);
        tcpFlagsFin = convStringToInt(a[26]);
        udpSrcPort = convStringToInt(a[27]);
        udpDstPort = convStringToInt(a[28]);
        udpLength = convStringToInt(a[29]);
        icmpType = convStringToInt(a[30]);
        icmpCode = convStringToInt(a[31]);
        dnsQryName = convStringToInt(a[32]);
        dnsRespName = convStringToInt(a[33]);
        httpRequestMethod = convStringToInt(a[34]);
        httpHost = convStringToInt(a[35]);
        httpUserAgent = convStringToInt(a[36]);
        httpRequestUri = convStringToInt(a[37]);
        tlsRecordVersion = convStringToInt(a[38]);
        tlsHandshakeVersion = convStringToInt(a[39]);
        arpSrcProtoIpv4 = convStringToInt(a[40]);
        arpDstProtoIpv4 = convStringToInt(a[41]);
    }
//        frameNumber = Integer.parseInt(a[0].replaceAll("\"", ""));
//
//
//        timestamp = convertFrameTime(a[1].replaceAll("\"", ""), a[2].replaceAll("\"", ""));
//        if (a.length < 4) {
//            return;
//        }
//        if (a[3].equals("")) {
//            ethSrc = ph;
//        } else {
//            ethSrc = Integer.parseInt(a[3].replaceAll("\"", ""));
//        }
//        if (a[4].equals("")) {
//            ethDst = ph;
//        } else {
//            ethDst = Integer.parseInt(a[4].replaceAll("\"", ""));
//        }
//        ipSrc = a[5].replaceAll("\"", "");
//        //int secDot = (ipSrc.substring(ipSrc.indexOf("."))).indexOf(".");
//        if (ipSrc != "") {
//
//            int firstDot = ipSrc.indexOf(".");
//            int secondDot = ipSrc.indexOf(".", firstDot + 1);
//            ipSrcNet = Integer.parseInt(ipSrc.substring(0, secondDot).replaceAll("\\.", ""));
//            ipSrcHost = Integer.parseInt(ipSrc.substring(secondDot).replaceAll("\\.", ""));
//        }
//        ipDst = a[6].replaceAll("\"", "");
//        if (ipDst  != ""){
//            int firstDot = ipDst.indexOf(".");
//            int secondDot = ipDst.indexOf(".", firstDot + 1);
//            ipDstNet = Integer.parseInt(ipDst.substring(0, secondDot).replaceAll("\\.", ""));
//            ipDstHost = Integer.parseInt(ipDst.substring(secondDot).replaceAll("\\.", ""));
//        }
//        ipProto = Integer.parseInt(a[7].replaceAll("\"", ""));

    @Override public void assemble(int id) {
        this.id = id;
        record[0] = timestamp;
        if (Main.numAttributes > 1) {record[1] = (long) frameNumber;}
        if (Main.numAttributes > 2) {record[2] = (long) ethSrc;}
        if (Main.numAttributes > 3) {record[3] = (long) ethDst;}
        if (Main.numAttributes > 4) {
            if (ipSrc.equals("")) {
                record[4] = (long) ph;
            } else {
                record[4] = (long) Long.parseLong(ipSrc.replaceAll("\\.", ""));//.hashCode();}
            }
        }
        if (Main.numAttributes > 5) {record[5] = (long) ipSrcNet;}
        if (Main.numAttributes > 6) {record[6] = (long) ipSrcHost;}
        if (Main.numAttributes > 7) {
            if (ipDst.equals("")) {
                record[7] = phl;
            } else {
                record[7] = (long) Long.parseLong(ipDst.replaceAll("\\.", ""));}
        }
        if (Main.numAttributes > 8) {record[8] = (long) ipDstNet;}
        if (Main.numAttributes > 9) {record[9] = (long) ipDstHost;}
        if (Main.numAttributes > 10) {record[10] = (long) ipProto;}
        assembled = true;
    }


     /*
    int ipTos;
    int ipTtl;
    int ipLen;
    int ipId;
    int ipFlags;
    int ipFragOffset;
    int tcpSrcPort;
    int tcpDstPort;
    int tcpSeq;
    int tcpAck;
    int tcpLen;
    int tcpFlags;
    int tcpWin;
    int tcpUrg;
    int tcpOptions;
    int tcpOptionsLen;
    int udpSrcPort;
    int udpDstPort;
    int udpLen;
    int udpChecksum;
    int icmpType;
    int icmpCode;
    int icmpChecksum;
    int icmpId;
    int icmpSeq;
    int icmpDataLen;
    int icmpData;
     */

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
