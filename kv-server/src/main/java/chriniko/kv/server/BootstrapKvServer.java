package chriniko.kv.server;

import chriniko.kv.protocol.ConnectionConstants;

import java.io.IOException;

public class BootstrapKvServer {


    public static void main(String[] args) throws IOException {

        //TODO use command args...

        String host = ConnectionConstants.DEFAULT_SERVER_HOST;
        int port = ConnectionConstants.DEFAULT_SERVER_PORT;

        KvParser kvParser = new KvParser();
        KvServer kvServer = new KvServer(kvParser);

        kvServer.run(host, port);

    }

}
