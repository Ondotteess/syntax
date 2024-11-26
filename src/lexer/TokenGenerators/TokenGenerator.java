package lexer.TokenGenerators;

import syspro.tm.lexer.Token;


public abstract class TokenGenerator {

    String sequence;
    public StringBuilder buffer;
    public int start;
    public int leadingTrivialLen;
    public int trailingTrivialLen;


    public TokenGenerator(String sequence,
                          StringBuilder buffer,
                          int start,
                          int leadingTrivialLen,
                          int trailingTrivialLen) {

        this.sequence = sequence;
        this.buffer = new StringBuilder(buffer);
        this.start = start;
        this.leadingTrivialLen = leadingTrivialLen;
        this.trailingTrivialLen = trailingTrivialLen;
    }

    protected abstract void getBuffer();

    public Token getToken() {
        return null;

    }

    public void addTrivialLengt(int trailingTrivialLen){
        this.trailingTrivialLen += trailingTrivialLen;
    }

    public int computeEnd() {
        return start + tokenLen() + leadingTrivialLen + trailingTrivialLen - 1;
    }

    public int tokenLen() {
        int unicodePointLength = 0;
        for (int i = 0; i < buffer.length(); ) {
            int codePoint = buffer.codePointAt(i);
            unicodePointLength++;
            i += Character.charCount(codePoint);
        }
        return unicodePointLength;
    }

    public int getIndentLevel(){
        return -1;
    }

    public int getIndentLenght(){
        return -1;
    }
}
