package chriniko.kv.server.infra;

import chriniko.kv.datatypes.*;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KvRecordTest {


    @Test
    void indexContents_worksAsExpected() throws Exception {

        // given
        String input = "{ \"_myList\" : [ { \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } } ; { \"_strTemp\" : \"allGood allFine all work\" } ; { \"_fn32\" : { \"_nf42\" : { \"_someOtherStr\" : \"someOtherStrValue\" } } } ] }";
        Value<?> r = DatatypesAntlrParser.process(input);

        KvRecord kvRecord = new KvRecord(r.getKey(), r);

        // when
        kvRecord.indexContents();


        // then
        LinkedHashMap<String, Value<?>> indexedContents = kvRecord.getIndexedContents();
        Assertions.assertNotNull(indexedContents);


        assertEquals(8, indexedContents.size());

        Set<Map.Entry<String, Value<?>>> entries = indexedContents.entrySet();
        Iterator<Map.Entry<String, Value<?>>> iterator = entries.iterator();

        Map.Entry<String, Value<?>> firstEntry = iterator.next();
        assertEquals("_fn3._nf4", firstEntry.getKey());
        assertTrue(firstEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> secondEntry = iterator.next();
        assertEquals("_fn3._nf4._float23", secondEntry.getKey());
        assertTrue(secondEntry.getValue() instanceof FloatValue);


        Map.Entry<String, Value<?>> thirdEntry = iterator.next();
        assertEquals("_fn3", thirdEntry.getKey());
        assertTrue(thirdEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> fourthEntry = iterator.next();
        assertEquals("_strTemp", fourthEntry.getKey());
        assertTrue(fourthEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> fifthEntry = iterator.next();
        assertEquals("_fn32._nf42", fifthEntry.getKey());
        assertTrue(fifthEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> sixthEntry = iterator.next();
        assertEquals("_fn32._nf42._someOtherStr", sixthEntry.getKey());
        assertTrue(sixthEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> seventhEntry = iterator.next();
        assertEquals("_fn32", seventhEntry.getKey());
        assertTrue(seventhEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> eighthEntry = iterator.next();
        assertEquals("_myList", eighthEntry.getKey());
        assertTrue(eighthEntry.getValue() instanceof ListValue);

    }
}
