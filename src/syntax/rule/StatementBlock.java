package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class StatementBlock implements Rule {

    public static SyntaxNode parse(Context context) {
        Node statementBlock = new Node(SyntaxKind.LIST);
        boolean return_null = true;
        while (true) {
            SyntaxNode statement = Statement.parse(context);
            if (statement != null) {
                statementBlock.addChild(statement);
                return_null = false;
            } else {
                break;
            }
        }

        return !return_null ? statementBlock : null;
    }



}
