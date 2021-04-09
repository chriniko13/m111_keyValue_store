package chriniko.kv.datatypes.parser;

import chriniko.kv.datatypes.NestedValue;
import chriniko.kv.datatypes.StringValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class DatatypesParserNestedValueTest {

    @Test
    void parseWorksAsExpected() {


        // when
        NestedValue result = DatatypesParser.parseNested("{ \"nested\" : { \"n1\" : { \"n2\" : { \"n3\" : { \"n4\" : { \"n5\" : { \"n6\" : { \"str3\" : \"6\" } } } } } } } }");


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


        // when
        result = DatatypesParser.parseNested("{ \"nested\" : { \"n1\" : { \"n2\" : { \"n3\" : { \"n4\" : { \"n5\" : { \"n6\" : { \"str3\" : 1 } } } } } } } }");

        // then
        assertEquals(Set.of(1), result.allFlatValues().stream().map(Value::getValue).collect(Collectors.toSet()));

        // when
        result = DatatypesParser.parseNested("{ \"nested\" : { \"n1\" : { \"n2\" : { \"n3\" : { \"n4\" : { \"n5\" : { \"n6\" : { \"str3\" : 1.13 } } } } } } } }");

        // then
        assertEquals(Set.of(1.13F), result.allFlatValues().stream().map(Value::getValue).collect(Collectors.toSet()));

    }


    @Test
    void parseWorksAsExpected2() {


        // when
        try {
            DatatypesParser.parseNested("{\"n1\" : {\"fn3\" : {\"nf4\" : {\"strTemp\" : \"allGood\"}} ; \"n2\" : {\"int2\" : 2} ; \"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}} ; \"n5\" : {\"float2\" : 2.34} ; \"n71\" : {\"n72\" : {\"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}} ; \"f1\" : {\"f2\" : {\"f3\" : {\"f4\" : {\"fString\" : \"fValue\"}}}}}}}}");
            fail();
        } catch (ParsingException e) {

            // then
            assertEquals("provided input is a complex case (nested type & listed type), please use parser created from ANTLR4", e.getMessage());
        }

    }


    @Test
    void parseWorksAsExpected2_1() {


        // when
        try {
            DatatypesParser.parseNested("{\"n1\" : {\"fn3\" : {\"nf4\" : {\"strTemp\" : \"allGood\"}} ; \"n2\" : {\"int2\" : 2} ; \"n3\" : {\"gn3\" : {\"gn4\" : {\"gstrTemp\" : \"allGood\"}} ; \"gf1\" : {\"gf2\" : {\"gf3\" : {\"gf4\" : {\"gfString\" : \"gfValue\"}}}}} ; \"n5\" : {\"float2\" : 2.34} ; \"n71\" : {\"n72\" : {\"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}} ; \"f1\" : {\"f2\" : {\"f3\" : {\"f4\" : {\"fString\" : \"fValue\"}}}}}}}}");
            fail();
        } catch (ParsingException e) {

            // then
            assertEquals("provided input is a complex case (nested type & listed type), please use parser created from ANTLR4", e.getMessage());
        }

    }


    @Test
    void parseWorksAsExpected3() {


        // when
        try {
            DatatypesParser.parseNested("{\"n1\" : {\"str1\" : \"4\" ; \"n2\" : {\"int2\" : 2} ; \"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}} ; \"n5\" : {\"float2\" : 2.34} ; \"n71\" : {\"n72\" : {}}}}");
            fail();
        } catch (ParsingException e) {

            // then
            assertEquals("provided input is a complex case (nested type & listed type), please use parser created from ANTLR4", e.getMessage());
        }

    }


    @Test
    void parseWorksAsExpected4() {

        // when
        try {
            DatatypesParser.parseNested("{\"n1\" : {\"str1\" : \"4\" ; \"n2\" : {\"int2\" : 2} ; \"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}} ; \"n5\" : {\"float2\" : 2.34} ; \"n71\" : {\"n72\" : {\"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}} ; \"f1\" : {\"f2\" : {\"f3\" : {\"f4\" : {\"fString\" : \"fValue\"}}}}}}}}");
            fail();
        } catch (ParsingException e) {

            // then
            assertEquals("provided input is a complex case (nested type & listed type), please use parser created from ANTLR4", e.getMessage());
        }

    }

}
