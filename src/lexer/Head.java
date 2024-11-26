package lexer;

public class Head {

    private final String s;
    private int startSubsequence;
    private int endSubSequence;
    private boolean endOfFile;

    public Head(String s) {
        this.s = s;
        this.startSubsequence = 0;
        this.endSubSequence = 0;
        this.endOfFile = false;
    }

    private int getCode() {
        if (endSubSequence < s.length()) {
            return s.codePointAt(endSubSequence);
        }
        return -1;
    }

    private void readComment(StringBuilder buff) {
        while (getCode() != '\n' && getCode() != -1) {
            int codePoint = getCode();
            buff.appendCodePoint(codePoint);
            endSubSequence += Character.charCount(codePoint);
        }

        if (getCode() == '\n') {
            int codePoint = getCode();
            buff.appendCodePoint(codePoint);
            endSubSequence += Character.charCount(codePoint);
        }

        if (getCode() == '#') {
            readComment(buff);
        }
    }

    private String readTrivial() {
        StringBuilder trivialBuffer = new StringBuilder();
        int codePoint = getCode();

        while (codePoint == ' ' || codePoint == '\t' || codePoint == '\n' || codePoint == '#' || codePoint == '\r') {
            if (codePoint == '#') {
                readComment(trivialBuffer);
            } else {
                trivialBuffer.appendCodePoint(codePoint);
                endSubSequence += Character.charCount(codePoint);
            }
            codePoint = getCode();
        }

        if (codePoint == -1) {
            endOfFile = true;
        }

        return trivialBuffer.toString();
    }

    private String readString() {
        StringBuilder stringBuffer = new StringBuilder();
        int codePoint = getCode();

        if (codePoint == '"') {
            stringBuffer.appendCodePoint(codePoint);
            endSubSequence += Character.charCount(codePoint);
            codePoint = getCode();

            while (codePoint != '"' && codePoint != -1 && codePoint != '\n') {
                if (codePoint == '\\') {
                    stringBuffer.appendCodePoint(codePoint);
                    endSubSequence += Character.charCount(codePoint);
                    codePoint = getCode();
                    if (codePoint != -1) {
                        stringBuffer.appendCodePoint(codePoint);
                        endSubSequence += Character.charCount(codePoint);
                    }
                } else {
                    stringBuffer.appendCodePoint(codePoint);
                    endSubSequence += Character.charCount(codePoint);
                }
                codePoint = getCode();
            }

            if (codePoint == '"') {
                stringBuffer.appendCodePoint(codePoint);
                endSubSequence += Character.charCount(codePoint);
            }
        }
        return stringBuffer.toString();
    }

    private String readTokenSequence() {
        StringBuilder tokenBuffer = new StringBuilder();
        int codePoint = getCode();

        if (codePoint == '"') {
            return readString();
        }

        while (codePoint != ' ' && codePoint != '\t' &&
                codePoint != '\n' && codePoint != '#' &&
                codePoint != -1 && codePoint != '"' && codePoint != '\r') {
            tokenBuffer.appendCodePoint(codePoint);
            endSubSequence += Character.charCount(codePoint);
            codePoint = getCode();
        }

        if (codePoint == -1) {
            endOfFile = true;
        }

        return tokenBuffer.toString();
    }

    private SequenceInfo read() {
        int startTrivial = endSubSequence;
        String initialTrivial = readTrivial();

        int startToken = endSubSequence;
        String tokenSequence = readTokenSequence();

        String trailingTrivial = readTrivial();

        if (tokenSequence.isEmpty() && initialTrivial.isEmpty()) {
            endOfFile = true;
            return null;
        }

        int leadingTriviaLength = initialTrivial.length();
        int trailingTriviaLength = trailingTrivial.length();

        String fullValue = initialTrivial + tokenSequence + trailingTrivial;

        if (!initialTrivial.isEmpty()) {
            return new SequenceInfo(fullValue, startTrivial, endSubSequence, leadingTriviaLength, trailingTriviaLength, endOfFile);
        } else {
            return new SequenceInfo(fullValue, startToken, endSubSequence, leadingTriviaLength, trailingTriviaLength, endOfFile);
        }
    }


    public SequenceInfo readSequence() {
        SequenceInfo sequenceInfo = null;

        if (!endOfFile) {
             sequenceInfo = read();
            if (sequenceInfo != null) {
                //System.out.println(sequenceInfo);
            }
        }
        return sequenceInfo;
    }
}
