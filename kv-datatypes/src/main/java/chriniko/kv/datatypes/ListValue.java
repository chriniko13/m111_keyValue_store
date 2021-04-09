package chriniko.kv.datatypes;

import java.util.*;

public final class ListValue extends Value<List<Value<?>>> {

    public static final String SEPARATOR = " ; ";

    private final List<Value<?>> values;

    public ListValue() {
        super(null);
        values = new LinkedList<>();
    }

    public static Value<?> of(Value<?>... elems) {
        Objects.requireNonNull(elems, "elems should not be null");
        final ListValue listValue = new ListValue();
        listValue.values.addAll(Arrays.asList(elems));
        return listValue;
    }

    public void add(Value<?> v) {
        values.add(v);
    }

    @Override
    public String asString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[ ");

        for (int i = 0; i < values.size(); i++) {

            Value<?> value = values.get(i);

            if (value instanceof NestedValue) {
                sb.append(value.asString());
            } else if (!(value instanceof EmptyValue)) {
                sb.append(value.asString());
            } else {
                sb.append(value.asString());
            }

            if (i != values.size() - 1) {
                sb.append(SEPARATOR);
            }

        }

        sb.append(" ]");
        return sb.toString();
    }

    @Override
    public String asStringUnwrapped() {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            Value<?> value = values.get(i);

            if (value instanceof NestedValue) {
                sb.append(value.asString());
            } else if (!(value instanceof EmptyValue)) {
                sb.append(value.asString());
            } else {
                sb.append(value.asString());
            }

            if (i != values.size() - 1) {
                sb.append(SEPARATOR);
            }
        }

        return sb.toString();
    }

    @Override
    public List<Value<?>> getValue() {
        return Collections.unmodifiableList(values);
    }


}
