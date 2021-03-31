package chriniko.kv.datatypes;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParserListValueTest {


    @Test
    void parseWorksAsExpected() {

        // when
        ListValue result = Parser.parseList("{\"name\" : \"nikolaos christidis\" ;" +
                " \"profession\" : \"student\" ;" +
                " \"age\" : 28 ;" +
                " \"grade\" : 8.5 ;" +
                " {} ;" +
                " \"creationTime\" : \"2020/10/5\"}"
        );


        // then
        assertNotNull(result);

        assertEquals(6, result.getValue().size());

        System.out.println("\nresult:");
        result.getValue().forEach(v -> {
            System.out.println("=====");
            System.out.println(v.asString());
        });


        final Iterator<Value<?>> iterator = result.getValue().iterator();
        assertEquals(StringValue.class, iterator.next().getClass());
        assertEquals(StringValue.class, iterator.next().getClass());
        assertEquals(IntValue.class, iterator.next().getClass());
        assertEquals(FloatValue.class, iterator.next().getClass());
        assertEquals(EmptyValue.class, iterator.next().getClass());
        assertEquals(StringValue.class, iterator.next().getClass());

    }


}
