package lexer.TokenGenerators;

import syspro.tm.lexer.Keyword;
import syspro.tm.lexer.KeywordToken;
import syspro.tm.lexer.Token;

public class KeywordGenerator extends TokenGenerator {

    private Keyword keyword;
    public KeywordGenerator(String sequence,
                            StringBuilder buffer,
                            int start,
                            int leadingTrivialLen,
                            int trailingTrivialLen) {
        super(sequence, buffer, start, leadingTrivialLen, trailingTrivialLen);
        getBuffer();
    }

    @Override
    protected void getBuffer() {
        switch (buffer.toString()) {
            case "this" -> keyword = Keyword.THIS;
            case "super" -> keyword = Keyword.SUPER;
            case "is" -> keyword = Keyword.IS;
            case "if" -> keyword = Keyword.IF;
            case "else" -> keyword = Keyword.ELSE;
            case "for" -> keyword = Keyword.FOR;
            case "in" -> keyword = Keyword.IN;
            case "while" -> keyword = Keyword.WHILE;
            case "def" -> keyword = Keyword.DEF;
            case "var" -> keyword = Keyword.VAR;
            case "val" -> keyword = Keyword.VAL;
            case "return" -> keyword = Keyword.RETURN;
            case "break" -> keyword = Keyword.BREAK;
            case "continue" -> keyword = Keyword.CONTINUE;
            case "abstract" -> keyword = Keyword.ABSTRACT;
            case "virtual" -> keyword = Keyword.VIRTUAL;
            case "override" -> keyword = Keyword.OVERRIDE;
            case "native" -> keyword = Keyword.NATIVE;
            case "class" -> keyword = Keyword.CLASS;
            case "object" -> keyword = Keyword.OBJECT;
            case "interface" -> keyword = Keyword.INTERFACE;
            case "null" -> keyword = Keyword.NULL;
        }
    }

    @Override
    public Token getToken() {
        return new KeywordToken(
                start,
                computeEnd(),
                leadingTrivialLen,
                trailingTrivialLen,
                keyword);
    }

}
