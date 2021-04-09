package chriniko.kv.datatypes.parser.antlr;

import chriniko.kv.datatypes.FloatValue;
import chriniko.kv.datatypes.IntValue;
import chriniko.kv.datatypes.StringValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatatypesAntlrParserIntValueTest {

    @Test
    void worksAsExpected() {

        // when
        Value<?> r = DatatypesAntlrParser.process("{ \"_int\" : 17 } ");

        // then
        assertNotNull(r);

        assertTrue(r instanceof IntValue);

        IntValue intValue = (IntValue) r;

        assertEquals("_int", intValue.getKey());
        assertEquals(17, intValue.getValue());


        try {
            // when
            DatatypesAntlrParser.process("{ \"_strTemp\" : 1ab7 } ");
            fail();
        } catch (ParsingException e) {
            // then
            assertEquals("not valid input provided", e.getMessage());
        }


    }

}
