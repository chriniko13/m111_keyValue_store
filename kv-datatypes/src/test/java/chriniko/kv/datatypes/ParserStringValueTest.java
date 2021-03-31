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


        // when
        try {
            Parser.parseString("{\"grade class\" : \"one two three four five six {} {} {{{} } }\"}");
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof ParsingException);
            assertEquals("malformed, key contains empty character", e.getMessage());
        }


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


        // when
        try {
            Parser.parseString("{\"grade-class\" : \"foo-bar;\"}");
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("value provided contains a not allowed char ;", e.getMessage());
        }


        // when
        r = Parser.parseString("\"profession\" : \"student\"", false);


        // then
        assertEquals("profession", r.getKey());
        assertEquals("student", r.getValue());

    }

}
