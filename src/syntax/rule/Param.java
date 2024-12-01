package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Symbol;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class Param implements Rule {
    public static SyntaxNode parse(Context context) {
        Node paramNode = new Node(SyntaxKind.PARAMETER_DEFINITION);

        if (Rule.isIdentifier(context.lookAhead())) {
            paramNode.addChild(new Node(SyntaxKind.IDENTIFIER, context.getToken()));
        } else {
            return null;
        }


        if (Rule.isColon(context.lookAhead())) {
            paramNode.addChild(new Node(Symbol.COLON, context.getToken()));
        } else {
            return null;
        }

        SyntaxNode typeName = TypeName.parse(context);
        if (typeName != null) {
            paramNode.addChild(typeName);
        } else {
            return null;
        }

        return paramNode;
    }

}
