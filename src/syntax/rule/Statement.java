package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class Statement implements Rule {

    public static SyntaxNode parse(Context context) {

        return switch (context.lookAhead().toString()) {
            case "var", "val" -> VariableDefStmt.parse(context);
            case "return" -> ReturnStatement.parse(context);
            case "break" -> BreakStatement.parse(context);
            case "continue" -> ContinueStatement.parse(context);
            case "if" -> IfStatement.parse(context);
            case "while" -> WhileStatement.parse(context);
            case "for" -> ForStatement.parse(context);

            default -> parseExpressionOrAssignment(context);
        };
    }

    private static SyntaxNode parseExpressionOrAssignment(Context context) {
        int current_position = context.getPosition();
        SyntaxNode primary = Primary.parse(context);

        if (primary != null && Rule.isEquals(context.lookAhead())) {
            context.setPosition(current_position);
            return AssignmentStatement.parse(context);
        }
        context.setPosition(current_position);
        SyntaxNode expression = Expression.parse(context);
        if (expression != null) {
            Node expr_stmnt = new Node(SyntaxKind.EXPRESSION_STATEMENT);
            // унести в класс
            expr_stmnt.addChild(expression);
            return expr_stmnt;
        }

        context.invalidRange();
        return null;
    }


}
