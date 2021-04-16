package chriniko.kv.server.actuator;

import chriniko.kv.server.infra.KvRecord;
import chriniko.kv.server.infra.KvStorageEngine;
import chriniko.kv.trie.error.ReadLockAcquireFailureException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedList;

public class KvStorageEngineState {

    private final KvStorageEngine kvStorageEngine;

    private final ObjectMapper objectMapper;

    public KvStorageEngineState(KvStorageEngine storageEngine) {
        this.kvStorageEngine = storageEngine;
        this.objectMapper = new ObjectMapper();
    }

    public String snapshot() throws ReadLockAcquireFailureException, JsonProcessingException {

        final LinkedList<String> records = new LinkedList<>();

        for (KvRecord kvRecord : kvStorageEngine.allRecords()) {

            String record = kvRecord.key() + ":" + kvRecord.value().asString();
            records.add(record);
        }

        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (String record : records) {
            arrayNode.add(record);
        }


        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.set("storage-engine-snapshot", arrayNode);


        return objectMapper.writeValueAsString(objectNode);
    }
}
