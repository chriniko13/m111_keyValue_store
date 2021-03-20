package chriniko.kv.broker;

import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.protocol.ConnectionConstants;
import chriniko.kv.protocol.NotOkayResponseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BootstrapKvBroker {

    public static void main(String[] args) {

        //TODO use provided input args

        List<KvServerContactPoint> kvServerContactPoints = new ArrayList<>(); // arg: s (serverFile.txt)
        kvServerContactPoints.add(new KvServerContactPoint(ConnectionConstants.DEFAULT_SERVER_HOST, ConnectionConstants.DEFAULT_SERVER_PORT));

        int replicationFactor = 1; // arg: k (replicationFactor)

        BufferedReader bufferedReader; // arg: i (dataToIndex.txt)
        InputStream in = BootstrapKvBroker.class.getResourceAsStream("/sampleDataToIndex.txt");
        bufferedReader = new BufferedReader(new InputStreamReader(in));


        try {
            KvBroker kvBroker = new KvBroker();
            kvBroker.start(kvServerContactPoints, bufferedReader, replicationFactor);

            System.out.println("kv-broker started up and connected to all kv-server-contact-points: " + kvServerContactPoints);
            System.out.println("ready for action....");



            while (true); // Note: keep up, so driver-program (user of broker) can execute other methods of the broker.

        } catch (NotOkayResponseException e) {
            System.err.println("not okay response received, error: " + e.getMessage());

            e.printStackTrace(System.err);

            System.exit(-1);

        } catch (IOException e) {
            System.err.println("connectivity issue experienced: " + e.getMessage());

            e.printStackTrace(System.err);

            System.exit(-2);

        } catch (ErrorReceivedFromKvServerException e) {
            System.err.println("kv server error response experienced: " + e.getMessage());

            e.printStackTrace(System.err);

            System.exit(-3);
        }


    }
}
