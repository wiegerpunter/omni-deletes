package omni.datasets.Record;

public class RecordUtils {
    public static boolean flexibleEquals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).longValue() == ((Number) b).longValue();
        }

        // Compare numeric to string
        if (a instanceof Number && b instanceof String) {
            return a.toString().equals(b);
        }

        if (a instanceof String && b instanceof Number) {
            return a.equals(b.toString());
        }

        // Fallback to regular equals
        return a.equals(b);
    }

}
