package chriniko.kv.datatypes.parser.antlr;

import chriniko.kv.datatypes.FloatValue;
import chriniko.kv.datatypes.StringValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatatypesAntlrParserFloatValueTest {

    @Test
    void worksAsExpected() {

        // when
        Value<?> r = DatatypesAntlrParser.process("{ \"_float\" : 17.11 } ");

        // then
        assertNotNull(r);

        assertTrue(r instanceof FloatValue);

        FloatValue floatValue = (FloatValue) r;

        assertEquals("_float", floatValue.getKey());
        assertEquals(17.11F, floatValue.getValue());


        try {
            // when
            DatatypesAntlrParser.process("{ \"_float\" : 17. 11 } ");
            fail();
        } catch (ParsingException e) {
            // then
            assertEquals("not valid input provided", e.getMessage());
        }


    }

}
