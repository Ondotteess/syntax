package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Keyword;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class BreakStatement implements Rule{

    public static SyntaxNode parse(Context context){

        Node _break = new Node(SyntaxKind.BREAK_STATEMENT);
        _break.addChild(new Node(Keyword.BREAK, context.getToken()));

        return _break;
    }
}
