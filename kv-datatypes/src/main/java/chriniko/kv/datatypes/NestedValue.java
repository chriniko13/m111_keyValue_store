package chriniko.kv.datatypes;


import lombok.Setter;

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

            for (int i=1; i<ns.length - 1; i++) {
                ns[i].setValue(ns[i+1]);
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

    public int depth() {

        Value<?> value = getValue();
        int res = 0;

        while (value != null) {

            if (value instanceof NestedValue) {
                res++;
                value = ((NestedValue) value).getValue();
            } else {
                value = null;
            }

        }

        return res;
    }

    public void asStringHelper(StringBuilder sb) {
        sb.append("\"").append(this.key).append("\" : ");

        Value<?> value = getValue();

        int opened = 0;
        while (value != null) {

            // construct string builder correctly
            if (value instanceof NestedValue) {

                sb.append("{").append("\"").append(value.key).append("\" : ");
                opened++;

            } else {
                sb.append(value.asString());

                if (opened > 0) {
                    sb.append("}");
                    opened--;
                }
            }

            // traverse
            if (value instanceof NestedValue) {
                value = ((NestedValue) value).getValue();
            } else {
                value = null;
            }

        } // while.


        // Note: add remaining closing parenthesis: }
        while (opened > 0) {
            sb.append("}");
            opened--;
        }
    }
}
