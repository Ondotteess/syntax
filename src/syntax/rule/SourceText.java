package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class SourceText implements Rule{

    public static SyntaxNode parse(Context context) {
        Node root = new Node(SyntaxKind.SOURCE_TEXT);

        Node listNode = new Node(SyntaxKind.LIST);

        while (context.lookAhead(0) != null) {
            SyntaxNode typeDefinition = TypeDef.parse(context);
            if (typeDefinition != null) listNode.addChild(typeDefinition);
        }

        root.addChild(listNode);

        return root;
    }

}
