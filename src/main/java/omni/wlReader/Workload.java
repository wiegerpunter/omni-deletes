package omni.wlReader;

import omni.Query;

import java.util.ArrayList;

public class Workload {
    String name;
    public ArrayList<Query> queries = new ArrayList<Query>();
    public Workload(String name) {
        this.name = name;
    }


}
