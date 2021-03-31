package chriniko.kv.datatypes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParserStringValueTest {

    @Test
    void parseWorksAsExpected() {

        // given
        StringValue stringValue = new StringValue("firstname", "nikos");

        String asString = stringValue.asString();
        System.out.println(asString);


        // when
        Value<String> result = Parser.parseString(asString);


        // then
        StringValue r = (StringValue) result;
        assertNotNull(r);
        assertEquals("firstname", r.getKey());
        assertEquals("nikos", r.getValue());



        // given
        stringValue = new StringValue("grade-class", "one two three four five six {} {} {{{} } }");

        asString = stringValue.asString();
        System.out.println(asString);


        // when
         result = Parser.parseString(asString);

        // then
        assertEquals("grade-class", result.getKey());
        assertEquals("one two three four five six {} {} {{{} } }", result.getValue());

        // TODO



        // when
        result = Parser.parseString("{\"grade-class\" : \"foo-bar\"}");

        // then
        assertEquals("grade-class", result.getKey());
        assertEquals("foo-bar", result.getValue());


        // when
        try {
            Parser.parseString("course");
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof ParsingException);
            assertEquals("malformed, value is not a string type", e.getMessage());
        }
    }

}
