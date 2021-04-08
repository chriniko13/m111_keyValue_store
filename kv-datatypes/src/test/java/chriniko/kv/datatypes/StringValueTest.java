package chriniko.kv.datatypes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class StringValueTest {

    @Test
    void constructWorksAsExpected() {

        // given
        StringValue stringValue = new StringValue("name", "nikolaos christidis");

        // when
        String asString = stringValue.asString();

        // then
        assertEquals("{ \"name\" : \"nikolaos christidis\" }", asString);
        assertEquals("\"name\" : \"nikolaos christidis\"", stringValue.asStringUnwrapped());

        assertEquals("nikolaos christidis", stringValue.getValue());


        // when
        try {
            new StringValue("name", "nikolaos ;christidis");
            fail();
        } catch (IllegalArgumentException e ){
            // then
            assertEquals("value provided contains a not allowed char ;", e.getMessage());
        }

    }

}