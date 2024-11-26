package syntax;

import syntax.code.Code;
import syspro.tm.lexer.IndentationToken;
import syspro.tm.lexer.Token;
import syspro.tm.parser.Diagnostic;
import syspro.tm.parser.TextSpan;
import syspro.tm.parser.DiagnosticInfo;
import syspro.tm.parser.ErrorCode;

import java.util.ArrayList;
import java.util.List;

public class Context {
    private final List<Token> tokens;
    private int position;
    private final List<TextSpan> invalidRanges;
    private final List<Diagnostic> diagnostics;

    public Context(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        this.invalidRanges = new ArrayList<>();
        this.diagnostics = new ArrayList<>();
    }

    public Token getToken() {
        return (position < tokens.size()) ? tokens.get(position++) : null;
    }

    public Token lookAhead(int distance) {
        return (position + distance < tokens.size())
                ? tokens.get(position + distance) : null;
    }

    public Token lookAhead() {
        return (position < tokens.size())
                ? tokens.get(position) : null;
    }

    public int getPosition(){
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void invalidRange() {
        invalidRange(position, position + 1);
    }

    public void invalidRange(int startPosition, int endPosition) {
        if (startPosition < tokens.size() && endPosition <= tokens.size()) {
            int start = tokens.get(startPosition).start;
            int end = tokens.get(endPosition - 1).end;
            TextSpan span = TextSpan.fromBounds(start, end);
            String errorMessage = "Syntax error in range [" + start + ", " + end + ")";
            Object[] messages = new Object[]{
                    errorMessage
            };
            invalidRanges.add(span);
            diagnostics.add(new Diagnostic(
                    new DiagnosticInfo(Code.SYNTAX, messages) ,
                    span,
                    List.of()
            ));
        }
    }

    public boolean isEndOfFile() {
        int i = 0;
        while (position + i < tokens.size()) {
            if (!(tokens.get(position + i) instanceof IndentationToken)) {
                return false;
            }
            i++;
        }
        return true;
    }

}
