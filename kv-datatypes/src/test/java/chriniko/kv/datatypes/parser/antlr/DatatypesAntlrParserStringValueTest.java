package chriniko.kv.datatypes.parser.antlr;

import chriniko.kv.datatypes.StringValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatatypesAntlrParserStringValueTest {

    @Test
    void worksAsExpected() {

        // when
        Value<?> r = DatatypesAntlrParser.process("{ \"_strTemp\" : \"allGood allFine all work\" } ");

        // then
        assertNotNull(r);

        assertTrue(r instanceof StringValue);

        StringValue stringValue = (StringValue) r;

        assertEquals("_strTemp", stringValue.getKey());
        assertEquals("allGood allFine all work", stringValue.getValue());


        // when
        r = DatatypesAntlrParser.process("{ \"_strTemp\" : \"allGood\" } ");

        // then
        assertNotNull(r);

        assertTrue(r instanceof StringValue);

        stringValue = (StringValue) r;

        assertEquals("_strTemp", stringValue.getKey());
        assertEquals("allGood", stringValue.getValue());

        // when
        r = DatatypesAntlrParser.process("{ \"_strTemp\" : \"3.1456\" } ");

        // then
        assertNotNull(r);

        assertTrue(r instanceof StringValue);

        stringValue = (StringValue) r;

        assertEquals("_strTemp", stringValue.getKey());
        assertEquals("3.1456", stringValue.getValue());

    }

}
