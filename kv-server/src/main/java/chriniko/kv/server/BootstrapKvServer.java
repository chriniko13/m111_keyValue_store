package chriniko.kv.server;

import chriniko.kv.protocol.ConnectionConstants;
import chriniko.kv.server.infra.KvServer;
import chriniko.kv.server.infra.KvServerConfig;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*

   -a ip_address    ---> ip address
   -p port          ---> port

 */
public class BootstrapKvServer {


    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("you did not provide the required parameters with the order: -a (required:string), -p (required:integer)");
            System.exit(-1);
            throw new IllegalStateException(); // to satisfy the compiler.
        }

        String hostnameStr = args[0];
        String portStr = args[1];


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


        String serverName = "server__" + hostnameStr + ":" + portStr;
        KvServer kvServer = KvServer.create(serverName, KvServerConfig.createDefault());

        kvServer.run(hostnameStr, Integer.parseInt(portStr), () -> {

            System.out.println("server with name: " + serverName + " is up at host: " + hostnameStr + ":" + portStr);

        });

    }

}
