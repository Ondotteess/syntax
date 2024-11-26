package lexer.TokenGenerators;

import syspro.tm.lexer.BadToken;
import syspro.tm.lexer.StringLiteralToken;
import syspro.tm.lexer.Token;

public class StringGenerator extends TokenGenerator {

    private int currentPosition;
    private Token outToken;
    public int read_tokens;

    public StringGenerator(String sequence,
                           StringBuilder buffer,
                           int start,
                           int leadingTrivialLen,
                           int trailingTrivialLen) {
        super(sequence, buffer, start, leadingTrivialLen, trailingTrivialLen);
        this.currentPosition = 1;
        this.read_tokens = 0;
        getBuffer();
    }

    @Override
    protected void getBuffer() {
        while (currentPosition < sequence.length()) {
            int currentChar = sequence.codePointAt(currentPosition);


            if (currentChar == '"') {
                buffer.appendCodePoint(currentChar);
                read_tokens++;
                currentPosition += Character.charCount(currentChar);
                this.outToken = new StringLiteralToken(
                        start,
                        start + read_tokens + leadingTrivialLen + trailingTrivialLen,
                        leadingTrivialLen,
                        trailingTrivialLen,
                        buffer.substring(1, buffer.length() - 1)
                );
                return;
            }


            if (currentChar == '\\') {
                currentPosition += Character.charCount(currentChar);
                if (currentPosition >= sequence.length() || !processEscapeSequence()) {
                    generateBadToken();
                    return;
                }
                read_tokens++;
            }

            else if (currentChar == '\r' || currentChar == '\n') {
                generateBadToken();
                return;
            } else {
                buffer.appendCodePoint(currentChar);
                currentPosition += Character.charCount(currentChar);
                read_tokens++;
            }
        }


        generateBadToken();
    }

    private boolean processEscapeSequence() {
        if (currentPosition >= sequence.length()) {
            return false;
        }

        int escapedChar = sequence.codePointAt(currentPosition);
        switch (escapedChar) {
            case 'n' -> buffer.append('\n');
            case 't' -> buffer.append('\t');
            case 'r' -> buffer.append('\r');
            case '"' -> buffer.append('"');
            case '\\' -> buffer.append('\\');
            case 'u' -> {
                return processUnicodeEscape(4);
            }
            case 'U' -> {
                if (currentPosition + 1 < sequence.length() && sequence.charAt(currentPosition + 1) == '+') {
                    currentPosition++;
                    read_tokens++;
                    int i = 0;
                    int cp = currentPosition;
                    while (sequence.charAt(cp) != '\\' && sequence.charAt(cp) != '\"') {
                        var c = sequence.codePointAt(cp);
                        read_tokens++;
                        i++;
                        cp += Character.charCount(c);
                    }
                    return processUnicodeEscape(i);
                }
                return false;
            }
            default -> {
                return false;
            }
        }
        currentPosition += Character.charCount(escapedChar);
        return true;
    }

    private boolean processUnicodeEscape(int length) {
        int unicodeStart = currentPosition + 1;
        int unicodeEnd = unicodeStart + length - 1;
        if (unicodeEnd > sequence.length()) {
            return false;
        }

        try {
            String l = "U+" + sequence.substring(unicodeStart, unicodeEnd);

            int codePoint = Integer.parseInt(sequence.substring(unicodeStart, unicodeEnd), 16);
            if (Character.isValidCodePoint(codePoint)) {
                buffer.appendCodePoint(codePoint);
                currentPosition = unicodeEnd ;
                return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    private void generateBadToken() {
        this.outToken = new BadToken(
                start,
                computeEnd(),
                leadingTrivialLen,
                trailingTrivialLen
        );
    }

    @Override
    public Token getToken() {
        return outToken;
    }

    @Override
    public int computeEnd() {
        return start + read_tokens + leadingTrivialLen + trailingTrivialLen;
    }
}
