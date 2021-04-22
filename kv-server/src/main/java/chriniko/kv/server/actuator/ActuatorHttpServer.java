package chriniko.kv.server.actuator;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class ActuatorHttpServer {

    private final KvStorageEngineState kvStorageEngineState;

    private Undertow server;

    public ActuatorHttpServer(KvStorageEngineState kvStorageEngineState) {
        this.kvStorageEngineState = kvStorageEngineState;
    }

    public void start(int port) {

        server = Undertow.builder()
                .addHttpListener(port, "localhost")

                //todo fix url...
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange x) throws Exception {
                        x.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

                        try {
                            String snapshot = kvStorageEngineState.snapshot();
                            x.getResponseSender().send(snapshot);
                        } catch (Exception e) {
                            x.getResponseSender().send("ERROR OCCURRED: " + e.getMessage());
                        }
                    }
                })
                .build();

        server.start();
    }

    public void shutdown() {
        server.stop();
    }
}
