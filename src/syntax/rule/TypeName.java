package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.IdentifierToken;
import syspro.tm.lexer.Symbol;
import syspro.tm.lexer.SymbolToken;
import syspro.tm.lexer.Token;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class TypeName implements Rule {

    public static SyntaxNode parse(Context context) {
        Node typeName;

        if (isQuestionMark(context.lookAhead())) {
            typeName = new Node(SyntaxKind.OPTION_NAME_EXPRESSION);
            typeName.addChild(new Node(Symbol.QUESTION, context.getToken()));
        } else {
            typeName = new Node(SyntaxKind.IDENTIFIER_NAME_EXPRESSION);
        }

        if (context.lookAhead() instanceof IdentifierToken) {
            Token identifier = context.getToken();
            typeName.addChild(new Node(SyntaxKind.IDENTIFIER, identifier));
        } else {
            return null;
        }

        if (isLess(context.lookAhead()) &&
                isValidGenericStart(context)) {
            return parseGeneric(context, typeName);
        }

        return typeName;
    }

    private static boolean isValidGenericStart(Context context) {
        int initialPosition = context.getPosition();
        context.getToken();

        int angleBracketCount = 1;
        while (angleBracketCount > 0) {
            Token next = context.lookAhead();
            if (next instanceof IdentifierToken) {
                context.getToken();
            } else if (next instanceof SymbolToken symbolToken) {
                if (symbolToken.symbol.equals(Symbol.LESS_THAN)) {
                    angleBracketCount++;
                    context.getToken();
                } else if (symbolToken.symbol.equals(Symbol.GREATER_THAN)) {
                    angleBracketCount--;
                    context.getToken();
                } else if (symbolToken.symbol.equals(Symbol.GREATER_THAN_GREATER_THAN)) {
                    angleBracketCount -= 2;
                } else if (symbolToken.symbol.equals(Symbol.COMMA)) {
                    context.getToken();
                } else {
                    context.setPosition(initialPosition);
                    return false;
                }
            } else {
                context.setPosition(initialPosition);
                return false;
            }
        }
        context.setPosition(initialPosition);
        return true;
    }

    private static Node parseGeneric(Context context, Node baseTypeName) {
        Node genericTypeName = new Node(SyntaxKind.GENERIC_NAME_EXPRESSION);
        if (baseTypeName.kind().equals(SyntaxKind.IDENTIFIER_NAME_EXPRESSION)) {
            SyntaxNode one = baseTypeName.slot(0);
            genericTypeName.addChild(one);
        }
        genericTypeName.addChild(new Node(Symbol.LESS_THAN, context.getToken()));

        Node separatedList = new Node(SyntaxKind.SEPARATED_LIST);

        SyntaxNode typeArgument = parse(context);
        if (typeArgument != null) {
            separatedList.addChild(typeArgument);

            while (isComma(context.lookAhead())) {
                separatedList.addChild(new Node(Symbol.COMMA, context.getToken()));
                typeArgument = parse(context);
                if (typeArgument != null) {
                    separatedList.addChild(typeArgument);
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }

        genericTypeName.addChild(separatedList);

        Token next = context.lookAhead();
        if (isGreater(next)) {
            genericTypeName.addChild(new Node(Symbol.GREATER_THAN, context.getToken()));
        } else if (isDoubleGreater(next)) {
            genericTypeName.addChild(new Node(Symbol.GREATER_THAN, splitDoubleGreater(context)));
            context.setPosition(context.getPosition() - 1);
        } else {
            return null;
        }

        return genericTypeName;
    }

    private static boolean isQuestionMark(Token token) {
        return token instanceof SymbolToken && ((SymbolToken) token).symbol.equals(Symbol.QUESTION);
    }

    private static boolean isLess(Token token) {
        return token instanceof SymbolToken && ((SymbolToken) token).symbol.equals(Symbol.LESS_THAN);
    }

    private static boolean isGreater(Token token) {
        return token instanceof SymbolToken && ((SymbolToken) token).symbol.equals(Symbol.GREATER_THAN);
    }

    private static boolean isComma(Token token) {
        return token instanceof SymbolToken && ((SymbolToken) token).symbol.equals(Symbol.COMMA);
    }

    private static boolean isDoubleGreater(Token token) {
        return token instanceof SymbolToken && ((SymbolToken) token).symbol.equals(Symbol.GREATER_THAN_GREATER_THAN);
    }

    private static Token splitDoubleGreater(Context context) {
        Token doubleGreater = context.getToken();
        SymbolToken firstGreater = new SymbolToken(
                doubleGreater.start,
                doubleGreater.start + 1,
                doubleGreater.leadingTriviaLength,
                0,
                Symbol.GREATER_THAN
        );
        SymbolToken secondGreater = new SymbolToken(
                doubleGreater.start + 1,
                doubleGreater.end,
                0,
                doubleGreater.trailingTriviaLength,
                Symbol.GREATER_THAN
        );

        context.insertTokenAtCurrent(secondGreater);
        context.setPosition(context.getPosition() + 1);
        return firstGreater;
    }
}
