package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Token;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class AssignmentStatement {

    public static SyntaxNode parse(Context context){
        Node assignment = new Node(SyntaxKind.ASSIGNMENT_STATEMENT);
        Node left = (Node) Primary.parse(context);
        Token equals = context.getToken();
        SyntaxNode right = Expression.parse(context);
        if (right == null) {
            context.invalidRange();
        }
        assignment.addChild(left);
        assignment.addChild(new Node(SyntaxKind.EQUALS_EXPRESSION, equals));
        assignment.addChild(right);
        return assignment;
    }

}
