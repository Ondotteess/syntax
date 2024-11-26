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

        Token a;        // TODO: как нибудь поумнее
        if (context.lookAhead() instanceof IdentifierToken) {
            a = context.lookAhead();
            typeName.addChild(new Node(SyntaxKind.IDENTIFIER, context.getToken()));
        } else {
            context.invalidRange();
            return null;
        }

        if (isLess(context.lookAhead())) {
            Node genericTypeName = new Node(SyntaxKind.GENERIC_NAME_EXPRESSION);
            genericTypeName.addChild(new Node(SyntaxKind.IDENTIFIER, a));

            genericTypeName.addChild(new Node(Symbol.LESS_THAN, context.getToken()));
            Node inner = new Node(SyntaxKind.SEPARATED_LIST);
            if (Rule.isIdentifier(context.lookAhead())) {
                inner.addChild(TypeName.parse(context));

                while (isComma(context.lookAhead())) {
                    inner.addChild(new Node(Symbol.COMMA, context.getToken()));

                    SyntaxNode nextTypeParam = TypeName.parse(context);
                    if (nextTypeParam != null) {
                        inner.addChild(nextTypeParam);
                    } else {
                        context.invalidRange();
                        return null;
                    }
                }
                genericTypeName.addChild(inner);
            } else {
                context.invalidRange();
                return null;
            }
            if (isGreater(context.lookAhead())) {
                genericTypeName.addChild(new Node(Symbol.GREATER_THAN, context.getToken()));
            } else {
                context.invalidRange();
                return null;
            }
            return genericTypeName;
        }
        return typeName;
    }

    private static boolean isQuestionMark(Token token){
        return (token instanceof SymbolToken && ((SymbolToken) token).symbol.equals(Symbol.QUESTION));
    }

    private static boolean isComma(Token token) {
        return (token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.COMMA));
    }

    private static boolean isLess(Token token) {
        return (token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.LESS_THAN));
    }

    private static boolean isGreater(Token token) {
        return (token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.GREATER_THAN));
    }


}
