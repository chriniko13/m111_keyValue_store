package chriniko.kv.protocol;

import java.util.function.Function;

public class ProtocolConstants {

    // infra constants
    public static final int BYTES_TO_ALLOCATE_PER_BUFFER = 2048;



    // responses
    public static final String OKAY_RESP = "OK";
    public static final String NOT_FOUND_RESP = "NOT_FOUND";
    public static final Function<Integer, String> ERROR_RESP = errorCode -> "ERROR: " + errorCode;

}
