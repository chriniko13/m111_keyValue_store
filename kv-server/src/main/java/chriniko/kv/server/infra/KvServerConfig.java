package chriniko.kv.server.infra;

public class KvServerConfig {


    private static final boolean DEFAULT_ASYNC_INDEXING = false;

    private static final boolean DEFAULT_USE_TRIE_FOR_QUERY_SEARCH = true;

    private final boolean asyncIndexing;
    private final boolean useTrieForQuerySearch;

    private KvServerConfig() {
        asyncIndexing = DEFAULT_ASYNC_INDEXING;
        useTrieForQuerySearch = DEFAULT_USE_TRIE_FOR_QUERY_SEARCH;
    }

    private KvServerConfig(boolean asyncIndexing, boolean useTrieForQuerySearch) {
        this.asyncIndexing = asyncIndexing;
        this.useTrieForQuerySearch = useTrieForQuerySearch;
    }

    public static KvServerConfig createDefault() {
        return new KvServerConfig();
    }

    public static KvServerConfig create(boolean asyncIndexing, boolean isDefaultUseTrieForQuerySearch) {
        return new KvServerConfig(asyncIndexing, isDefaultUseTrieForQuerySearch);
    }

    public boolean isAsyncIndexing() {
        return asyncIndexing;
    }

    public boolean isUseTrieForQuerySearch() {
        return useTrieForQuerySearch;
    }
}
