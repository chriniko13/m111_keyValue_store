package chriniko.kv.datatypes;

import java.util.LinkedHashSet;
import java.util.Set;

public final class StringValue extends Value<String> {

    public static final Set<String> NOT_ALLOWED_CHARS;
    static {
        NOT_ALLOWED_CHARS = new LinkedHashSet<>();
        NOT_ALLOWED_CHARS.add(ListValue.SEPARATOR.trim());
    }

    private final String value;

    public StringValue(String key, String value) {
        super(key);

        for (String notAllowedChar : NOT_ALLOWED_CHARS) {
            if (value.contains(notAllowedChar)) {
                throw new IllegalArgumentException("value provided contains a not allowed char " + notAllowedChar);
            }
        }

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
