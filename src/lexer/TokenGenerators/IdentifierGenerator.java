package lexer.TokenGenerators;

import syspro.tm.lexer.*;

import java.util.regex.Pattern;

public class IdentifierGenerator extends TokenGenerator {

    private final Pattern ID_CONTINUE_PATTERN = Pattern.compile("[\\p{L}\\p{Nl}\\p{Nd}\\p{Pc}\\p{Mn}\\p{Mc}\\p{Cf}]");
    private int index;

    private Keyword getContextualKeyword(String word){
        return switch (word) {
            case "class" -> Keyword.CLASS;
            case "null" -> Keyword.NULL;
            case "interface" -> Keyword.INTERFACE;
            case "object" -> Keyword.OBJECT;
            default -> null;
        };
    }

    public IdentifierGenerator(String sequence,
                               StringBuilder buffer,
                               int start,
                               int leadingTrivialLen,
                               int trailingTrivialLen) {

        super(sequence, buffer, start, leadingTrivialLen,trailingTrivialLen);
        this.index = buffer.length();
    }

    @Override
    protected void getBuffer() {

    }

    @Override
    public Token getToken() {

            while (index < sequence.length()) {
                int currentChar = sequence.codePointAt(index);
                if (ID_CONTINUE_PATTERN.matcher(Character.toString(currentChar)).matches()) {
                    buffer.appendCodePoint(currentChar);
                } else {
                    break;
                }
                index += Character.charCount(currentChar);
            }

        return new IdentifierToken(start,
                                    computeEnd(),
                                    leadingTrivialLen,
                                    trailingTrivialLen,
                                    buffer.toString(),
                                    getContextualKeyword(buffer.toString()));

    }

}