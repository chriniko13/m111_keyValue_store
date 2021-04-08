package chriniko.kv.datatypes.parser;

import chriniko.kv.datatypes.*;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.parser.DatatypesParser;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DatatypesParserListValueTest {


    @Test
    void parseWorksAsExpected() {

        // when
        ListValue result = DatatypesParser.parseList("{\"name\" : \"nikolaos christidis\" ;" +
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



    @Test
    void parseWorksAsExpected2() {

        // when
        ListValue result = DatatypesParser.parseList("{ \"name\" : \"nikolaos christidis\" " +
                "; \"profession\" : \"student\" " +
                "; \"age\" : 28 ; \"grade\" : 8.5 " +
                "; { } " +
                "; \"creationTime\" : \"2020/10/5\" " +
                "; \"n1\" : { \"n2\" : { \"n3\" : { \"s\" : \"v\" } } } }"
        );


        // then
        assertNotNull(result);

        assertEquals(7, result.getValue().size());

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


        Value<?> value = iterator.next();
        assertEquals(NestedValue.class, value.getClass());
        NestedValue nestedValue = (NestedValue) value;

        Set<String> keys = nestedValue.allKeys();
        System.out.println("allKeys: " + keys);
        assertEquals(Set.of("n1", "n2", "n3", "s"), keys);

        Set<Value<?>> values = nestedValue.allFlatValues();
        System.out.println("allFlatValues: " + values);
        assertEquals(1, values.size());

        Value<?> v = values.iterator().next();
        assertEquals("s", v.getKey());
        assertEquals("v", v.getValue());
    }


    @Test
    void parseWorksAsExpected3() {

        // when
        try {
           DatatypesParser.parseList("{\"n1\" : {\"str1\" : \"4\" " +
                    "; \"n2\" : {\"int2\" : 2} " +
                    "; \"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}} " +
                    "; \"n5\" : {\"float2\" : 2.34} " +
                    "; \"n71\" : {\"n72\" : {\"float3\" : 3.34 ; \"float4\" : 4.34}}}}"
            );
            fail();

        } catch (ParsingException e) {

            // then
            assertEquals("not valid list value to be parsed - unbalanced parenthesis", e.getMessage());
        }

    }
}
