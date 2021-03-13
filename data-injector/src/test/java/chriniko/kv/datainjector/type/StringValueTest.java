package chriniko.kv.datainjector.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringValueTest {

    @Test
    void constructWorksAsExpected() {

        // given
        StringValue stringValue = new StringValue("name", "nikolaos christidis");

        // when
        String asString = stringValue.asString();

        // then
        assertEquals("{\"name\" : \"nikolaos christidis\"}", asString);
        assertEquals("\"name\" : \"nikolaos christidis\"", stringValue.asStringUnwrapped());

        assertEquals("nikolaos christidis", stringValue.getValue());

    }

}