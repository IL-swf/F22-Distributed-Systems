import java.util.HashMap;
import java.util.Map;

public class Control {
    public static void main(String[] args) throws Exception {

        HashMap<Integer, String> women = new HashMap<>();
        women.put(30000, "Alice");
        women.put(30001, "Betty");
        women.put(30002, "Claire");
        women.put(30003, "Donna");
        women.put(30004, "Ellie");

        HashMap<Integer, String> men = new HashMap<>();
        men.put(40000, "Aaron");
        men.put(40001, "Bill");
        men.put(40002, "Charles");
        men.put(40003, "Dennis");
        men.put(40004, "Elijah");

        Environment environment = new Environment(50000, women, men);
        environment.start();
    }
}
