package syntax;

import syspro.tm.lexer.IndentationToken;
import syspro.tm.lexer.SymbolToken;
import syspro.tm.lexer.Token;

import java.util.List;

public class Context {
    private final List<Token> tokens;
    private int position;

    public Context(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
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

    public void insertTokenAtCurrent(SymbolToken token) {
        if (position < 0 || position > tokens.size()) {
            throw new IndexOutOfBoundsException("Invalid position for token insertion.");
        }
        tokens.add(position, token);
    }

}
