package lexer.TokenGenerators;

import syspro.tm.lexer.Symbol;
import syspro.tm.lexer.SymbolToken;
import syspro.tm.lexer.Token;


public class SymbolGenerator extends TokenGenerator {

    Symbol symbol;
    public SymbolGenerator(String sequence,
                           StringBuilder buffer,
                           int start,
                           int leadingTrivialLen,
                           int trailingTrivialLen) {

        super(sequence, buffer, start, leadingTrivialLen, trailingTrivialLen);
        getBuffer();
    }

    @Override
    protected void getBuffer() {
        if (sequence.length() > 1) {
            int codeNext = sequence.codePointAt(1);
            String tempBuffer = buffer.toString() + (char) codeNext;

            if (tempBuffer.equals("&&") || tempBuffer.equals("||") ||
                    tempBuffer.equals("<=") || tempBuffer.equals(">=") ||
                    tempBuffer.equals("<<") || tempBuffer.equals(">>") ||
                    tempBuffer.equals("==") || tempBuffer.equals("!=") ||
                    tempBuffer.equals("<:")) {

                buffer.appendCodePoint(codeNext);

            }
        }
        symbol = switchSymbol(buffer.toString());
    }

    private Symbol switchSymbol(String sym) {
        return switch (sym) {
            case "." -> Symbol.DOT;
            case ":" -> Symbol.COLON;
            case "," -> Symbol. COMMA;
            case "+" -> Symbol.PLUS;
            case "-" -> Symbol. MINUS;
            case "*" -> Symbol.ASTERISK;
            case "/" -> Symbol. SLASH;
            case "%" -> Symbol.PERCENT;
            case "!" -> Symbol. EXCLAMATION;
            case "~" -> Symbol.TILDE;
            case "&" -> Symbol. AMPERSAND;
            case "|" -> Symbol.BAR;
            case "&&" -> Symbol. AMPERSAND_AMPERSAND;
            case "||" -> Symbol.BAR_BAR;
            case "^" -> Symbol.CARET;
            case "<" -> Symbol.LESS_THAN;
            case "<=" -> Symbol.LESS_THAN_EQUALS;
            case ">" -> Symbol.GREATER_THAN;
            case ">=" -> Symbol.GREATER_THAN_EQUALS;
            case "<<" -> Symbol.LESS_THAN_LESS_THAN;
            case ">>" -> Symbol.GREATER_THAN_GREATER_THAN;
            case "[" -> Symbol.OPEN_BRACKET;
            case "]" -> Symbol.CLOSE_BRACKET;
            case "(" -> Symbol.OPEN_PAREN;
            case ")" -> Symbol.CLOSE_PAREN;
            case "=" -> Symbol.EQUALS;
            case "==" -> Symbol.EQUALS_EQUALS;
            case "!=" -> Symbol.EXCLAMATION_EQUALS;
            case "?" -> Symbol.QUESTION;
            case "<:" -> Symbol.BOUND;

            default -> throw new IllegalStateException("Unexpected value: " + sym);
        };
    }


    @Override
    public Token getToken() {
        return new SymbolToken(
                start,
                computeEnd(),
                leadingTrivialLen,
                trailingTrivialLen,
                symbol);
    }

}


