package chriniko.kv.datainjector.core;

import chriniko.kv.datatypes.*;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

public final class RecordKeyFileParser {

    public static Map<String /*field name*/, Class<? extends Value<?>>> process(BufferedReader bufferedReader) {

        val m = new HashMap<String /*field name*/, Class<? extends Value<?>>>();

        try (bufferedReader) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {

                String[] splittedLine = line.split(" ");
                String fieldName = splittedLine[0];
                String type = splittedLine[1];

                final Class<? extends Value<?>> clazz;

                switch (type) {
                    case "empty":
                        clazz = EmptyValue.class;
                        break;
                    case "float":
                        clazz = FloatValue.class;
                        break;
                    case "int":
                        clazz = IntValue.class;
                        break;
                    case "string":
                        clazz = StringValue.class;
                        break;
                    case "nested":
                        clazz = NestedValue.class;
                        break;
                    case "list":
                        clazz = ListValue.class;
                        break;

                    default:
                        // list and nested are provided as command line params to DataGenerator.java
                        throw new IllegalStateException("supported data types are: " + SupportedValueTypes.supportedTypes);
                }


                m.put(fieldName, clazz);
            }

            return m;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
