package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Symbol;
import syspro.tm.lexer.SymbolToken;
import syspro.tm.lexer.Token;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class Expression implements Rule {

    public static SyntaxNode parse(Context context) {
        return parseLogicalOr(context);
    }

    private static SyntaxNode parseLogicalOr(Context context) {
        SyntaxNode left = parseLogicalAnd(context);
        while (Rule.isLogicalOr(context.lookAhead())) {
            Token op = context.getToken();
            SyntaxNode right = parseLogicalAnd(context);
            left = combine(SyntaxKind.LOGICAL_OR_EXPRESSION, left, op, right);
        }
        return left;
    }

    private static SyntaxNode parseLogicalAnd(Context context) {
        SyntaxNode left = parseEquality(context);
        while (Rule.isLogicalAnd(context.lookAhead())) {
            SyntaxNode right = parseEquality(context);
            Token op = context.getToken();
            left = combine(SyntaxKind.LOGICAL_AND_EXPRESSION, left, op, right);
        }
        return left;
    }

    private static SyntaxNode parseEquality(Context context) {
        SyntaxNode left = parseRelational(context);
        while (Rule.isEqualityOperator(context.lookAhead())) {
            Token op = context.getToken();
            SyntaxNode right = parseRelational(context);
            if (op instanceof SymbolToken && (((SymbolToken) op).symbol.equals(Symbol.EQUALS_EQUALS))) {
                left = combine(SyntaxKind.EQUALS_EXPRESSION, left, op, right);
            } else {
                left = combine(SyntaxKind.NOT_EQUALS_EXPRESSION, left, op, right);
            }
        }
        return left;
    }

    private static SyntaxNode parseRelational(Context context) {
        int initialPosition = context.getPosition();
        // тут может быть параметризованный тип
        // еще через один посмотреть и решить
        Token token = context.lookAhead();
        Token token1 = context.lookAhead(1);

        if (Rule.isTypeName(context) && Rule.isLessThan(token1)) {
            SyntaxNode prim = Primary.parse(context);
            if (prim != null) {
                return prim;    // TODO: сделать поумнее
            }
            context.setPosition(initialPosition);
        }

        SyntaxNode left = parseBitwiseShift(context);
        while (Rule.isRelationalOperator(context.lookAhead())) {
            Token operator = context.getToken();
            SyntaxNode right = parseBitwiseShift(context);
            SyntaxKind kind = switch (operator) {
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.LESS_THAN)) ->
                        SyntaxKind.LESS_THAN_EXPRESSION;
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.GREATER_THAN)) ->
                        SyntaxKind.GREATER_THAN_EXPRESSION;
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.LESS_THAN_EQUALS)) ->
                        SyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION;
                case null, default -> SyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION;
            };

            left = combine(kind, left, operator, right);
        }
        return left;
    }

    private static SyntaxNode parseBitwiseShift(Context context) {
        SyntaxNode left = parseAdditive(context);
        while (Rule.isBitwiseShiftOperator(context.lookAhead())) {
            Token operator = context.getToken();
            SyntaxNode right = parseAdditive(context);
            if (operator instanceof SymbolToken && ((SymbolToken) operator).symbol.equals(Symbol.LESS_THAN_LESS_THAN)) {
                left = combine(SyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION, left, operator, right);
            } else {
                left = combine(SyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION, left, operator, right);
            }       }
        return left;
    }

    private static SyntaxNode parseAdditive(Context context) {
        SyntaxNode left = parseMultiplicative(context);
        while (Rule.isAdditive(context.lookAhead())) {
            Token operator = context.getToken();
            SyntaxNode right = parseMultiplicative(context);
            if (operator instanceof SymbolToken && ((SymbolToken) operator).symbol.equals(Symbol.PLUS)){
                left = combine(SyntaxKind.ADD_EXPRESSION, left, operator, right);
            } else {
                left = combine(SyntaxKind.SUBTRACT_EXPRESSION, left, operator, right);
            }
        }
        return left;
    }

    private static SyntaxNode parseMultiplicative(Context context) {
        SyntaxNode left = parseUnary(context);
        while (Rule.isMultiplicative(context.lookAhead())) {
            Token operator = context.getToken();
            SyntaxNode right = parseUnary(context);
            SyntaxKind kind = switch (operator) {
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.ASTERISK)) ->
                        SyntaxKind.MULTIPLY_EXPRESSION;
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.SLASH)) ->
                        SyntaxKind.DIVIDE_EXPRESSION;
                case null, default ->
                        SyntaxKind.MODULO_EXPRESSION;
            };
            left = combine(kind, left, operator, right);
        }
        return left;
    }

    private static SyntaxNode parseUnary(Context context) {
        Token next = context.lookAhead();
        if (Rule.isUnary(next)) {
            Token operator = context.getToken();
            SyntaxNode operand = Primary.parse(context);
            SyntaxKind kind = switch (operator) {
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.PLUS)) ->
                        SyntaxKind.UNARY_PLUS_EXPRESSION;
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.EXCLAMATION)) ->
                        SyntaxKind.LOGICAL_NOT_EXPRESSION;
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.TILDE)) ->
                        SyntaxKind.BITWISE_NOT_EXPRESSION;
                case null, default ->
                        SyntaxKind.UNARY_MINUS_EXPRESSION;
            };
            return combine(kind, operator, operand);
        }
        return parsePrimary(context);
    }

    private static SyntaxNode parsePrimary(Context context) {
        return Primary.parse(context);
    }

    private static Node combine(SyntaxKind kind, SyntaxNode left, Token operator, SyntaxNode right) {
        Node node = new Node(kind);
        node.addChild(left);
        node.addChild(new Node(((SymbolToken) operator).symbol, operator));
        node.addChild(right);
        return node;
    }

    private static Node combine(SyntaxKind kind, Token operator, SyntaxNode operand) {
        Node node = new Node(kind);
        node.addChild(new Node(((SymbolToken) operator).symbol, operator));
        node.addChild(operand);
        return node;
    }
}
