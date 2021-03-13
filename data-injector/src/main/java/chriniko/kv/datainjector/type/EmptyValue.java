package chriniko.kv.datainjector.type;

public final class EmptyValue extends Value<Void> {
    public EmptyValue() {
        super(null);
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
