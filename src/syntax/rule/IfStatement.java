package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Keyword;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class IfStatement implements Rule{

    /**
     *      1)      if
     *      2)      expression
     *      3)      indent
     *      4)      statement_block
     *      5)      dedent
     *      6)      else
     *      7)      indent
     *      8)      statement_block
     *      9)      dedent
     * */

    public static SyntaxNode parse(Context context){

        Node if_stmt = new Node(SyntaxKind.IF_STATEMENT);

        SyntaxNode if_keyword = Rule.isIf(context.lookAhead())
                ? new Node(Keyword.IF, context.getToken())
                : null;

        SyntaxNode if_expression = Expression.parse(context);

        SyntaxNode indent = Rule.isIndent(context.lookAhead())
                ? new Node(SyntaxKind.INDENT, context.getToken())
                : null;

        SyntaxNode if_statement_block = StatementBlock.parse(context);

        SyntaxNode dedent = Rule.isDedent(context.lookAhead())
                ? new Node(SyntaxKind.DEDENT, context.getToken())
                : null;

        SyntaxNode else_keyword = Rule.isElse(context.lookAhead())
                ? new Node(Keyword.ELSE, context.getToken())
                : null;

        if (else_keyword == null) {
            if (dedent == null) indent = null;
            if_stmt.addChild(if_keyword);
            if_stmt.addChild(if_expression);
            if_stmt.addChild(dedent == null ? null : indent);
            if_stmt.addChild(if_statement_block);
            if_stmt.addChild(dedent);
            if_stmt.addChild(null);
            if_stmt.addChild(null);
            if_stmt.addChild(null);
            if_stmt.addChild(null);
            return if_stmt;
        }

        SyntaxNode else_indent = Rule.isIndent(context.lookAhead())
                ? new Node(SyntaxKind.INDENT, context.getToken())
                : null;

        SyntaxNode else_statement_block = StatementBlock.parse(context);

        SyntaxNode else_dedent = Rule.isDedent(context.lookAhead())
                ? new Node(SyntaxKind.DEDENT, context.getToken())
                : null;


        if_stmt.addChild(if_keyword);
        if_stmt.addChild(if_expression);
        if_stmt.addChild(indent);
        if_stmt.addChild(if_statement_block);
        if_stmt.addChild(dedent);
        if_stmt.addChild(else_keyword);
        if_stmt.addChild(else_indent);
        if_stmt.addChild(else_statement_block);
        if_stmt.addChild(else_dedent);

        return if_stmt;

    }

}
