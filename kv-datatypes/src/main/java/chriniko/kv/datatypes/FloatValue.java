package chriniko.kv.datatypes;

public final class FloatValue extends Value<Float> {

    private final float value;

    public FloatValue(String key, float value) {
        super(key);
        this.value = value;
    }

    @Override
    public Float getValue() {
        return value;
    }


}
