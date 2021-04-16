package chriniko.kv.broker;

import chriniko.kv.broker.api.KvServerContactPoint;
import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.broker.operation.KvBroker;
import chriniko.kv.protocol.NotOkayResponseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/*
    Parameters:
        -s serverFile.txt      --> file which has hostname and ports so that broker can connect
        -i dataToIndex.txt     --> not mandatory / path to file generated from `data-injector` which contains data that will be send from broker to kv-server
        -k 2                   --> replication factor
 */
public class BootstrapKvBroker {

    public static void main(String[] args) {

        // ============================================================================================================================================================
        // use provided input args
        boolean dataToIndexFilePathProvided;

        if (args.length == 3) {

            dataToIndexFilePathProvided = true;

        } else if (args.length == 2) {

            dataToIndexFilePathProvided = false;

        } else {
            System.err.println("you did not provide the required parameters with the order: -s (required:string-pathToFile), -i (not-required:string-pathToFile), -k (required:int)");
            System.exit(-1);
            throw new IllegalStateException(); // to satisfy the compiler.
        }


        // ============================================================================================================================================================
        // first extract the kv-server contact points (hostname, port)
        final List<KvServerContactPoint> kvServerContactPoints = new ArrayList<>(); // arg: s (serverFile.txt)

        String serverFilePath = args[0];
        Path path = Paths.get(serverFilePath);
        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;
            while ((line = reader.readLine()) != null) {

                String[] splitted = line.split(" ");
                if (splitted.length !=2 ) {
                    System.err.println("invalid (format of lines) serverFile provided");
                    System.exit(-2);
                }

                String hostname = splitted[0];
                String portStr = splitted[1];

                try {
                    InetAddress.getByName(hostname);
                } catch (UnknownHostException e) {
                    System.err.println("not valid hostname provided");
                    System.exit(-3);
                    throw new IllegalStateException(); // to satisfy the compiler.
                }

                try {
                    Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    System.err.println("not valid port provided");
                    System.exit(-4);
                    throw new IllegalStateException(); // to satisfy the compiler.
                }


                String serverName = "server__" + hostname + ":" + portStr;
                KvServerContactPoint kvServerContactPoint = new KvServerContactPoint(serverName, hostname, Integer.parseInt(portStr));
                kvServerContactPoints.add(kvServerContactPoint);
            }

        } catch (IOException e) {
            System.err.println("not valid path for serverFile provided");
            System.exit(-5);
        }




        // ============================================================================================================================================================
        // then proceed with setting up the broker....
        final BufferedReader bufferedReader; // arg: i (dataToIndex.txt)
        final int replicationFactor;

        if (dataToIndexFilePathProvided) { // args.length == 3


            String dataToIndexFilePath = args[1];
            try {
                Path p = Paths.get(dataToIndexFilePath);
                bufferedReader = Files.newBufferedReader(p);
            } catch (IOException e) {
                System.err.println("not valid dataToIndexFilePath provided");
                System.exit(-16);
                throw new IllegalStateException(); // to satisfy the compiler.
            }

            String replicationFactorStr = args[2];
            try {
                replicationFactor = Integer.parseInt(replicationFactorStr);
            } catch (NumberFormatException e) {
                System.err.println("not valid replicationFactor provided");
                System.exit(-17);
                throw new IllegalStateException(); // to satisfy the compiler.
            }


        } else { // args.length == 2

            String replicationFactorStr = args[1];
            try {
                replicationFactor = Integer.parseInt(replicationFactorStr);
            } catch (NumberFormatException e) {
                System.err.println("not valid replicationFactor provided");
                System.exit(-26);
                throw new IllegalStateException(); // to satisfy the compiler.
            }


            InputStream in = BootstrapKvBroker.class.getResourceAsStream("/sampleDataToIndex.txt");
            bufferedReader = new BufferedReader(new InputStreamReader(in));
        }




        // ============================================================================================================================================================
        // start the broker....
        try {
            KvBroker kvBroker = new KvBroker();
            kvBroker.start(kvServerContactPoints, bufferedReader, true, replicationFactor, null);

            System.out.println("kv-broker started up and connected to all kv-server-contact-points: " + kvServerContactPoints);
            System.out.println("ready for action....");


            //TODO implement CLI...
            while (true) ; // Note: keep up, so driver-program (user of broker) can execute other methods of the broker.

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
