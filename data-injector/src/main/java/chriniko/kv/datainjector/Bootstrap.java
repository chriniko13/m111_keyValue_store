package chriniko.kv.datainjector;

import chriniko.kv.datainjector.core.DataGenerator;
import chriniko.kv.datainjector.core.Record;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Bootstrap {

    /*
        Parameters passed:
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
    public static void main(String[] args) throws Exception {

        // Note: extract console parameters
        if (args == null || args.length < 4 || args.length > 5) {
            System.err.println("you did not provide the required parameters with the order: -n (required:integer), -d (required:integer), -m (required:integer), " +
                    "-l (required:integer), -k (not-required:string(filepath), if not provided will use the sampleKeyFile.txt)");

            System.exit(-1);
        }

        int noOfLines = Integer.parseInt(args[0]);
        int depth= Integer.parseInt(args[1]);
        int keysPerValue= Integer.parseInt(args[2]);
        int stringLength= Integer.parseInt(args[3]);

        final String keyFile;
        if (args.length == 5) {
            keyFile = args[4];
        } else {
            keyFile = null;
        }


        // Note: generate file
        final Path tempDirectory = Files.createTempDirectory("data-injector-dir");

        final DataGenerator dataGenerator = new DataGenerator();
        List<Record> records = dataGenerator.create(noOfLines, depth, keysPerValue, stringLength, keyFile);
        Path resultPath = dataGenerator.sinkResultsToFile(tempDirectory.toString(), records);


        System.out.println("data generator finished successfully, check generated file at: " + resultPath);

        System.exit(0);
    }
}
