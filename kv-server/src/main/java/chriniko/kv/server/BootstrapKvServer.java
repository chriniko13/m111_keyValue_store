package chriniko.kv.server;

import chriniko.kv.server.actuator.ActuatorHttpServer;
import chriniko.kv.server.actuator.KvStorageEngineState;
import chriniko.kv.server.infra.KvServer;
import chriniko.kv.server.infra.KvServerConfig;
import chriniko.kv.server.infra.KvStorageEngine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*

   -a ip_address    ---> ip address
   -p port          ---> port
   -aP actuatorPort ---> actuator port

 */
public class BootstrapKvServer {


    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.err.println("you did not provide the required parameters with the order: -a (required:string), -p (required:integer), -aP (required:integer)");
            System.exit(-1);
            throw new IllegalStateException(); // to satisfy the compiler.
        }

        String hostnameStr = args[0];
        String portStr = args[1];
        String actuatorPortStr = args[2];


        try {
            InetAddress.getByName(hostnameStr);
        } catch (UnknownHostException e) {
            System.err.println("not valid hostname provided");
            System.exit(-2);
            throw new IllegalStateException(); // to satisfy the compiler.
        }

        try {
            Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            System.err.println("not valid port provided");
            System.exit(-3);
            throw new IllegalStateException(); // to satisfy the compiler.
        }

        try {
            Integer.parseInt(actuatorPortStr);
        } catch (NumberFormatException e) {
            System.err.println("not valid actuator port provided");
            System.exit(-4);
            throw new IllegalStateException(); // to satisfy the compiler.
        }



        // --- setup server
        String serverName = "server__" + hostnameStr + ":" + portStr;
        KvServer kvServer = KvServer.create(serverName, KvServerConfig.createDefault());
        KvStorageEngine storageEngine = kvServer.getStorageEngine();


        // --- start actuator
        int actuatorPort = Integer.parseInt(actuatorPortStr);
        KvStorageEngineState kvStorageEngineState = new KvStorageEngineState(storageEngine);
        ActuatorHttpServer actuatorHttpServer = new ActuatorHttpServer(kvStorageEngineState);

        new Thread(() -> {
            actuatorHttpServer.start(actuatorPort);
            System.out.println("actuator is up at port: " + actuatorPort);
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("will shutdown actuator now...");
            actuatorHttpServer.shutdown();
        }));


        // --- start server
        kvServer.run(hostnameStr, Integer.parseInt(portStr), () -> {

            System.out.println("server with name: " + serverName + " is up at host: " + hostnameStr + ":" + portStr);

        });

    }

}
