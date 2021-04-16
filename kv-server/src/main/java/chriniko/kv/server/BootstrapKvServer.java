package chriniko.kv.server;

import chriniko.kv.protocol.ConnectionConstants;
import chriniko.kv.server.infra.KvServer;
import chriniko.kv.server.infra.KvServerConfig;

import java.io.IOException;

public class BootstrapKvServer {


    public static void main(String[] args) throws IOException {

        //TODO use command args...

        String host = ConnectionConstants.DEFAULT_SERVER_HOST;
        int port = ConnectionConstants.DEFAULT_SERVER_PORT;


        KvServer kvServer = KvServer.create("server1", KvServerConfig.createDefault());
        kvServer.run(host, port, () -> {});

    }

}
