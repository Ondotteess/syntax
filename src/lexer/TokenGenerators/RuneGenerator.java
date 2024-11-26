package lexer.TokenGenerators;

import syspro.tm.lexer.RuneLiteralToken;
import syspro.tm.lexer.Token;

public class RuneGenerator extends TokenGenerator {

    private int runeCodePoint;

    public RuneGenerator(String sequence,
                         StringBuilder buffer,
                         int start,
                         int leadingTrivialLen,
                         int trailingTrivialLen) {

        super(sequence, buffer, start, leadingTrivialLen, trailingTrivialLen);
        getBuffer();
    }

    @Override
    protected void getBuffer() {
        if (sequence.length() < 3 || sequence.charAt(0) != '\'' || sequence.charAt(sequence.length() - 1) != '\'') {
            return;
        }

        String rc = sequence.substring(1, sequence.length() - 1);

        if (rc.startsWith("\\")) {
            this.runeCodePoint = parseEscapedRune(rc.substring(1));
        } else {
            this.runeCodePoint = rc.codePointAt(0);
        }
    }

    @Override
    public Token getToken() {
        int end = start + tokenLen() - 1;
        return new RuneLiteralToken(
                start,
                end,
                leadingTrivialLen,
                trailingTrivialLen,
                runeCodePoint
        );
    }

    @Override
    public int computeEnd() {
        return start + tokenLen() - 2 + leadingTrivialLen + trailingTrivialLen;
    }

    private int parseEscapedRune(String escaped) {

        if (escaped.startsWith("U+")) {
            int codePoint = Integer.parseInt(escaped.substring(2), 16);
            if (Character.isValidCodePoint(codePoint)) {
                return codePoint;
            }
        }

        else if (escaped.startsWith("\\u")) {
            if (escaped.length() == 6) {
                int codePoint = Integer.parseInt(escaped.substring(2), 16);
                if (Character.isValidCodePoint(codePoint)) {
                    return codePoint;
                }
            }
        }
        return -1;
    }

}
