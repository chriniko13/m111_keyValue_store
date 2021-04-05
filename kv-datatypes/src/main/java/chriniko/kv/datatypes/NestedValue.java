package chriniko.kv.datatypes;


import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

public final class NestedValue extends Value<Value<?>> {

    @Setter
    private Value<?> value;

    public NestedValue(String key, Value<?> value) {
        super(key);
        this.value = value;
    }

    public NestedValue(String key) {
        super(key);
        this.value = null;
    }

    public static NestedValue combine(String key, NestedValue... ns) {
        if (ns == null || ns.length < 1) {
            throw new IllegalArgumentException("ns should has at least 1 record (size >= 1)");
        }

        NestedValue nv = new NestedValue(key);
        nv.setValue(ns[0]);


        if (ns.length > 1) {
            ns[0].setValue(ns[1]);

            for (int i = 1; i < ns.length - 1; i++) {
                ns[i].setValue(ns[i + 1]);
            }
        }

        return nv;
    }

    @Override
    public Value<?> getValue() {
        return value;
    }


    @Override
    public String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        asStringHelper(sb);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String asStringUnwrapped() {
        StringBuilder sb = new StringBuilder();
        asStringHelper(sb);
        return sb.toString();
    }

    public int maxDepth() {

        Value<?> value = getValue();
        int res = 0;

        final Queue<Value<?>> q = new LinkedList<>();

        boolean startOfNesting = false;
        int currentMax = -1;

        while (value != null) {

            if (value instanceof NestedValue) {

                if (!startOfNesting) startOfNesting = true;
                res++;

                value = ((NestedValue) value).getValue();


            } else if (value instanceof ListValue) {

                if (startOfNesting) {
                    startOfNesting = false;
                    currentMax = Math.max(res, currentMax);
                    res = 0;
                }

                ListValue lv = (ListValue) value;
                q.addAll(lv.getValue());

                value = q.poll();

            } else {

                if (startOfNesting) {
                    startOfNesting = false;
                    currentMax = Math.max(res, currentMax);
                    res = 0;
                }

                if (q.isEmpty()) {
                    value = null;
                } else {
                    value = q.poll();
                }

            }

        }

        return currentMax;
    }

    public void asStringHelper(StringBuilder sb) {
        sb.append("\"").append(this.key).append("\" : ");

        Value<?> v = getValue();

        int opened = 0;
        while (v != null) {

            // construct string builder correctly
            if (v instanceof NestedValue) {

                sb.append("{").append("\"").append(v.key).append("\" : ");

                opened++;

            } else {
                sb.append(v.asString());

                if (opened > 0) {
                    sb.append("}");
                    opened--;
                }
            }

            // traverse
            if (v instanceof NestedValue) {
                v = ((NestedValue) v).getValue();
            } else {
                v = null;
            }

        } // while.


        // Note: add remaining closing parenthesis: }
        while (opened > 0) {
            sb.append("}");
            opened--;
        }
    }
}
