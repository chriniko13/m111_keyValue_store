package chriniko.kv.broker;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor

@EqualsAndHashCode

@ToString
public class KvServerContactPoint {

    private final String serverName;
    private final String host;
    private final int port;

}
