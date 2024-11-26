package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Keyword;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class ContinueStatement implements Rule{

    public static SyntaxNode parse(Context context){
        Node _continue = new Node(SyntaxKind.CONTINUE_STATEMENT);
        _continue.addChild(new Node(Keyword.CONTINUE, context.getToken()));

        return _continue;
    }

}
