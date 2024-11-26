package lexer.TokenGenerators;

import syspro.tm.lexer.BuiltInType;
import syspro.tm.lexer.IntegerLiteralToken;
import syspro.tm.lexer.Token;

import java.util.regex.Pattern;

public class IntegerGenerator extends TokenGenerator {

    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");

    private int index;
    private BuiltInType type;
    private boolean hasTypeSuffix;

    public IntegerGenerator(String sequence,
                            StringBuilder buffer,
                            int start,
                            int leadingTrivialLen,
                            int trailingTrivialLen) {

        super(sequence, buffer, start, leadingTrivialLen, trailingTrivialLen);
        this.index = buffer.length();
        getBuffer();
    }

    @Override
    protected void getBuffer(){
        while (this.index < sequence.length()) {
            int currentChar = sequence.codePointAt(index);

            if (DIGIT_PATTERN.matcher(Character.toString(currentChar)).matches()) {
                buffer.appendCodePoint(currentChar);
            } else {
                break;
            }
            this.index++;
        }

        String suffix = "";
        if (index < sequence.length() - 2) {
            int suffixStart = index;
            int suffixEnd = Math.min(index + 3, sequence.length());
            suffix = sequence.substring(suffixStart, suffixEnd);

            switch (suffix) {
                case "i32" -> {
                    type = BuiltInType.INT32;
                    buffer.append("i32");
                }
                case "i64" -> {
                    type = BuiltInType.INT64;
                    buffer.append("i64");
                }
                case "u32" -> {
                    type = BuiltInType.UINT32;
                    buffer.append("u32");
                }
                case "u64" -> {
                    type = BuiltInType.UINT64;
                    buffer.append("u64");
                }
                default -> {
                    type = BuiltInType.UINT64;
                    hasTypeSuffix = false;
                    return;
                }
            }
            hasTypeSuffix = true;
        }
        else {
            type = BuiltInType.INT64;
            hasTypeSuffix = false;
        }

    }

    @Override
    public Token getToken() {
        if (hasTypeSuffix){
            return new IntegerLiteralToken(
                    start,
                    computeEnd(),
                    leadingTrivialLen,
                    trailingTrivialLen,
                    type,
                    hasTypeSuffix,
                    Integer.parseInt(buffer.substring(0, buffer.toString().length() - 3)));
        }
        else {
            return new IntegerLiteralToken(
                    start,
                    computeEnd(),
                    leadingTrivialLen,
                    trailingTrivialLen,
                    type,
                    hasTypeSuffix,
                    Integer.parseInt(buffer.toString()));
        }
    }

}
