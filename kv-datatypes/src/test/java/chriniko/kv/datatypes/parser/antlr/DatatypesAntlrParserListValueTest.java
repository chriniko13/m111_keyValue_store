package chriniko.kv.datatypes.parser.antlr;

import chriniko.kv.datatypes.*;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatatypesAntlrParserListValueTest {

    @Test
    void worksAsExpected() {

        // when
        Value<?> r = DatatypesAntlrParser.process("{ \"_strTemp\" : \"allGood allFine all work\" " +
                ";  \"_float2\" : 2.24 " +
                "; \"_int3\" : 17 }");

        // then
        assertNotNull(r);
        assertTrue(r instanceof ListValue);

        ListValue listValue = (ListValue) r;

        List<Value<?>> entries = listValue.getValue();
        assertEquals(3, entries.size());


        Iterator<Value<?>> iterator = entries.iterator();

        Value<?> v = iterator.next();
        assertTrue(v instanceof StringValue);
        StringValue stringValue = (StringValue) v;
        assertEquals("_strTemp", stringValue.getKey());
        assertEquals("allGood allFine all work", stringValue.getValue());


        v = iterator.next();
        assertTrue(v instanceof FloatValue);
        FloatValue floatValue = (FloatValue) v;
        assertEquals("_float2", floatValue.getKey());
        assertEquals(2.24F, floatValue.getValue());


        v = iterator.next();
        assertTrue(v instanceof IntValue);
        IntValue intValue = (IntValue) v;
        assertEquals("_int3", intValue.getKey());
        assertEquals(17, intValue.getValue());

    }


}
