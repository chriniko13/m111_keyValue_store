package chriniko.kv.datatypes.parser;

import chriniko.kv.datatypes.IntValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.parser.DatatypesParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatatypesParserIntValueTest {

    @Test
    void parseWorksAsExpected() {

        // given
        IntValue intValue = new IntValue("grade", 7);

        String asString = intValue.asString();
        System.out.println(asString);


        // when
        Value<Integer> result = DatatypesParser.parseInt(asString);


        // then
        IntValue r = (IntValue) result;
        assertNotNull(r);
        assertEquals("grade", r.getKey());
        assertEquals(7, r.getValue());



        // given
        intValue = new IntValue("grade class", 7);

        asString = intValue.asString();
        System.out.println(asString);


        // when
        try {
            DatatypesParser.parseInt(asString);
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof ParsingException);
            assertEquals("malformed, key contains empty character", e.getMessage());
        }



        // when
        try {
            DatatypesParser.parseInt("{\"grade-class\" : \"foo-bar\"}");
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof ParsingException);
            assertEquals("malformed, value is not an int type", e.getMessage());
        }


        // when
        try {
            DatatypesParser.parseInt("course");
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof ParsingException);
            assertEquals("malformed, value is not an int type", e.getMessage());
        }
    }
}
