package chriniko.kv.datatypes;

public final class EmptyValue extends Value<Void> {
    public EmptyValue() {
        super(null);
    }

    public EmptyValue(String key) {
        super(key);
    }

    @Override
    public String asString() {
        return "{}";
    }

    @Override
    public String asStringUnwrapped() {
        return "";
    }

    @Override
    public Void getValue() {
        return null;
    }


}
