package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Symbol;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class TypeBound implements Rule {

    public static SyntaxNode parse(Context context) {
        Node typeBound = new Node(SyntaxKind.TYPE_BOUND);
        Node list = new Node(SyntaxKind.SEPARATED_LIST);

        typeBound.addChild(new Node(Symbol.BOUND, context.getToken()));

        SyntaxNode firstType = TypeName.parse(context);
        if (firstType == null) {
            return null;
        }
        list.addChild(firstType);
        while (Rule.isAmpersand(context.lookAhead())) {
            list.addChild(new Node(Symbol.AMPERSAND, context.getToken()));
            SyntaxNode nextType = TypeName.parse(context);
            if (nextType == null) {
                return null;
            }
            list.addChild(nextType);
        }
        typeBound.addChild(list);
        return typeBound;
    }
}
