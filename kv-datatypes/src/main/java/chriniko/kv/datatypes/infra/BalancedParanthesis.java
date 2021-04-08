package chriniko.kv.datatypes.infra;

import java.util.ArrayDeque;
import java.util.Deque;

public final class BalancedParanthesis {

    public static boolean process(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException();
        }

        char[] chars = s.toCharArray();

        final Deque<Character> stack = new ArrayDeque<>();

        for (char aChar : chars) {
            if (aChar == '{') {
                stack.push(aChar);
            }

            if (aChar == '}') {
                if (!stack.isEmpty() && '{' == stack.peekFirst()) {
                    stack.pop();
                } else {
                    stack.push(aChar);
                }
            }
        }

        return stack.isEmpty();
    }

}
