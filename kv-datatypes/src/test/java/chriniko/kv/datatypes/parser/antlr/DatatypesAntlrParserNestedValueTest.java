package chriniko.kv.datatypes.parser.antlr;

import chriniko.kv.datatypes.IntValue;
import chriniko.kv.datatypes.NestedValue;
import chriniko.kv.datatypes.StringValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class DatatypesAntlrParserNestedValueTest {

    @Test
    void worksAsExpected() throws Exception {

        // when
        Value<?> r = DatatypesAntlrParser.process("{ \"_fn3\" : { \"_nf4\" : { \"_strTemp\" : \"allGood allFine all work\" } } }");

        // then
        assertNotNull(r);

        assertTrue(r instanceof NestedValue);

        NestedValue nestedValue = (NestedValue) r;

        assertEquals(1, nestedValue.maxDepth());

        assertTrue(nestedValue.getValue() instanceof NestedValue);
        assertTrue(nestedValue.getValue().getValue() instanceof StringValue);

        assertEquals("_fn3", nestedValue.getKey());

        assertEquals(Set.of("allGood allFine all work"), nestedValue.allFlatValues().stream().map(Value::getValue).collect(Collectors.toSet()));

        assertEquals(Set.of("_fn3", "_nf4", "_strTemp"), nestedValue.allKeys());


        // when
        r = DatatypesAntlrParser.process("{ \"_address1\" : { \"_address2\" : { \"_address3\" : { \"_more-details\" : { \"_number\" : 27 } } } } }");

        // then
        assertNotNull(r);

        assertTrue(r instanceof NestedValue);

        nestedValue = (NestedValue) r;

        assertEquals(3, nestedValue.maxDepth());

        assertTrue(nestedValue.getValue() instanceof NestedValue);
        assertTrue(nestedValue.getValue().getValue() instanceof NestedValue);
        assertTrue(((Value<?>) nestedValue.getValue().getValue()).getValue() instanceof NestedValue);
        assertTrue(((Value<?>) ((Value<?>) nestedValue.getValue().getValue()).getValue()).getValue() instanceof IntValue);

        assertEquals("_address1", nestedValue.getKey());

        assertEquals(Set.of(27), nestedValue.allFlatValues().stream().map(Value::getValue).collect(Collectors.toSet()));

        assertEquals(Set.of("_address1", "_address2", "_address3", "_more-details", "_number"), nestedValue.allKeys());

    }

}
