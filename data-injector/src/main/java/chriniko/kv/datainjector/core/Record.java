package chriniko.kv.datainjector.core;

import chriniko.kv.datainjector.type.Value;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class Record {

    private final String key;
    private final Value<?> value;

    @Override
    public String toString() {
        return "\"" + key + "\" : " + value.asString();
    }
}
