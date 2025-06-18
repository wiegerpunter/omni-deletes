package omni.test.standaloneAlphaMinwise;

public class Event {
    public int setIndex; // which set (for intersection: -1 means all sets)
    public int value;
    public int op; // 1 for insert, -1 for delete

    public Event(int setIndex, int value, int op) {
        this.setIndex = setIndex;
        this.value = value;
        this.op = op;
    }
}