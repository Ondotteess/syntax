package syntax.rule;

import syntax.Context;
import syspro.tm.lexer.*;
import syspro.tm.parser.SyntaxNode;

public interface Rule {
    static SyntaxNode parse(Context context) {
        return null;
    }

    static boolean isComma(Token token) {
        return (token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.COMMA));
    }

    static boolean isIf(Token token) {
        return (token instanceof KeywordToken &&
                ((KeywordToken) token).keyword.equals(Keyword.IF));
    }

    static boolean isElse(Token token) {
        return (token instanceof KeywordToken &&
                ((KeywordToken) token).keyword.equals(Keyword.ELSE));
    }

    static boolean isTypeName(Context context) {
        int initialPosition = context.getPosition();    // потом восстановимся
        context.setPosition(initialPosition);

        if (context.lookAhead() instanceof SymbolToken &&
                ((SymbolToken) context.lookAhead()).symbol.equals(Symbol.QUESTION)) {
            context.getToken();
        }
        Token token1 = context.lookAhead();

        if (!(context.lookAhead() instanceof IdentifierToken)) {
            context.setPosition(initialPosition);
            return false;
        }
        context.getToken();
        if (context.lookAhead() instanceof SymbolToken &&
                ((SymbolToken) context.lookAhead()).symbol.equals(Symbol.LESS_THAN)) {
            context.getToken();
            if (!Rule.isIdentifier(context.lookAhead())) {
                context.setPosition(initialPosition);
                return false;
            }
            context.getToken();

            while (context.lookAhead() instanceof SymbolToken &&
                    ((SymbolToken) context.lookAhead()).symbol.equals(Symbol.COMMA)) {
                context.getToken();

                if (!Rule.isIdentifier(context.lookAhead())) {
                    context.setPosition(initialPosition);
                    return false;
                }
            }

            if (!(context.lookAhead() instanceof SymbolToken &&
                    ((SymbolToken) context.lookAhead()).symbol.equals(Symbol.GREATER_THAN))) {
                context.setPosition(initialPosition);
                return false;
            }
            context.getToken();
        }
        context.setPosition(initialPosition);
        return true;
    }


    static boolean isCloseBracket(Token token) {
        return (token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.CLOSE_BRACKET));
    }

    static boolean isDot(Token token) {
        return (token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.DOT));
    }

    static boolean isOpenBracket(Token token) {
        return (token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.OPEN_BRACKET));
    }

    static boolean isIdentifier(Token token) {
        return (token instanceof IdentifierToken);
    }

    static boolean isDef(Token token) {
        return (token instanceof KeywordToken &&
                (((KeywordToken) token).keyword.equals(Keyword.DEF)));
    }

    static boolean isLiteral(Token token) {
        return (token instanceof IntegerLiteralToken ||
                token instanceof RuneLiteralToken ||
                token instanceof StringLiteralToken ||
                token instanceof BooleanLiteralToken);
    }

    static boolean isIntegerLiteral(Token token) {
        return token instanceof IntegerLiteralToken;
    }

    static boolean isRuneLiteral(Token token) {
        return token instanceof RuneLiteralToken;
    }
    static boolean isStringLiteral(Token token) {
        return token instanceof StringLiteralToken;
    }
    static boolean isBooleanLiteral(Token token) {
        return token instanceof BooleanLiteralToken;
    }

    static boolean isThis(Token token){
        return (token instanceof KeywordToken && (((KeywordToken) token).keyword.equals(Keyword.THIS)));
    }

    static boolean isSuper(Token token){
        return (token instanceof KeywordToken && (((KeywordToken) token).keyword.equals(Keyword.SUPER)));
    }

    static boolean isName(Token token) {
        return token instanceof IdentifierToken ||
                (token instanceof KeywordToken &&
                        ((KeywordToken) token).keyword.equals(Keyword.THIS));
    }

    static boolean isColon(Token token) {
        return (token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.COLON));
    }

    static boolean isOpenParan(Token token) {
        return (token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.OPEN_PAREN));
    }

    static boolean isCloseParen(Token token) {
        return (token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.CLOSE_PAREN));
    }

    static boolean isIndent(Token token) {
        return (token instanceof IndentationToken &&
                ((IndentationToken) token).isIndent());
    }

    static boolean isDedent(Token token) {
        return (token instanceof IndentationToken &&
                ((IndentationToken) token).isDedent());
    }

    static boolean isModifier(Token token) {
        return (token instanceof KeywordToken &&
                (((KeywordToken) token).keyword.equals(Keyword.ABSTRACT) ||
                        ((KeywordToken) token).keyword.equals(Keyword.VIRTUAL)   ||
                        ((KeywordToken) token).keyword.equals(Keyword.OVERRIDE)  ||
                        ((KeywordToken) token).keyword.equals(Keyword.NATIVE)));

    }

    static boolean isBound(Token next) {
        return next instanceof SymbolToken && ((SymbolToken) next).symbol.equals(Symbol.BOUND);
    }

    static boolean isAmpersand(Token token){
        return ((token instanceof SymbolToken) && (((SymbolToken) token).symbol.equals(Symbol.AMPERSAND)));
    }

    static boolean isContextual(Token keyword) {
        return keyword instanceof IdentifierToken &&
                (((IdentifierToken) keyword).contextualKeyword == Keyword.CLASS ||
                        ((IdentifierToken) keyword).contextualKeyword == Keyword.OBJECT ||
                        ((IdentifierToken) keyword).contextualKeyword == Keyword.INTERFACE);
    }

    static boolean isLessThan(Token next) {
        return next instanceof SymbolToken && ((SymbolToken) next).symbol.equals(Symbol.LESS_THAN);
    }

    static boolean isVarOrVal(Token token) {
        return (token instanceof KeywordToken &&
                ((((KeywordToken) token)
                        .keyword
                        .equals(Keyword.VAL)) ||
                        (((KeywordToken) token)
                                .keyword
                                .equals(Keyword.VAR))));
    }

    static boolean isEquals(Token token) {
        return token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.EQUALS);
    }

    static boolean isLogicalOr(Token token) {
        return token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.BAR_BAR);
    }

    static boolean isLogicalAnd(Token token) {
        return token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.AMPERSAND_AMPERSAND);
    }

    static boolean isEqualityOperator(Token token) {
        return token instanceof SymbolToken &&
                (((SymbolToken) token).symbol.equals(Symbol.EQUALS_EQUALS) ||
                 ((SymbolToken) token).symbol.equals(Symbol.EXCLAMATION_EQUALS));
    }

    static boolean isRelationalOperator(Token token) {
        return token instanceof SymbolToken &&
                (((SymbolToken) token).symbol.equals(Symbol.LESS_THAN) ||
                 ((SymbolToken) token).symbol.equals(Symbol.GREATER_THAN) ||
                 ((SymbolToken) token).symbol.equals(Symbol.GREATER_THAN_EQUALS) ||
                 ((SymbolToken) token).symbol.equals(Symbol.LESS_THAN_EQUALS));
    }

    static boolean isBitwiseShiftOperator(Token token) {
        return token instanceof SymbolToken &&
                (((SymbolToken) token).symbol.equals(Symbol.LESS_THAN_LESS_THAN) ||
                 ((SymbolToken) token).symbol.equals(Symbol.GREATER_THAN_GREATER_THAN));
    }

    static boolean isAdditive(Token token) {
        return token instanceof SymbolToken &&
                (((SymbolToken) token).symbol.equals(Symbol.PLUS) ||
                 ((SymbolToken) token).symbol.equals(Symbol.MINUS));
    }

    static boolean isMultiplicative(Token token) {
        return token instanceof SymbolToken &&
                (((SymbolToken) token).symbol.equals(Symbol.ASTERISK) ||
                 ((SymbolToken) token).symbol.equals(Symbol.SLASH) ||
                 ((SymbolToken) token).symbol.equals(Symbol.PERCENT));
    }


    static boolean isUnary(Token token) {
        return token instanceof SymbolToken &&
                (((SymbolToken) token).symbol.equals(Symbol.PLUS) ||
                        ((SymbolToken) token).symbol.equals(Symbol.MINUS) ||
                        ((SymbolToken) token).symbol.equals(Symbol.TILDE) ||
                        ((SymbolToken) token).symbol.equals(Symbol.EXCLAMATION));
    }

    static boolean isTrue(Token token) {
        return (token instanceof BooleanLiteralToken && ((BooleanLiteralToken) token).value);
    }

    static boolean isBad(Token token) {
        return token instanceof BadToken;
    }

    static boolean isFor(Token token) {
        return (token instanceof KeywordToken && ((KeywordToken) token).keyword.equals(Keyword.FOR));
    }

    static boolean isIn(Token token) {
        return (token instanceof KeywordToken && ((KeywordToken) token).keyword.equals(Keyword.IN));
    }

    static boolean isWhile(Token token) {
        return (token instanceof KeywordToken && ((KeywordToken) token).keyword.equals(Keyword.WHILE));
    }

    static boolean isIsOperator(Token token) {
        return (token instanceof KeywordToken && ((KeywordToken) token).keyword.equals(Keyword.IS));
    }
}
