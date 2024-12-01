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
            return null;
        }

        Token next = context.lookAhead(1);


        return typeParam;
    }

    private static boolean isIdentifier(Token token) {
        return token instanceof IdentifierToken;
    }

}
