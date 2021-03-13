package chriniko.kv.datainjector;

import chriniko.kv.datainjector.core.DataGenerator;

public class Bootstrap {

    public static void main(String[] args) throws Exception {


        DataGenerator dataGenerator = new DataGenerator();

        dataGenerator.create(10, 1, 3, 15, null);


    }
}
