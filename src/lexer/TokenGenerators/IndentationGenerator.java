package lexer.TokenGenerators;

import syspro.tm.lexer.IndentationToken;
import syspro.tm.lexer.Token;

public class IndentationGenerator extends TokenGenerator{

    private final int currentIndentLength;
    private final int currentIndentLevel;
    private int newLevel;
    private int newLenght;
    private int difference;
    private boolean endOfFile;

    public IndentationGenerator(String sequence,
                                StringBuilder buffer,
                                int start,
                                int leadingTrivialLen,
                                int trailingTrivialLen,
                                int currentIndentLength,
                                int currentIndentLevel,
                                boolean endOfFile) {

        super(sequence, buffer, start, leadingTrivialLen, trailingTrivialLen);
        this.currentIndentLength = currentIndentLength;
        this.newLenght = currentIndentLength;
        this.currentIndentLevel = currentIndentLevel;
        this.endOfFile = endOfFile;

        getBuffer();
    }

    public IndentationGenerator(IndentationGenerator other) {
        super(other.sequence, new StringBuilder(other.buffer), other.start, other.leadingTrivialLen, other.trailingTrivialLen);
        this.currentIndentLength = other.currentIndentLength;
        this.currentIndentLevel = other.currentIndentLevel;
        this.newLevel = other.newLevel;
        this.newLenght = other.newLenght;
        this.difference = other.difference;
        this.endOfFile = other.endOfFile;
    }

    private void setNews(int indentLength){
        if (endOfFile && indentLength == trailingTrivialLen) {
            newLevel = 0;
        }
        else if (indentLength == 0) {
            newLevel = 0;
        }
        else if (indentLength % 2 != 0) {
            newLevel = currentIndentLevel;
            newLenght = indentLength;
        } else if (currentIndentLevel == 0) {
            newLenght = indentLength;
            newLevel = 1;
        } else if (indentLength % currentIndentLength != 0) {
            newLevel = currentIndentLevel;
        }  else if (indentLength % currentIndentLength == 0) {
            newLevel = indentLength / currentIndentLength;
        }
    }

    public void setEndOfFile(){
        this.endOfFile = true;
    }

    protected void getBuffer() {
        buffer.setLength(0);
        int currentIndex = 0;
        int indentLength = 0;
        int leadTriv = 0;
        int trailTriv = 0;
        boolean newlineFound = false;

        while (currentIndex < sequence.length()) {
            int currentCodePoint = sequence.codePointAt(currentIndex);

            buffer.appendCodePoint(currentCodePoint);

            if (currentCodePoint == '\n') {
                newlineFound = true;

                if (currentIndex == sequence.length() - 1
                        || (currentIndex == sequence.length() - 2 && sequence.codePointAt(currentIndex + 1) == '\r')) {
                    newLevel = currentIndentLevel;
                    break;
                } else if (currentIndex < sequence.length() - 1 && sequence.codePointAt(currentIndex + 1) == '\n') {
                    newLevel = currentIndentLevel;
                }

                currentIndex++;
                while (currentIndex < sequence.length()) {
                    int nextChar = sequence.codePointAt(currentIndex);
                    if (nextChar == ' ' || nextChar == '\t') {
                        buffer.appendCodePoint(nextChar);
                        indentLength++;
                        trailTriv++;
                        currentIndex++;
                    } else if (nextChar == '\n' || nextChar == '\r') {
                        newLevel = currentIndentLevel;
                        return;
                    } else if (nextChar == '#') {
                        buffer.appendCodePoint(nextChar);
                        trailTriv++;
                        currentIndex++;
                        while (currentIndex < sequence.length()) {
                            nextChar = sequence.codePointAt(currentIndex);
                            if (nextChar == '\n' || nextChar == '\r') {
                                break;
                            }
                            buffer.appendCodePoint(nextChar);
                            trailTriv++;
                            currentIndex++;
                        }
                        break;
                    } else {
                        break;
                    }
                }
                break;
            } else {
                currentIndex += Character.charCount(currentCodePoint);
            }
            if (!newlineFound) {
                leadTriv++;
            }
        }

        if (buffer.toString().contains("\r")) {
            leadingTrivialLen += (leadTriv - 1);
        }
        else {
            leadingTrivialLen += leadTriv;
        }
        trailingTrivialLen = trailTriv;
        setNews(indentLength);
    }

    @Override
    public int getIndentLevel(){
        return newLevel;
    }

    @Override
    public int getIndentLenght(){
        return newLenght;
    }

    public void setDifference(int difference) {
        this.difference = difference;
    }

    @Override
    public Token getToken() {
        int end = computeEnd();
        if (!buffer.toString().contains("\n")) {
            end = start;
        }
        //System.out.println("start: "+ start + " end: " + end + " len: " + (end - start - 1));

        return new IndentationToken(start,
                                    end,
                                    leadingTrivialLen,
                                    trailingTrivialLen,
                                    difference);

    }

    @Override
    public int computeEnd() {
        return start + leadingTrivialLen + trailingTrivialLen + (buffer.indexOf("\r") != -1 ? 1 : 0) ;
    }

    public int getDifference(){
        return newLevel - currentIndentLevel;
    }

}
