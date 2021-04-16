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
        assertEquals("_myList~>_strTemp", firstEntry.getKey());
        assertTrue(firstEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> secondEntry = iterator.next();
        assertEquals("_myList~>_fn3~>_nf4~>_float23", secondEntry.getKey());
        assertTrue(secondEntry.getValue() instanceof FloatValue);


        Map.Entry<String, Value<?>> thirdEntry = iterator.next();
        assertEquals("_myList~>_fn3~>_nf4", thirdEntry.getKey());
        assertTrue(thirdEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> fourthEntry = iterator.next();
        assertEquals("_myList~>_fn3", fourthEntry.getKey());
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
        assertEquals("_myList~>_fn3~>_nf4~>_float23", firstEntry.getKey());
        assertTrue(firstEntry.getValue() instanceof FloatValue);


        secondEntry = iterator.next();
        assertEquals("_myList~>_fn3~>_nf4", secondEntry.getKey());
        assertTrue(secondEntry.getValue() instanceof NestedValue);


        thirdEntry = iterator.next();
        assertEquals("_myList~>_fn3", thirdEntry.getKey());
        assertTrue(thirdEntry.getValue() instanceof NestedValue);


        fourthEntry = iterator.next();
        assertEquals("_myList~>_strTemp", fourthEntry.getKey());
        assertTrue(fourthEntry.getValue() instanceof StringValue);


        fifthEntry = iterator.next();
        assertEquals("_myList~>_fn32~>_nf42~>_someOtherStr", fifthEntry.getKey());
        assertTrue(fifthEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> sixthEntry = iterator.next();
        assertEquals("_myList~>_fn32~>_nf42", sixthEntry.getKey());
        assertTrue(sixthEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> seventhEntry = iterator.next();
        assertEquals("_myList~>_fn32", seventhEntry.getKey());
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
        assertEquals("_myList~>_fn1~>_nf2~>_int1", firstEntry.getKey());
        assertTrue(firstEntry.getValue() instanceof IntValue);


        secondEntry = iterator.next();
        assertEquals("_myList~>_fn1~>_nf2", secondEntry.getKey());
        assertTrue(secondEntry.getValue() instanceof NestedValue);


        thirdEntry = iterator.next();
        assertEquals("_myList~>_fn1", thirdEntry.getKey());
        assertTrue(thirdEntry.getValue() instanceof NestedValue);


        fourthEntry = iterator.next();
        assertEquals("_myList~>_fn3~>_nf4~>_float23", fourthEntry.getKey());
        assertTrue(fourthEntry.getValue() instanceof FloatValue);


        fifthEntry = iterator.next();
        assertEquals("_myList~>_fn3~>_nf4", fifthEntry.getKey());
        assertTrue(fifthEntry.getValue() instanceof NestedValue);


        sixthEntry = iterator.next();
        assertEquals("_myList~>_fn3", sixthEntry.getKey());
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
        assertEquals("_n1~>_str1", firstEntry.getKey());
        assertTrue(firstEntry.getValue() instanceof StringValue);


        secondEntry = iterator.next();
        assertEquals("_n1~>_n2~>_int2", secondEntry.getKey());
        assertTrue(secondEntry.getValue() instanceof IntValue);


        thirdEntry = iterator.next();
        assertEquals("_n1~>_n2", thirdEntry.getKey());
        assertTrue(thirdEntry.getValue() instanceof NestedValue);


        fourthEntry = iterator.next();
        assertEquals("_n1~>_n3~>_n4~>_strTemp", fourthEntry.getKey());
        assertTrue(fourthEntry.getValue() instanceof StringValue);


        fifthEntry = iterator.next();
        assertEquals("_n1~>_n3~>_n4", fifthEntry.getKey());
        assertTrue(fifthEntry.getValue() instanceof NestedValue);


        sixthEntry = iterator.next();
        assertEquals("_n1~>_n3", sixthEntry.getKey());
        assertTrue(sixthEntry.getValue() instanceof NestedValue);


        seventhEntry = iterator.next();
        assertEquals("_n1", seventhEntry.getKey());
        assertTrue(seventhEntry.getValue() instanceof ListValue);

    }


    @Test
    void worksAsExpected2() throws Exception {

        String input = "{ \"_studentDetails\" : [ { \"_username\" : \"chriniko\" } " +

                "; { \"_email\" : \"chriniko\" } " +

                "; { \"_address\" : [ { \"_street\" : \"Panepistimioupoli 123, Kesariani\" } " +
                                        "; { \"_postCode\" : \"16121\" } " +
                                        "; { \"_city\" : \"Athens\" } " +
                                        "; { \"_country\" : \"Greece\" } ] } " +

                "; { \"_name\" : [ { \"_firstname\" : \"Nikolaos\" } " +
                                    "; { \"_surname\" : \"Christidis\" } ] } ] }";


        System.out.println(input);


        LinkedHashMap<String, Value<?>> r = KvRecordIndexer.process(input);

        // then
        assertNotNull(r);
        assertEquals(11, r.size());

        Set<Map.Entry<String, Value<?>>> entries = r.entrySet();

        Iterator<Map.Entry<String, Value<?>>> iterator = entries.iterator();


        Map.Entry<String, Value<?>> firstEntry = iterator.next();
        assertEquals("_studentDetails~>_username", firstEntry.getKey());
        assertTrue(firstEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> secondEntry = iterator.next();
        assertEquals("_studentDetails~>_email", secondEntry.getKey());
        assertTrue(secondEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> thirdEntry = iterator.next();
        assertEquals("_studentDetails~>_address~>_street", thirdEntry.getKey());
        assertTrue(thirdEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> fourthEntry = iterator.next();
        assertEquals("_studentDetails~>_address~>_postCode", fourthEntry.getKey());
        assertTrue(fourthEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> fifthEntry = iterator.next();
        assertEquals("_studentDetails~>_address~>_city", fifthEntry.getKey());
        assertTrue(fifthEntry.getValue() instanceof StringValue);

        Map.Entry<String, Value<?>> sixthEntry = iterator.next();
        assertEquals("_studentDetails~>_address~>_country", sixthEntry.getKey());
        assertTrue(sixthEntry.getValue() instanceof StringValue);

        Map.Entry<String, Value<?>> seventhEntry = iterator.next();
        assertEquals("_studentDetails~>_address", seventhEntry.getKey());
        assertTrue(seventhEntry.getValue() instanceof ListValue);


        Map.Entry<String, Value<?>> eightEntry = iterator.next();
        assertEquals("_studentDetails~>_name~>_firstname", eightEntry.getKey());
        assertTrue(eightEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> ninthEntry = iterator.next();
        assertEquals("_studentDetails~>_name~>_surname", ninthEntry.getKey());
        assertTrue(ninthEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> tenthEntry = iterator.next();
        assertEquals("_studentDetails~>_name", tenthEntry.getKey());
        assertTrue(tenthEntry.getValue() instanceof ListValue);


        Map.Entry<String, Value<?>> eleventhEntry = iterator.next();
        assertEquals("_studentDetails", eleventhEntry.getKey());
        assertTrue(eleventhEntry.getValue() instanceof ListValue);
    }


    // TODO 3

    // TODO 4

    // TODO 5
}