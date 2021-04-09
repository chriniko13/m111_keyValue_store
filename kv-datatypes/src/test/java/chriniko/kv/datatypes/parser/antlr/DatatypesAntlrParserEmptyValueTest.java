package chriniko.kv.datatypes.parser.antlr;

import chriniko.kv.datatypes.EmptyValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatatypesAntlrParserEmptyValueTest {

    @Test
    void worksAsExpected() {

        // when
        Value<?> r = DatatypesAntlrParser.process("{ \"_empty\" : {   } } ");

        // then
        assertNotNull(r);

        assertTrue(r instanceof EmptyValue);

        EmptyValue emptyValue = (EmptyValue) r;

        assertEquals("_empty", emptyValue.getKey());
    }

}
