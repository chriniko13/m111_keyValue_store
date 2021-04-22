package chriniko.kv.protocol;

import java.util.function.BiFunction;

public class ProtocolConstants {

    // infra constants
    public static final int BYTES_TO_ALLOCATE_PER_BUFFER = 2048;




    // message  constants
    public static final String INFO_SEP =  "#"; // info separator





    // responses
    public static final String OKAY_RESP = "OK";

    public static final String UNKNOWN_COMMAND_RESP = "UNKNOWN_COMMAND_RECEIVED";
    public static final String NOT_FOUND_RESP = "NOT_FOUND";

    public static final BiFunction<ErrorTypeConstants, String, String> ERROR_RESP = (errorType, errorMsg) -> "ERROR[" + errorType + "] --- message: " + errorMsg;

}
