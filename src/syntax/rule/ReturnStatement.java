package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Keyword;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class ReturnStatement implements Rule{

    public static SyntaxNode parse(Context context){

        Node return_node = new Node(SyntaxKind.RETURN_STATEMENT);

        return_node.addChild(new Node(Keyword.RETURN, context.getToken()));
        SyntaxNode expression = Expression.parse(context);
        return_node.addChild(expression);
        return return_node;
    }

}
