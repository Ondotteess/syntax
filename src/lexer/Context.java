package lexer;

public class Context {

    public int start;
    public int end;
    public int leadingTriviaLength;
    public int trailingTriviaLength;
    public int currentIndentLength;
    public int currentIndentLevel;

    public Context() {
        this.start = 0;
        this.end = 0;
        this.leadingTriviaLength = 0;
        this.trailingTriviaLength = 0;
        this.currentIndentLength = 0;
        this.currentIndentLevel = 0;
    }

}

