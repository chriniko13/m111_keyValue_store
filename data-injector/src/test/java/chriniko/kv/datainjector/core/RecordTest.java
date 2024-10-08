package chriniko.kv.datainjector.core;

import chriniko.kv.datatypes.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecordTest {

    @Test
    void constructWorksAsExpected() {

        // when
        Value<?> r = ListValue.of("studDetails",
                new StringValue("name", "Mary"),

                ListValue.of("address",
                        new StringValue("street", "Panepistimiou"),
                        new IntValue("number", 12))
        );

        Record record = new Record("mary2021005", r);

        String s = record.toString();

        // then
        assertEquals("\"mary2021005\" : { \"studDetails\" : [ { \"name\" : \"Mary\" } ; { \"address\" : [ { \"street\" : \"Panepistimiou\" } ; { \"number\" : 12 } ] } ] }", s);

    }
}