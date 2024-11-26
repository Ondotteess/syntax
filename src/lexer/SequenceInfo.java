package lexer;

public class SequenceInfo {
    private final String token;
    private final int leadingTriviaLength;
    private final int trailingTriviaLength;
    private final int start;
    private final int end;
    public final boolean lastSequence;

    public SequenceInfo(String token, int start, int end, int leadingTriviaLength, int trailingTriviaLength, boolean lastSequence) {
        this.token = token;
        this.start = start;
        this.end = end;
        this.leadingTriviaLength = leadingTriviaLength;
        this.trailingTriviaLength = trailingTriviaLength;
        this.lastSequence = lastSequence;
    }

    public String getToken() {
        return token;
    }

    public int getLeadingTriviaLength() {
        return leadingTriviaLength;
    }

    public int getTrailingTriviaLength() {
        return trailingTriviaLength;
    }

    @Override
    public String toString() {
        return "TokenInfo{" +
                "token='" + token + '\'' +
                ", start="+ start +
                ", end="+ end +
                ", leadingTriviaLength=" + leadingTriviaLength +
                ", trailingTriviaLength=" + trailingTriviaLength +
                '}';
    }
}
