package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Keyword;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.parser.TextSpan;

public class WhileStatement implements Rule {

    public static SyntaxNode parse(Context context) {
        Node while_stmt = new Node(SyntaxKind.WHILE_STATEMENT);

        SyntaxNode while_keyword = Rule.isWhile(context.lookAhead())
                ? new Node(Keyword.WHILE, context.getToken())
                : null;

        SyntaxNode condition = Expression.parse(context);

        SyntaxNode indent = Rule.isIndent(context.lookAhead())
                ? new Node(SyntaxKind.INDENT, context.getToken())
                : null;

        SyntaxNode statement_block = StatementBlock.parse(context);

        SyntaxNode dedent = Rule.isDedent(context.lookAhead())
                ? new Node(SyntaxKind.DEDENT, context.getToken())
                : null;

        while_stmt.addChild(while_keyword);
        while_stmt.addChild(condition);
        while_stmt.addChild(indent);
        while_stmt.addChild(statement_block);
        while_stmt.addChild(dedent);


        if (condition == null) {
            while_stmt.addInvalidRange(
                    TextSpan.fromBounds(context.getPosition(), context.getPosition() + 1),
                    "expected condition after while"
            );
        }

        if (indent == null || statement_block == null || dedent == null) {
            while_stmt.addInvalidRange(
                    TextSpan.fromBounds(context.getPosition(), context.getPosition() + 1),
                    "expected stetement block"
            );
        }

        return while_stmt;
    }
}
