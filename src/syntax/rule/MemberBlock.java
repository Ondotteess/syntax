package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class MemberBlock implements Rule {

    public static SyntaxNode parse(Context context) {

        Node memberBlock = new Node(SyntaxKind.LIST);
        boolean isEmpty = true;

        while (true) {
            SyntaxNode memberDef = MemberDef.parse(context);
            if (memberDef == null) {
                break;
            }
            isEmpty = false;
            memberBlock.addChild(memberDef);
        }

        return isEmpty ? null: memberBlock;

    }

}