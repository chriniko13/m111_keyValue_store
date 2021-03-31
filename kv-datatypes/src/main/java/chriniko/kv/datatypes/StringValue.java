package chriniko.kv.datatypes;

public final class StringValue extends Value<String> {

    private final String value;

    public StringValue(String key, String value) {
        super(key);
        this.value = value;
    }

    @Override
    public String asString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("{")
                .append("\"").append(key)
                .append("\" : ")
                .append("\"").append(getValue()).append("\"")
                .append("}");

        return sb.toString();
    }

    @Override
    public String asStringUnwrapped() {
        final StringBuilder sb = new StringBuilder();

        sb
                .append("\"").append(key)
                .append("\" : ")
                .append("\"").append(getValue()).append("\"");

        return sb.toString();
    }

    @Override
    public String getValue() {
        return value;
    }
}
