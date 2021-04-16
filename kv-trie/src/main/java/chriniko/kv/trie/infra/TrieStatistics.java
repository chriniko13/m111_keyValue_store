package chriniko.kv.trie.infra;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@RequiredArgsConstructor
@Getter
public class TrieStatistics<T> {

    private final int countOfNoCompleteWords;

    private final int countOfCompleteWords;
    private final int countOfCompleteWordsWithOldData;

    private final List<T> values;
}
