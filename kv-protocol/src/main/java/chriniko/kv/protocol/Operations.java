package chriniko.kv.protocol;


public enum Operations {

    // infra-checks
    HEALTH_CHECK("HEALTH"),


    // operations
    GET("GET"),
    PUT("PUT"),
    DELETE("DELETE"),
    QUERY("QUERY");


    private final String msgOp;

    Operations(String msgOp) {
        this.msgOp = msgOp;
    }

    public String getMsgOp() {
        return msgOp;
    }
}
