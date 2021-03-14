package chriniko.kv.datainjector.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NestedValueTest {


    @Test
    void constructWorksAsExpected() {


        // when
        NestedValue nestedValue = new NestedValue("address",
                new NestedValue("street",
                        new IntValue("number", 27)));

        String asString = nestedValue.asString();

        // then
        assertEquals("{\"address\" : {\"street\" : {\"number\" : 27}}}", asString);
        assertTrue(nestedValue.getValue() instanceof NestedValue);


        // when
        nestedValue = new NestedValue("address",
                new NestedValue("street",
                        new NestedValue("details",
                                new NestedValue("more-details",
                                        new IntValue("number", 27)))));

        asString = nestedValue.asString();

        // then
        assertEquals("{\"address\" : {\"street\" : {\"details\" : {\"more-details\" : {\"number\" : 27}}}}}", asString);
        assertTrue(nestedValue.getValue() instanceof NestedValue);
        assertEquals(3, nestedValue.depth());


        // when - { “name” : “Mary” ; “address” : { “street” : “Panepistimiou” ; “number” : 12 } }
        //        {"name" : "Mary" ; "address" : {"street" : "Panepistimiou" ; "number" : 12}}
        Value<?> r = ListValue.of(
                new StringValue("name", "Mary"),

                new NestedValue("address", ListValue.of(
                        new StringValue("street", "Panepistimiou"),
                        new IntValue("number", 12)))
        );


        // then
        assertEquals("{\"name\" : \"Mary\" ; \"address\" : {\"street\" : \"Panepistimiou\" ; \"number\" : 12}}", r.asString());
        assertEquals("\"name\" : \"Mary\" ; \"address\" : {\"street\" : \"Panepistimiou\" ; \"number\" : 12}", r.asStringUnwrapped());

    }


    @Test
    void combineWorksAsExpected() {

        // when
        NestedValue result = NestedValue.combine("nested",
                new NestedValue("n1", new IntValue("int1", 1)),
                new NestedValue("n2", new IntValue("int2", 2)),
                new NestedValue("n3", new IntValue("int3", 3)),
                new NestedValue("n4", new StringValue("str1", "4")),
                new NestedValue("n5", new StringValue("str2", "5")),
                new NestedValue("n6", new StringValue("str3", "6"))
        );


        // then
        assertEquals(6, result.depth());

        assertEquals("{\"nested\" : {\"n1\" : {\"n2\" : {\"n3\" : {\"n4\" : {\"n5\" : {\"n6\" : {\"str3\" : \"6\"}}}}}}}}", result.asString());

    }


}