package chriniko.kv.server.infra;

import chriniko.kv.datatypes.*;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import chriniko.kv.server.index.KvIndexedData;
import chriniko.kv.trie.Trie;
import chriniko.kv.trie.infra.TrieStatistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class KvRecordTest {


    @Test
    void indexContents_worksAsExpected() throws Exception {

        // given
        String input = "{ \"_myList\" : [ { \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } } ; { \"_strTemp\" : \"allGood allFine all work\" } ; { \"_fn32\" : { \"_nf42\" : { \"_someOtherStr\" : \"someOtherStrValue\" } } } ] }";
        Value<?> r = DatatypesAntlrParser.process(input);

        KvRecord kvRecord = new KvRecord(r.getKey(), r);

        // when
        kvRecord.indexContents();


        // then (check indexed contents by using map data-structure)
        LinkedHashMap<String, Value<?>> indexedContents = kvRecord.getIndexedContentsByKeyPath();
        Assertions.assertNotNull(indexedContents);


        assertEquals(8, indexedContents.size());

        Set<Map.Entry<String, Value<?>>> entries = indexedContents.entrySet();
        Iterator<Map.Entry<String, Value<?>>> iterator = entries.iterator();

        Map.Entry<String, Value<?>> firstEntry = iterator.next();
        assertEquals("_myList~>_fn3~>_nf4~>_float23", firstEntry.getKey());
        assertTrue(firstEntry.getValue() instanceof FloatValue);


        Map.Entry<String, Value<?>> secondEntry = iterator.next();
        assertEquals("_myList~>_fn3~>_nf4", secondEntry.getKey());
        assertTrue(secondEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> thirdEntry = iterator.next();
        assertEquals("_myList~>_fn3", thirdEntry.getKey());
        assertTrue(thirdEntry.getValue() instanceof NestedValue);


        Map.Entry<String, Value<?>> fourthEntry = iterator.next();
        assertEquals("_myList~>_strTemp", fourthEntry.getKey());
        assertTrue(fourthEntry.getValue() instanceof StringValue);


        Map.Entry<String, Value<?>> fifthEntry = iterator.next();
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



        // then (check indexed contents by using trie data-structure)
        Trie<KvIndexedData> indexedContentsByKeyPathTrie = kvRecord.getIndexedContentsByKeyPathTrie();

        TrieStatistics trieStatistics = indexedContentsByKeyPathTrie.gatherStatisticsWithRecursion();

        assertEquals(52, trieStatistics.getCountOfNoCompleteWords());
        assertEquals(8, trieStatistics.getCountOfCompleteWords());


        final List<KvIndexedData> kvIndexedData = trieStatistics.getValues();
        assertEquals(8, kvIndexedData.size());

        assertEquals("_myList", kvIndexedData.get(0).key());
        assertTrue(kvIndexedData.get(0).value() instanceof ListValue);
        assertTrue(indexedContentsByKeyPathTrie.find("_myList").isPresent());

        assertEquals("_myList~>_strTemp", kvIndexedData.get(1).key());
        assertTrue(kvIndexedData.get(1).value() instanceof StringValue);
        assertTrue(indexedContentsByKeyPathTrie.find("_myList~>_strTemp").isPresent());

        assertEquals("_myList~>_fn3", kvIndexedData.get(2).key());
        assertTrue(kvIndexedData.get(2).value() instanceof NestedValue);
        assertTrue(indexedContentsByKeyPathTrie.find("_myList~>_fn3").isPresent());

        assertEquals("_myList~>_fn32", kvIndexedData.get(3).key());
        assertTrue(kvIndexedData.get(3).value() instanceof NestedValue);
        assertTrue(indexedContentsByKeyPathTrie.find("_myList~>_fn32").isPresent());

        assertEquals("_myList~>_fn32~>_nf42", kvIndexedData.get(4).key());
        assertTrue(kvIndexedData.get(4).value() instanceof NestedValue);
        assertTrue(indexedContentsByKeyPathTrie.find("_myList~>_fn32~>_nf42").isPresent());

        assertEquals("_myList~>_fn32~>_nf42~>_someOtherStr", kvIndexedData.get(5).key());
        assertTrue(kvIndexedData.get(5).value() instanceof StringValue);
        assertTrue(indexedContentsByKeyPathTrie.find("_myList~>_fn32~>_nf42~>_someOtherStr").isPresent());

        assertEquals("_myList~>_fn3~>_nf4", kvIndexedData.get(6).key());
        assertTrue(kvIndexedData.get(6).value() instanceof NestedValue);
        assertTrue(indexedContentsByKeyPathTrie.find("_myList~>_fn3~>_nf4").isPresent());

        assertEquals("_myList~>_fn3~>_nf4~>_float23", kvIndexedData.get(7).key());
        assertTrue(kvIndexedData.get(7).value() instanceof FloatValue);
        assertTrue(indexedContentsByKeyPathTrie.find("_myList~>_fn3~>_nf4~>_float23").isPresent());


    }
}
