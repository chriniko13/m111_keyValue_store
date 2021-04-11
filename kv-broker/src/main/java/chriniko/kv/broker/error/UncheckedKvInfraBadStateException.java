package chriniko.kv.broker.error;

import lombok.Getter;

public class UncheckedKvInfraBadStateException extends RuntimeException {

    @Getter
    private final KvInfraBadStateException error;

    public UncheckedKvInfraBadStateException(KvInfraBadStateException error) {
        this.error = error;
    }
}
