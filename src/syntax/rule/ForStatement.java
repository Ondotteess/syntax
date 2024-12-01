package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Keyword;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class ForStatement implements Rule {


    public static SyntaxNode parse(Context context) {

        Node for_stmt = new Node(SyntaxKind.FOR_STATEMENT);

        SyntaxNode for_keyword = Rule.isFor(context.lookAhead())
                ? new Node(Keyword.FOR, context.getToken())
                : null;

        SyntaxNode for_primary = Primary.parse(context);

        SyntaxNode in_keyword = Rule.isIn(context.lookAhead())
                ? new Node(Keyword.IN, context.getToken())
                : null;

        SyntaxNode for_expression = Expression.parse(context);

        SyntaxNode indent = Rule.isIndent(context.lookAhead())
                ? new Node(SyntaxKind.INDENT, context.getToken())
                : null;

        SyntaxNode statement_block = StatementBlock.parse(context);

        SyntaxNode dedent = Rule.isDedent(context.lookAhead())
                ? new Node(SyntaxKind.DEDENT, context.getToken())
                : null;

        for_stmt.addChild(for_keyword);
        for_stmt.addChild(for_primary);
        for_stmt.addChild(in_keyword);
        for_stmt.addChild(for_expression);
        for_stmt.addChild(indent);
        for_stmt.addChild(statement_block);
        for_stmt.addChild(dedent);

        return for_stmt;
    }
}
