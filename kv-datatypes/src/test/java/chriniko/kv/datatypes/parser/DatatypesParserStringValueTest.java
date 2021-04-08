package chriniko.kv.datatypes.parser;

import chriniko.kv.datatypes.StringValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.parser.DatatypesParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatatypesParserStringValueTest {

    @Test
    void parseWorksAsExpected() {

        // given
        StringValue stringValue = new StringValue("firstname", "nikos");

        String asString = stringValue.asString();
        System.out.println(asString);


        // when
        Value<String> result = DatatypesParser.parseString(asString);


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
         result = DatatypesParser.parseString(asString);

        // then
        assertEquals("grade-class", result.getKey());
        assertEquals("one two three four five six {} {} {{{} } }", result.getValue());


        // when
        try {
            DatatypesParser.parseString("{\"grade class\" : \"one two three four five six {} {} {{{} } }\"}");
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof ParsingException);
            assertEquals("malformed, key contains empty character", e.getMessage());
        }


        // when
        result = DatatypesParser.parseString("{\"grade-class\" : \"foo-bar\"}");

        // then
        assertEquals("grade-class", result.getKey());
        assertEquals("foo-bar", result.getValue());


        // when
        try {
            DatatypesParser.parseString("course");
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof ParsingException);
            assertEquals("malformed, value is not a string type", e.getMessage());
        }


        // when
        try {
            DatatypesParser.parseString("{\"grade-class\" : \"foo-bar;\"}");
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("value provided contains a not allowed char ;", e.getMessage());
        }


        // when
        r = DatatypesParser.parseString("\"profession\" : \"student\"", false);


        // then
        assertEquals("profession", r.getKey());
        assertEquals("student", r.getValue());

    }

}
