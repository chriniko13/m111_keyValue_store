package chriniko.kv.broker;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class KvServerHealthStateStats {

    private Instant lastTimeChecked;

    private KvServerHealthState healthState;

}
