package chriniko.kv.datatypes.parser;

import chriniko.kv.datatypes.FloatValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.parser.DatatypesParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatatypesParserFloatValueTest {


    @Test
    void parseWorksAsExpected() throws Exception {

        // given
        FloatValue floatValue = new FloatValue("grade", 7.5F);

        String asString = floatValue.asString();
        System.out.println(asString);


        // when
        Value<Float> result = DatatypesParser.parseFloat(asString);


        // then
        FloatValue r = (FloatValue) result;
        assertNotNull(r);
        assertEquals("grade", r.getKey());
        assertEquals(7.5F, r.getValue());



        // given
        floatValue = new FloatValue("grade class", 7.5F);

        asString = floatValue.asString();
        System.out.println(asString);


        // when
        try {
            DatatypesParser.parseFloat(asString);
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof ParsingException);
            assertEquals("malformed, key contains empty character", e.getMessage());
        }



        // when
        try {
            DatatypesParser.parseFloat("{\"grade-class\" : \"foo-bar\"}");
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof ParsingException);
            assertEquals("malformed, value is not a float type", e.getMessage());
        }


        // when
        try {
            DatatypesParser.parseFloat("course");
            fail();
        } catch (Exception e) {
            // then

            assertTrue(e instanceof ParsingException);
            assertEquals("malformed, value is not a float type", e.getMessage());
        }
    }

}