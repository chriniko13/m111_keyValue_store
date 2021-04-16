package chriniko.kv.broker.api;

import java.util.LinkedList;

public class QueryKey {

    private final LinkedList<String> keysPath = new LinkedList<>();

    private QueryKey() {
    }

    public static QueryKey build(String... elems) {

        QueryKey queryKey = new QueryKey();

        for (String elem : elems) {
            queryKey.addKeyToPath(elem);
        }

        return queryKey;
    }

    private void addKeyToPath(String s) {
        keysPath.add(s);
    }

    public String asString() {
        return String.join("~>", keysPath);
    }
}
