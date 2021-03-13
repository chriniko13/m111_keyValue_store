package chriniko.kv.datainjector.type;

public final class IntValue extends Value<Integer> {

    private final int value;

    public IntValue(String key, int value) {
        super(key);
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
