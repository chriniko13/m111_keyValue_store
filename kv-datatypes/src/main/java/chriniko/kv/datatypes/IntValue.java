package chriniko.kv.datatypes;

public final class IntValue extends FlatValue<Integer> {

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
