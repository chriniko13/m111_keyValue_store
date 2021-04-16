package com.chriniko.sample_app;

import chriniko.kv.broker.api.ConsistencyLevel;
import chriniko.kv.broker.api.KvServerContactPoint;
import chriniko.kv.broker.api.QueryKey;
import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.broker.operation.KvBroker;
import chriniko.kv.datatypes.ListValue;
import chriniko.kv.datatypes.StringValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import chriniko.kv.protocol.NotOkayResponseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.*;

public class SampleAppRun {

    public static void main(String[] args) throws Exception {

        String host = args[0];
        int port1 = Integer.parseInt(args[1]);
        int port2 = Integer.parseInt(args[2]);
        int port3 = Integer.parseInt(args[3]);

//        String host = "localhost";
//        int port1 = 8081;
//        int port2 = 8082;
//        int port3 = 8083;

        ExecutorService workerPool = Executors.newCachedThreadPool();

        final CountDownLatch brokerIsReady = new CountDownLatch(1);
        final int replicationFactor = 2;
        final KvBroker kvBroker = new KvBroker();

        CompletableFuture.runAsync(() -> {

            try {
                kvBroker.start(
                        Arrays.asList(
                                new KvServerContactPoint("server1", host, port1),
                                new KvServerContactPoint("server2", host, port2),
                                new KvServerContactPoint("server3", host, port3)
                        ),
                        null,
                        false,
                        replicationFactor,
                        brokerIsReady::countDown
                );
            } catch (NotOkayResponseException | IOException | ErrorReceivedFromKvServerException e) {
                e.printStackTrace(System.err);
                System.exit(-1);
            }

        }, workerPool);


        final boolean reachedZero = brokerIsReady.await(15, TimeUnit.SECONDS);
        if (!reachedZero)  {
            System.err.println("broker could not start");
            System.exit(-2);
        }


        // -------------------------------------------------------------------------------------------------------------
        // put
        System.out.println("\n\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~put~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        kvBroker.put("chrinikoData",
                DatatypesAntlrParser.process("{ \"_studentDetails\" : [ { \"_username\" : \"chriniko\" } ; { \"_email\" : \"chriniko\" } ; { \"_address\" : [ { \"_street\" : \"Panepistimioupoli 123, Kesariani\" } ; { \"_postCode\" : \"16121\" } ; { \"_city\" : \"Athens\" } ; { \"_country\" : \"Greece\" } ] } ; { \"_name\" : [ { \"_firstname\" : \"Nikolaos\" } ; { \"_surname\" : \"Christidis\" } ] } ] }"),
                ConsistencyLevel.ALL);


        // -------------------------------------------------------------------------------------------------------------
        // get
        System.out.println("\n\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~get~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Optional<Value<?>> getResult = kvBroker.get("chrinikoData", ConsistencyLevel.ALL);
        if (getResult.isEmpty()) {
            System.out.println("error in get operation");
            System.exit(-3);
        }

        Value<?> v = getResult.get();
        System.out.println("value: " + v.asString());


        // -------------------------------------------------------------------------------------------------------------
        // query
        System.out.println("\n\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~query~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        // when
        Optional<Value<?>> queryResult = kvBroker.query("chrinikoData", QueryKey.build("_studentDetails", "_username"), ConsistencyLevel.ALL);


        // then
        if (queryResult.isEmpty()) {
            System.exit(-11);
            throw new IllegalStateException(); // satisfy compiler
        }

        Value<?> value = queryResult.get();
        System.out.println("QUERY 1: " + value.asString());



        // =============================================================================================================
        // when
        queryResult = kvBroker.query("chrinikoData", QueryKey.build("_studentDetails", "_address", "_street"), ConsistencyLevel.ALL);


        // then
        if (queryResult.isEmpty()) {
            System.exit(-12);
            throw new IllegalStateException(); // satisfy compiler
        }

        value = queryResult.get();
        System.out.println("QUERY 2: " + value.asString());



        // =============================================================================================================
        // when
        queryResult = kvBroker.query("chrinikoData", QueryKey.build("_studentDetails", "_name"), ConsistencyLevel.ALL);


        // then
        if (queryResult.isEmpty()) {
            System.exit(-13);
            throw new IllegalStateException(); // satisfy compiler
        }

        value = queryResult.get();
        System.out.println("QUERY 3: " + value.asString());


        // =============================================================================================================
        // when
        queryResult = kvBroker.query("chrinikoData", QueryKey.build("_studentDetails", "_name", "_firstname"), ConsistencyLevel.ALL);


        // then
        if (queryResult.isEmpty()) {
            System.exit(-14);
            throw new IllegalStateException(); // satisfy compiler
        }

        value = queryResult.get();
        System.out.println("QUERY 4: " + value.asString());


        // =============================================================================================================
        // when
        queryResult = kvBroker.query("chrinikoData", QueryKey.build("_studentDetails", "_name", "_surname"), ConsistencyLevel.ALL);


        // then
        if (queryResult.isEmpty()) {
            System.exit(-15);
            throw new IllegalStateException(); // satisfy compiler
        }

        value = queryResult.get();
        System.out.println("QUERY 5: " + value.asString());




        // -------------------------------------------------------------------------------------------------------------
        // delete
        System.out.println("\n\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~delete~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Optional<Value<?>> deleteResult = kvBroker.delete("chrinikoData", ConsistencyLevel.ALL);
        if (deleteResult.isEmpty()) {
            System.out.println("error in delete operation (1)");
            System.exit(-5);
        }
        getResult = kvBroker.get("chrinikoData", ConsistencyLevel.ALL);
        if (getResult.isPresent()) {
            System.out.println("error in delete operation (2)");
            System.exit(-6);
        }



        kvBroker.stop();
        workerPool.shutdown();

        System.exit(0);

    }
}
