package chriniko.kv.datainjector.core;

import chriniko.kv.datainjector.type.ListValue;
import chriniko.kv.datainjector.type.NestedValue;
import chriniko.kv.datainjector.type.Value;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataGeneratorTest {

    @Test
    void processWorksAsExpected() throws Exception {

        // given
        Path tempDirectory = Files.createTempDirectory("test-dir");
        System.out.println("tempDirectory: " + tempDirectory);

        DataGenerator dataGenerator = new DataGenerator();


        // when
        List<Record> records = dataGenerator.create(20, 1, 2, 60, null);


        // then
        assertEquals(20, records.size());

        Record record = records.get(0);
        assertNotNull(record.getKey());

        Value<?> value = record.getValue();
        assertTrue(value instanceof ListValue);
        ListValue listValue = (ListValue) value;
        assertEquals(2, listValue.getValue().size());

        Value<?> someValue = listValue.getValue().get(0);
        assertFalse(someValue instanceof NestedValue);

        Path path = dataGenerator.sinkResultsToFile(tempDirectory.toString(), records);
        System.out.println("to see created file check path: " + path);



        // when (keysPerValue more than entries in sampleKeyFile.txt)
        records = dataGenerator.create(20, 1, 20, 60, null);


        // then
        assertEquals(20, records.size());

        record = records.get(0);
        assertNotNull(record.getKey());

        value = record.getValue();
        assertTrue(value instanceof ListValue);
        listValue = (ListValue) value;
        assertEquals(5, listValue.getValue().size());

        someValue = listValue.getValue().get(0);
        assertFalse(someValue instanceof NestedValue);

        path = dataGenerator.sinkResultsToFile(tempDirectory.toString(), records);
        System.out.println("to see created file check path: " + path);



        // when (depth/nesting is more than 1)
        records = dataGenerator.create(20, 5, 20, 60, null);



        // then
        assertEquals(20, records.size());

        record = records.get(0);
        assertNotNull(record.getKey());

        value = record.getValue();
        assertTrue(value instanceof ListValue);
        listValue = (ListValue) value;
        assertEquals(5, listValue.getValue().size());

        someValue = listValue.getValue().get(0);
        assertTrue(someValue instanceof NestedValue);

        NestedValue nestedValue = (NestedValue) someValue;
        assertEquals(5, nestedValue.depth());

        path = dataGenerator.sinkResultsToFile(tempDirectory.toString(), records);
        System.out.println("to see created file check path: " + path);
    }
}