package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Keyword;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class ForStatement implements Rule{

    public static SyntaxNode parse(Context context){

        Node for_stmnt = new Node(SyntaxKind.FOR_STATEMENT);

        SyntaxNode for_key = new Node(Keyword.FOR, context.getToken());
        SyntaxNode for_primary = Primary.parse(context);
        SyntaxNode in = new Node(Keyword.IN, context.getToken());
        SyntaxNode for_expr = Expression.parse(context);
        SyntaxNode indent = new Node(SyntaxKind.INDENT, context.getToken());
        SyntaxNode statement_block = StatementBlock.parse(context);
        SyntaxNode dedent = new Node(SyntaxKind.DEDENT, context.getToken());

        // TODO: тут null может быть везде

        for_stmnt.addChild(for_key);
        for_stmnt.addChild(for_primary);
        for_stmnt.addChild(in);
        for_stmnt.addChild(for_expr);
        for_stmnt.addChild(indent);
        for_stmnt.addChild(statement_block);
        for_stmnt.addChild(dedent);

        return for_stmnt;

    }

}
