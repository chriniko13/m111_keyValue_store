package chriniko.kv.server.index;

import chriniko.kv.datatypes.*;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KvRecordIndexerTest {

    @Test
    void process_worksAsExpected() throws Exception {


        // =====================================================
        // when
        String input = "{ \"_myList\" : [ { \"_strTemp\" : \"allGood allFine all work\" } ; { \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } } ] }";
        LinkedHashMap<String, Value<?>> r = KvRecordIndexer.process(input);

        // then
        assertNotNull(r);
        assertEquals(5, r.size());

        Set<Map.Entry<String, Value<?>>> entries = r.entrySet();

        Iterator<Map.Entry<String, Value<?>>> iterator = entries.iterator();


        Map.Entry<String, Value<?>> firstEntry = iterator.next();
        assertEquals("_strTemp", firstEntry.getKey());
        assertTrue(firstEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> secondEntry = iterator.next();
        assertEquals("_fn3~>_nf4", secondEntry.getKey());
        assertTrue(secondEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> thirdEntry = iterator.next();
        assertEquals("_fn3~>_nf4~>_float23", thirdEntry.getKey());
        assertTrue(thirdEntry.getValue() instanceof FloatValue);


        Map.Entry<String, Value<?>> fourthEntry = iterator.next();
        assertEquals("_fn3", fourthEntry.getKey());
        assertTrue(fourthEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> fifthEntry = iterator.next();
        assertEquals("_myList", fifthEntry.getKey());
        assertTrue(fifthEntry.getValue() instanceof ListValue);


        // =====================================================
        // when
        input = "{ \"_myList\" : [ { \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } } ; { \"_strTemp\" : \"allGood allFine all work\" } ; { \"_fn32\" : { \"_nf42\" : { \"_someOtherStr\" : \"someOtherStrValue\" } } } ] }";
        r = KvRecordIndexer.process(input);

        // then
        assertNotNull(r);
        assertEquals(8, r.size());

        entries = r.entrySet();
        iterator = entries.iterator();

        firstEntry = iterator.next();
        assertEquals("_fn3~>_nf4", firstEntry.getKey());
        assertTrue(firstEntry.getValue() instanceof NestedValue);


        secondEntry = iterator.next();
        assertEquals("_fn3~>_nf4~>_float23", secondEntry.getKey());
        assertTrue(secondEntry.getValue() instanceof FloatValue);


        thirdEntry = iterator.next();
        assertEquals("_fn3", thirdEntry.getKey());
        assertTrue(thirdEntry.getValue() instanceof NestedValue);


        fourthEntry = iterator.next();
        assertEquals("_strTemp", fourthEntry.getKey());
        assertTrue(fourthEntry.getValue() instanceof StringValue);


        fifthEntry = iterator.next();
        assertEquals("_fn32~>_nf42", fifthEntry.getKey());
        assertTrue(fifthEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> sixthEntry = iterator.next();
        assertEquals("_fn32~>_nf42~>_someOtherStr", sixthEntry.getKey());
        assertTrue(sixthEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> seventhEntry = iterator.next();
        assertEquals("_fn32", seventhEntry.getKey());
        assertTrue(seventhEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> eighthEntry = iterator.next();
        assertEquals("_myList", eighthEntry.getKey());
        assertTrue(eighthEntry.getValue() instanceof ListValue);



        // =====================================================
        // when
        input = "{ \"_myList\" : [ { \"_fn1\" : { \"_nf2\" : { \"_int1\" : 2 } } } ; { \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } } ] }";
        r = KvRecordIndexer.process(input);

        // then
        assertNotNull(r);
        assertEquals(7, r.size());

        entries = r.entrySet();
        iterator = entries.iterator();

        firstEntry = iterator.next();
        assertEquals("_fn1~>_nf2", firstEntry.getKey());
        assertTrue(firstEntry.getValue() instanceof NestedValue);


        secondEntry = iterator.next();
        assertEquals("_fn1~>_nf2~>_int1", secondEntry.getKey());
        assertTrue(secondEntry.getValue() instanceof IntValue);


        thirdEntry = iterator.next();
        assertEquals("_fn1", thirdEntry.getKey());
        assertTrue(thirdEntry.getValue() instanceof NestedValue);


        fourthEntry = iterator.next();
        assertEquals("_fn3~>_nf4", fourthEntry.getKey());
        assertTrue(fourthEntry.getValue() instanceof NestedValue);


        fifthEntry = iterator.next();
        assertEquals("_fn3~>_nf4~>_float23", fifthEntry.getKey());
        assertTrue(fifthEntry.getValue() instanceof FloatValue);


        sixthEntry = iterator.next();
        assertEquals("_fn3", sixthEntry.getKey());
        assertTrue(sixthEntry.getValue() instanceof NestedValue);


        seventhEntry = iterator.next();
        assertEquals("_myList", seventhEntry.getKey());
        assertTrue(seventhEntry.getValue() instanceof ListValue);



        // =====================================================
        // when
        input = "{ \"_n1\" : [ { \"_str1\" : \"4\" } ; { \"_n2\" : { \"_int2\" : 2 } } ; { \"_n3\" : { \"_n4\" : { \"_strTemp\" : \"allGood\" } } } ] }";
        r = KvRecordIndexer.process(input);


        // then
        assertNotNull(r);

        assertEquals(7, r.size());


        entries = r.entrySet();
        iterator = entries.iterator();

        firstEntry = iterator.next();
        assertEquals("_str1", firstEntry.getKey());
        assertTrue(firstEntry.getValue() instanceof StringValue);


        secondEntry = iterator.next();
        assertEquals("_n2", secondEntry.getKey());
        assertTrue(secondEntry.getValue() instanceof NestedValue);


        thirdEntry = iterator.next();
        assertEquals("_n2~>_int2", thirdEntry.getKey());
        assertTrue(thirdEntry.getValue() instanceof IntValue);


        fourthEntry = iterator.next();
        assertEquals("_n3~>_n4", fourthEntry.getKey());
        assertTrue(fourthEntry.getValue() instanceof NestedValue);


        fifthEntry = iterator.next();
        assertEquals("_n3~>_n4~>_strTemp", fifthEntry.getKey());
        assertTrue(fifthEntry.getValue() instanceof StringValue);


        sixthEntry = iterator.next();
        assertEquals("_n3", sixthEntry.getKey());
        assertTrue(sixthEntry.getValue() instanceof NestedValue);


        seventhEntry = iterator.next();
        assertEquals("_n1", seventhEntry.getKey());
        assertTrue(seventhEntry.getValue() instanceof ListValue);

    }


    // TODO 2

    // TODO 3

    // TODO 4

    // TODO 5
}