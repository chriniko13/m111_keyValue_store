package chriniko.kv.server;

import chriniko.kv.datatypes.FlatValue;
import chriniko.kv.datatypes.ListValue;
import chriniko.kv.datatypes.NestedValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.trie.TrieEntry;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class KvRecord implements TrieEntry<Value<?>> {

    private final String key;
    private final Value<?> value;

    private final Instant createTime;
    private Instant updateTime;

    public KvRecord(String key, Value<?> value) {
        this.key = key;
        this.value = value;

        this.createTime = Instant.now();
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public void setKey(String k) {
        if (!key.equals(k)) {
            throw new IllegalStateException("KvRecord (k != key)");
        }
    }

    @Override
    public Value<?> value() {
        return value;
    }

    @Override
    public Instant createTime() {
        return createTime;
    }

    @Override
    public Instant updateTime() {
        return updateTime;
    }

    @Override
    public void triggerUpdateTime() {
        updateTime = Instant.now();
    }


    private final Map<String /*path, eg: address.street*/, Value<?>> indexedContents = new LinkedHashMap<>();

    //todo fix it...
    public void indexContents() {
        indexedContents.clear();

        if (!(value instanceof ListValue) && !(value instanceof NestedValue)) {
            // if flat value then it is easy
            FlatValue<?> flatValue = (FlatValue<?>) value;

            indexedContents.put(flatValue.getKey(), flatValue);

        } else {

            final ArrayDeque<Value<?>> stack = new ArrayDeque<>();
            stack.push(value);

            final LinkedList<String> pathCreator = new LinkedList();

            while (!stack.isEmpty()) {

                final Value<?> current = stack.pop();

                if (current instanceof NestedValue) {

                    pathCreator.add(current.getKey());

                    String key = String.join(".", pathCreator);
                    indexedContents.put(key, current);


                    Object next = current.getValue();



                } else if (current instanceof ListValue) {


                    //todo...

                } else if (current instanceof FlatValue) {

                } else {
                    throw new IllegalStateException();
                }

            }

        }


    }
}
