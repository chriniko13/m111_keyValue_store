package chriniko.kv.broker;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;

public class AvailablePortInfra {


    public static LinkedList<Integer> availablePorts(int count) throws IOException {

        final LinkedList<Integer> result = new LinkedList<>();

        for (int i = 1; i <= count; i++) {
            result.add(findRandomOpenPortOnAllLocalInterfaces());
        }

        return result;
    }

    private static Integer findRandomOpenPortOnAllLocalInterfaces() throws IOException {
        try (ServerSocket socket = new ServerSocket(0);) {
            return socket.getLocalPort();
        }
    }

}
