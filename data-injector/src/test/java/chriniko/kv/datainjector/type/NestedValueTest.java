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
                        new NestedValue("details", new IntValue("number", 27))));

        asString = nestedValue.asString();

        // then
        assertEquals("{\"address\" : {\"street\" : {\"details\" : {\"number\" : 27}}}", asString);
        assertTrue(nestedValue.getValue() instanceof NestedValue);


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


}