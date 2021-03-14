package chriniko.kv.broker;

import chriniko.kv.protocol.NotOkayResponseException;
import chriniko.kv.protocol.Operations;
import chriniko.kv.protocol.ProtocolConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/*
    The broker should start with the following command:
        kvBroker -s serverFile.txt -i dataToIndex.txt -k 2

    The  serverFile.txt is a space separated list of server IPs and their respective ports that will be
    listening for queries and indexing commands. For example:
    123.123.12.12 8000
    123.123.12.12 8001
    123.2.3.4 9000

    Is an example of a serverfile indicating that this broker will be working with 3 servers with the IPs
    described and on the respective ports described.

    The dataToIndex.txt is a file containing data that was output from the previous part of the project (check data-injector) that was generating the data.

    The k value is the replication factor, i.e. how many different servers will have the same replicated data.
 */
public class KvBroker {

    private static final ConcurrentHashMap<KvServerContactPoint, KvServerClient> kvServerClients = new ConcurrentHashMap<>();

    public void start(List<KvServerContactPoint> kvServerContactPoints,
                      BufferedReader dataToIndexBufferedReader,
                      int replicationFactor) throws NotOkayResponseException, IOException {

        if (kvServerContactPoints.size() < replicationFactor) {
            throw new IllegalArgumentException("provided kv-server contact points(-s) are less than provided replicationFactor(-k)");
        }


        // Note: first try to connect to all provided contact points
        for (KvServerContactPoint kvServerContactPoint : kvServerContactPoints) {

            try {
                KvServerClient kvServerClient = KvServerClient.start(kvServerContactPoint.getHost(), kvServerContactPoint.getPort());

                String response = kvServerClient.sendMessage(Operations.HEALTH_CHECK.getMsgOp());
                System.out.println("response received from kv-server: " + kvServerContactPoint + ", response: " + response);

                if (ProtocolConstants.OKAY_RESP.equals(response)) {

                    System.out.println("connected successfully to kv-server: " + kvServerContactPoint);
                    kvServerClients.put(kvServerContactPoint, kvServerClient);

                } else {
                    System.err.println("not received okay response from kv-server: " + kvServerContactPoint);

                    String msg = "not received okay response from kv-server: " + kvServerContactPoint + ", response: " + response;
                    throw new NotOkayResponseException(msg);
                }

            } catch (IOException e) {
                System.err.println("could not connect to kv-server: " + kvServerContactPoint + " because it was during of the bootstrap of the kv-broker it will fail");
                throw e;
            }
        }

        // Note: do our housekeeping stuff when kv broker gets terminated.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("will close all kvServerClients now");

            kvServerClients.forEach(((kvServerContactPoint, kvServerClient) -> {
                try {
                    kvServerClient.stop();
                } catch (IOException ignored) {
                }
            }));

        }));



        // Note: now that we have connected to all kv-servers, time to start healthCheck thread-worker, which at a fixed time interval will
        //       check for the availability (is up?) for all the kvServerContactPoints and keep track of the status on a map
        // TODO....





        // Note: now is time for each line of generated data to randomly pick k(replicationFactor) servers and send a request of the form PUT data.
        // TODO...


    }


    /**
     * This method should be used after a call to {@link KvBroker#start(List, BufferedReader, int)} has been made.
     * @param op
     * @return
     */
    public String executeOperation(Operations op) {


        // TODO add logic...

        throw new UnsupportedOperationException("TODO");
    }
}
