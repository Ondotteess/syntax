package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.IdentifierToken;
import syspro.tm.lexer.Symbol;
import syspro.tm.lexer.SymbolToken;
import syspro.tm.lexer.Token;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class TypeParam implements Rule {

    public static SyntaxNode parse(Context context) {

        Token identifier = context.getToken();

        Node typeParam = null;
        if (isIdentifier(identifier)) {
            typeParam = new Node(SyntaxKind.IDENTIFIER, identifier);
        } else {
            context.invalidRange();
            return null;
        }

        Token next = context.lookAhead(1);

        //if (isTypeBound(next)) {
        //    SyntaxNode typeBound = TypeBound.parse(context);
        //    typeParam.addChild(typeBound);
        //}

        return typeParam;
    }

    private static boolean isIdentifier(Token token) {
        return token instanceof IdentifierToken;
    }

    private static boolean isTypeBound(Token token) {
        return token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.BOUND);
    }

    private static boolean isGreater(Token token) {
        return token instanceof SymbolToken &&
                ((SymbolToken) token).symbol.equals(Symbol.GREATER_THAN);
    }

}
