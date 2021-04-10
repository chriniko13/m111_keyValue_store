package chriniko.kv.datainjector.core;

import chriniko.kv.datainjector.doc.Nullable;
import chriniko.kv.datatypes.*;
import chriniko.kv.datatypes.infra.SupportedValueTypes;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

public final class DataGenerator {

    /*
        Your first task will be to write a program that generates syntactically correct data that will be loaded to
        your key value store. Your program should operate as follows:

        $ createData -k keyFile.txt -n 1000 -d 3 -l 4 -m 5

        where:

            -n  indicates the number of lines (i.e. separate data) that we would like to generate (e.g. 1000)

            -d  is the maximum level of nesting (i.e. how many times in a line a value can have a set of { key : values } ).
                Zero means no nesting, i.e. there is only one set of key-values per line (in the value of the
                high level key)

            -m  is the maximum number of keys inside each value.

            -l  is the maximum length of a string value whenever you need to generate a string. For example 4
            means that we can generate Strings of up to length 4 (e.g. “ab”, “abcd”, “a”). We should not generate
            empty strings (i.e. “” is not correct). Strings can be only letters (upper and lowercase) and numbers. No
            symbols.


            -k  keyFile.txt is a file containing a space-separated list of key names and their data types that we
                can potentially use for creating data. For example:

                name string
                age int
                height float
                street string
                level int

     */
    public List<Record> create(int noOfLines /*n*/, int depth /*d*/,
                               int keysPerValue /*m*/, int stringLength /*l*/,
                               @Nullable String keyFile /*k*/) {

        if (noOfLines < 1 || noOfLines > 200_000) {
            throw new IllegalStateException("noOfLines(n): should be >= 1 and <= 200_000");
        }

        if (depth < 1 || depth > 20) {
            throw new IllegalStateException("depth(d): should be >= 1 and <= 20");
        }

        if (keysPerValue < 1 || keysPerValue > 20) {
            throw new IllegalStateException("keysPerValue(m): should be >= 1 and <= 20");
        }

        if (stringLength < 10 || stringLength > 125) {
            throw new IllegalStateException("stringLength(l): should be >= 10 and <= 125");
        }


        final BufferedReader keyFileBufferedReader;
        if (keyFile == null) {

            InputStream in = getClass().getResourceAsStream("/sampleKeyFile.txt");
            keyFileBufferedReader = new BufferedReader(new InputStreamReader(in));


        } else {
            try {
                //TODO test if it works...
                keyFileBufferedReader = Files.newBufferedReader(Paths.get(URI.create(keyFile)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        final Faker faker = new Faker();

        final Map<String, Class<? extends Value<?>>> dataTypesInfo = RecordKeyFileParser.process(keyFileBufferedReader);

        final List<Record> recordsToSaveToDestinationFile = new ArrayList<>();

        int idSequence = 0;

        for (int i = 0; i < noOfLines; i++) {

            final ListValue listValue = new ListValue("_contents");

            int currentEntryProcessed = 0;
            for (Map.Entry<String, Class<? extends Value<?>>> entry : dataTypesInfo.entrySet()) {

                if (++currentEntryProcessed > keysPerValue) {
                    // Note: the maximum number of keys inside each value reached, so stop.
                    break;
                }

                String fieldName = entry.getKey();
                Class<? extends Value<?>> fieldType = entry.getValue();

                if (fieldName.equals("name") && fieldType.equals(StringValue.class)) {

                    String username = faker.name().username();
                    if (username.length() > stringLength) {
                        username = username.substring(0, stringLength);
                    }
                    StringValue stringValue = new StringValue("_name", username);


                    if (depth == 1) {
                        listValue.add(stringValue);
                    } else {
                        listValue.add(
                                NestedValue.combine("_nestedName",
                                        IntStream.rangeClosed(1, depth)
                                                .boxed()
                                                .map(r -> new NestedValue("_nested" + r, stringValue))
                                                .toArray(NestedValue[]::new)
                                )
                        );
                    }

                } else if (fieldName.equals("age") && fieldType.equals(IntValue.class)) {

                    IntValue intValue = new IntValue("_age", getRandomNumber(10, 99));
                    if (depth == 1) {
                        listValue.add(intValue);
                    } else {

                        listValue.add(
                                NestedValue.combine("_nestedAge",
                                        IntStream.rangeClosed(1, depth)
                                                .boxed()
                                                .map(r -> new NestedValue("_nested" + r, intValue))
                                                .toArray(NestedValue[]::new)
                                )
                        );
                    }

                } else if (fieldName.equals("height") && fieldType.equals(FloatValue.class)) {

                    FloatValue floatValue = new FloatValue("_height", getRandomNumber(1.00F, 2.35F));
                    if (depth == 1) {
                        listValue.add(floatValue);
                    } else {

                        listValue.add(
                                NestedValue.combine("_nestedHeight",
                                        IntStream.rangeClosed(1, depth)
                                                .boxed()
                                                .map(r -> new NestedValue("_nested" + r, floatValue))
                                                .toArray(NestedValue[]::new)
                                )
                        );
                    }

                } else if (fieldName.equals("street") && fieldType.equals(StringValue.class)) {

                    StringValue stringValue = new StringValue("_street", faker.address().fullAddress());
                    if (depth == 1) {
                        listValue.add(stringValue);
                    } else {

                        listValue.add(
                                NestedValue.combine("_nestedStreet",
                                        IntStream.rangeClosed(1, depth)
                                                .boxed()
                                                .map(r -> new NestedValue("_nested" + r, stringValue))
                                                .toArray(NestedValue[]::new)
                                )
                        );
                    }

                } else if (fieldName.equals("level") && fieldType.equals(IntValue.class)) {

                    IntValue intValue = new IntValue("_level", getRandomNumber(1, 20));
                    if (depth == 1) {
                        listValue.add(intValue);
                    } else {

                        listValue.add(
                                NestedValue.combine("_nestedLevel",
                                        IntStream.rangeClosed(1, depth)
                                                .boxed()
                                                .map(r -> new NestedValue("_nested" + r, intValue))
                                                .toArray(NestedValue[]::new)
                                )
                        );
                    }

                } else {
                    // ################# NOT DECLARED KEY FIELDS SECTION #################

                    // Note: now we do a best guess with only fieldType, because
                    //       as stated in the assignment => Do not worry if the data do not make sense (e.g. age contains an address).

                    if (fieldType.equals(EmptyValue.class)) {

                        EmptyValue emptyValue = new EmptyValue();
                        if (depth == 1) {
                            listValue.add(emptyValue);
                        } else {

                            listValue.add(
                                    NestedValue.combine("_nestedEmpty",
                                            IntStream.rangeClosed(1, depth)
                                                    .boxed()
                                                    .map(r -> new NestedValue("_nested" + r, emptyValue))
                                                    .toArray(NestedValue[]::new)
                                    )
                            );
                        }

                    } else if (fieldType.equals(FloatValue.class)) {

                        FloatValue floatValue = new FloatValue("_sampleFloat" + (++idSequence), getRandomNumber(12.00F, 45.00F));
                        if (depth == 1) {
                            listValue.add(floatValue);
                        } else {

                            listValue.add(
                                    NestedValue.combine("_nestedSampleFloat",
                                            IntStream.rangeClosed(1, depth)
                                                    .boxed()
                                                    .map(r -> new NestedValue("_nested" + r, floatValue))
                                                    .toArray(NestedValue[]::new)
                                    )
                            );
                        }

                    } else if (fieldType.equals(IntValue.class)) {

                        IntValue intValue = new IntValue("_sampleInt" + (++idSequence), getRandomNumber(12, 45));
                        if (depth == 1) {
                            listValue.add(intValue);
                        } else {

                            listValue.add(
                                    NestedValue.combine("_nestedSampleInt",
                                            IntStream.rangeClosed(1, depth)
                                                    .boxed()
                                                    .map(r -> new NestedValue("_nested" + r, intValue))
                                                    .toArray(NestedValue[]::new)
                                    )
                            );
                        }

                    } else if (fieldType.equals(NestedValue.class)) {
                        throw new IllegalStateException("list is not supported without specifying the fieldName rule");

                    } else if (fieldType.equals(StringValue.class)) {

                        String fact = faker.chuckNorris().fact();
                        if (fact.length() > stringLength) {
                            fact = fact.substring(0, stringLength);
                        }
                        StringValue stringValue = new StringValue("_sampleString" + (++idSequence), fact);

                        if (depth == 1) {
                            listValue.add(stringValue);
                        } else {

                            listValue.add(
                                    NestedValue.combine("_nestedSampleString",
                                            IntStream.rangeClosed(1, depth)
                                                    .boxed()
                                                    .map(r -> new NestedValue("_nested" + r, stringValue))
                                                    .toArray(NestedValue[]::new)
                                    )
                            );
                        }

                    } else if (fieldType.equals(ListValue.class)) {
                        throw new IllegalStateException("list is not supported without specifying the fieldName rule");

                    } else {
                        throw new IllegalStateException("fieldType provided is not valid, supported types are: " + SupportedValueTypes.supportedTypes);
                    }

                }


            } // dataTypesInfo.entrySet.

            String key = UUID.randomUUID().toString();

            recordsToSaveToDestinationFile.add(
                    new Record(key, listValue)
            );

        } // noOfLines.

        return recordsToSaveToDestinationFile;
    }


    private static int getRandomNumber(int min, int max) {
        return RandomUtils.nextInt(min, max);
    }

    private static float getRandomNumber(float min, float max) {
        return RandomUtils.nextFloat(min, max);
    }


    public Path sinkResultsToFile(final String path, final List<Record> result) {

        final String outFile = "dataToIndex" + UUID.randomUUID().toString().replace("-", "_") + ".txt";

        Path p = Paths.get(path, outFile);
        try {
            Files.deleteIfExists(p);
            Files.createFile(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(p)) {

            for (Record record : result) {

                String recordAsString = record.toString();
                bufferedWriter.write(recordAsString);
                bufferedWriter.newLine();
            }

            return p;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
