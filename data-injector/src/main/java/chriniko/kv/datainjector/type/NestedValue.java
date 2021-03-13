package chriniko.kv.datainjector.type;


public final class NestedValue extends Value<Value<?>> {

    private final Value<?> value;

    public NestedValue(String key, Value<?> value) {
        super(key);
        this.value = value;
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

    public void asStringHelper(StringBuilder sb) {
        sb.append("\"").append(this.key).append("\" : ");

        Value<?> value = getValue();

        boolean opened = false;
        while (value != null) {

            // construct string builder correctly
            if (value instanceof NestedValue) {

                sb.append("{").append("\"").append(value.key).append("\" : ");
                opened = true;

            } else {
                sb.append(value.asString());

                if (opened) {
                    sb.append("}");
                    opened = false;
                }
            }

            // traverse
            if (value instanceof NestedValue) {
                value = ((NestedValue) value).getValue();
            } else {
                value = null;
            }

        } // while.
    }
}
