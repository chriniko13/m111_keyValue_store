package chriniko.kv.datatypes.error;

import lombok.Getter;

public class UncheckedParsingException extends RuntimeException {

    @Getter
    private final ParsingException error;

    public UncheckedParsingException(ParsingException e) {
        this.error = e;
    }
}
