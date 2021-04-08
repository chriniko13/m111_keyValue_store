package chriniko.kv.datainjector.core;

import chriniko.kv.datatypes.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecordTest {

    @Test
    void constructWorksAsExpected() {

        // when - { “name” : “Mary” ; “address” : { “street” : “Panepistimiou” ; “number” : 12 } }
        //        {"name" : "Mary" ; "address" : {"street" : "Panepistimiou" ; "number" : 12}}
        Value<?> r = ListValue.of(
                new StringValue("name", "Mary"),

                new NestedValue("address", ListValue.of(
                        new StringValue("street", "Panepistimiou"),
                        new IntValue("number", 12)))
        );

        Record record = new Record("mary2021005", r);

        String s = record.toString();

        // then
        assertEquals("\"mary2021005\" : { \"name\" : \"Mary\" ; \"address\" : { \"street\" : \"Panepistimiou\" ; \"number\" : 12 } }", s);

    }
}