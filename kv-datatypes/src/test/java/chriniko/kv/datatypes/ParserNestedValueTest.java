package chriniko.kv.datatypes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserNestedValueTest {

    @Test
    void parseWorksAsExpected() {


        // when
        NestedValue result = Parser.parseNested("{\"nested\" : {\"n1\" : {\"n2\" : {\"n3\" : {\"n4\" : {\"n5\" : {\"n6\" : {\"str3\" : \"6\"}}}}}}}}");


        // then
        Assertions.assertNotNull(result);

        assertEquals(6, result.maxDepth());


        assertEquals("nested", result.getKey());
        assertEquals(NestedValue.class, result.getValue().getClass());

        NestedValue v = (NestedValue) result.getValue();
        for (int i = 1; i <= 6; i++) {

            assertEquals("n" + i, v.getKey());

            if (i != 6) {
                assertTrue(v.getValue() instanceof NestedValue);
                v = (NestedValue) v.getValue();
            }
        }

        // now just grab the last one
        Value<?> value = v.getValue();
        assertTrue(value instanceof StringValue);

        StringValue stringValue = (StringValue) value;
        assertEquals("str3", stringValue.getKey());
        assertEquals("6", stringValue.getValue());

    }


    //TODO...
    @Test
    void parseWorksAsExpected2() {


        // when
        NestedValue result = Parser.parseNested("{\"n1\" : {\"str1\" : \"4\" ; \"n2\" : {\"int2\" : 2} ; \"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}} ; \"n5\" : {\"float2\" : 2.34} ; \"n71\" : {\"n72\" : {\"float3\" : 3.34}}}}");


        // then
        Assertions.assertNotNull(result);

        assertEquals(6, result.maxDepth());

    }


    //TODO...
    @Test
    void parseWorksAsExpected3() {


        // when
        NestedValue result = Parser.parseNested("{\"n1\" : {\"str1\" : \"4\" ; \"n2\" : {\"int2\" : 2} ; \"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}} ; \"n5\" : {\"float2\" : 2.34} ; \"n71\" : {\"n72\" : {}}}}");


        // then
        Assertions.assertNotNull(result);

        assertEquals(6, result.maxDepth());

    }


    //TODO...
    @Test
    void parseWorksAsExpected4() {


        // when
        NestedValue result = Parser.parseNested("{\"n1\" : {\"str1\" : \"4\" ; \"n2\" : {\"int2\" : 2} ; \"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}} ; \"n5\" : {\"float2\" : 2.34} ; \"n71\" : {\"n72\" : {\"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}} ; \"f1\" : {\"f2\" : {\"f3\" : {\"f4\" : {\"fString\" : \"fValue\"}}}}}}}}");


        // then
        Assertions.assertNotNull(result);

        assertEquals(6, result.maxDepth());

    }

}
