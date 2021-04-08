package chriniko.kv.datatypes.infra;

import chriniko.kv.datatypes.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum SupportedValueTypes {

    EMPTY(EmptyValue.class, "empty"),
    FLOAT(FloatValue.class, "float"),
    INT(IntValue.class, "int"),
    NESTED(NestedValue.class, "nested"),
    STRING(StringValue.class, "string"),
    LIST(ListValue.class, "list");

    @Getter
    private final Class<? extends Value<?>> clazzSupport;

    @Getter
    private final String type;


    public static final Set<String> supportedTypes;

    static {
        supportedTypes = Arrays.stream(SupportedValueTypes.values())
                .map(SupportedValueTypes::getType)
                .collect(Collectors.toSet());
    }
}
