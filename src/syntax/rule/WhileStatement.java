package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Keyword;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class WhileStatement implements Rule {

    public static SyntaxNode parse(Context context){
        Node while_stmnt = new Node(SyntaxKind.WHILE_STATEMENT);

        SyntaxNode while_keyword = new Node(Keyword.WHILE, context.getToken());
        SyntaxNode expression = Expression.parse(context);
        SyntaxNode indent = new Node(SyntaxKind.INDENT, context.getToken());
        SyntaxNode stmt_block = StatementBlock.parse(context);
        SyntaxNode dedent = new Node(SyntaxKind.DEDENT, context.getToken());

        // TODO: тут все вполне может поломаться

        while_stmnt.addChild(while_keyword);
        while_stmnt.addChild(expression);
        while_stmnt.addChild(indent);
        while_stmnt.addChild(stmt_block);
        while_stmnt.addChild(dedent);

        return while_stmnt;
    }

}
