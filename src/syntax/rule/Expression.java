package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.*;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.parser.TextSpan;

import java.util.List;

public class Expression implements Rule {

    public static SyntaxNode parse(Context context) {
        return parseLogicalOr(context);
    }

    private static SyntaxNode parseLogicalOr(Context context) {
        SyntaxNode left = parseLogicalAnd(context);
        while (Rule.isLogicalOr(context.lookAhead())) {
            Token op = context.getToken();
            SyntaxNode right = parseLogicalAnd(context);
            if (right == null) {
                addErrorAndRecover(context, (Node) left, op, "Expected expression after '||'", List.of(Symbol.CLOSE_PAREN, Symbol.COMMA));
                return left;
            }
            left = combine(SyntaxKind.LOGICAL_OR_EXPRESSION, left, op, right);
        }
        return left;
    }

    private static SyntaxNode parseLogicalAnd(Context context) {
        SyntaxNode left = parseBitwiseOr(context);
        while (Rule.isLogicalAnd(context.lookAhead())) {
            Token op = context.getToken();
            SyntaxNode right = parseBitwiseOr(context);
            if (right == null) {
                addErrorAndRecover(context, (Node) left, op, "Expected expression after '&&'", List.of(Symbol.CLOSE_PAREN, Symbol.COMMA));
                return left;
            }
            left = combine(SyntaxKind.LOGICAL_AND_EXPRESSION, left, op, right);
        }
        return left;
    }

    private static SyntaxNode parseBitwiseOr(Context context) {
        SyntaxNode left = parseBitwiseXor(context);
        while (Rule.isBitwiseOr(context.lookAhead())) {
            Token op = context.getToken();
            SyntaxNode right = parseBitwiseXor(context);
            if (right == null) {
                addErrorAndRecover(context, (Node) left, op, "Expected expression after '|'", List.of(Symbol.CLOSE_PAREN, Symbol.COMMA));
                return left;
            }
            left = combine(SyntaxKind.BITWISE_OR_EXPRESSION, left, op, right);
        }
        return left;
    }

    private static SyntaxNode parseBitwiseXor(Context context) {
        SyntaxNode left = parseBitwiseAnd(context);
        while (Rule.isBitwiseXor(context.lookAhead())) {
            Token op = context.getToken();
            SyntaxNode right = parseBitwiseAnd(context);
            if (right == null) {
                addErrorAndRecover(context, (Node) left, op, "Expected expression after '^'", List.of(Symbol.CLOSE_PAREN, Symbol.COMMA));
                return left;
            }
            left = combine(SyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION, left, op, right);
        }
        return left;
    }

    private static SyntaxNode parseBitwiseAnd(Context context) {
        SyntaxNode left = parseEquality(context);
        while (Rule.isBitwiseAnd(context.lookAhead())) {
            Token op = context.getToken();
            SyntaxNode right = parseEquality(context);
            if (right == null) {
                addErrorAndRecover(context, (Node) left, op, "Expected expression after '&'", List.of(Symbol.CLOSE_PAREN, Symbol.COMMA));
                return left;
            }
            left = combine(SyntaxKind.BITWISE_AND_EXPRESSION, left, op, right);
        }
        return left;
    }

    private static SyntaxNode parseEquality(Context context) {
        SyntaxNode left = parseRelational(context);
        while (true) {
            Token op = context.lookAhead();
            if (Rule.isEqualityOperator(op)) {
                context.getToken();
                SyntaxNode right = parseRelational(context);
                if (right == null) {
                    addErrorAndRecover(context, (Node) left, op, "Expected expression after equality operator", List.of(Symbol.CLOSE_PAREN, Symbol.COMMA));
                    return left;
                }
                if (op instanceof SymbolToken && (((SymbolToken) op).symbol.equals(Symbol.EQUALS_EQUALS))) {
                    left = combine(SyntaxKind.EQUALS_EXPRESSION, left, op, right);
                } else {
                    left = combine(SyntaxKind.NOT_EQUALS_EXPRESSION, left, op, right);
                }
            } else {
                break;
            }
        }
        return left;
    }

    private static SyntaxNode parseRelational(Context context) {
        int initialPosition = context.getPosition();

        if (isPotentialGeneric(context)) {
            SyntaxNode prim = Primary.parse(context);
            if (prim != null) {
                return prim;
            }
            context.setPosition(initialPosition);
        }

        Node left = (Node) parseBitwiseShift(context);
        while (Rule.isRelationalOperator(context.lookAhead())) {
            Token operator = context.getToken();
            SyntaxNode right = parseBitwiseShift(context);

            if (right == null) {
                addErrorAndRecover(context, left, operator, "Expected expression after relational operator", List.of(Symbol.CLOSE_PAREN, Symbol.COMMA));
                return left;
            }

            SyntaxKind kind = switch (operator) {
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.LESS_THAN)) ->
                        SyntaxKind.LESS_THAN_EXPRESSION;
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.GREATER_THAN)) ->
                        SyntaxKind.GREATER_THAN_EXPRESSION;
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.LESS_THAN_EQUALS)) ->
                        SyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION;
                case SymbolToken symbolToken when (symbolToken.symbol.equals(Symbol.GREATER_THAN_EQUALS)) ->
                        SyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION;
                default -> null;
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
            if (right == null) {
                addErrorAndRecover(context, (Node) left, operator, "Expected expression after bitwise shift operator", List.of(Symbol.CLOSE_PAREN, Symbol.COMMA));
                return left;
            }
            if (operator instanceof SymbolToken && ((SymbolToken) operator).symbol.equals(Symbol.LESS_THAN_LESS_THAN)) {
                left = combine(SyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION, left, operator, right);
            } else {
                left = combine(SyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION, left, operator, right);
            }
        }
        return left;
    }

    private static SyntaxNode parseAdditive(Context context) {
        SyntaxNode left = parseMultiplicative(context);
        while (Rule.isAdditive(context.lookAhead())) {
            Token operator = context.getToken();
            SyntaxNode right = parseMultiplicative(context);
            if (right == null) {
                addErrorAndRecover(context, (Node) left, operator, "Expected expression after additive operator", List.of(Symbol.CLOSE_PAREN, Symbol.COMMA));
                return left;
            }
            if (operator instanceof SymbolToken && ((SymbolToken) operator).symbol.equals(Symbol.PLUS)) {
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
            if (right == null) {
                addErrorAndRecover(context, (Node) left, operator, "Expected expression after multiplicative operator", List.of(Symbol.CLOSE_PAREN, Symbol.COMMA));
                return left;
            }
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
            if (operand == null) {
                Node node = new Node(SyntaxKind.UNARY_MINUS_EXPRESSION);
                node.addInvalidRange(
                        TextSpan.fromBounds(operator.start, operator.end),
                        "Expected operand after unary operator"
                );
                return node;
            }
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

    private static void addErrorAndRecover(Context context, Node left, Token operator, String errorMessage, List<Symbol> stopSymbols) {
        left.addInvalidRange(TextSpan.fromBounds(operator.start, operator.end), errorMessage);
        recover(context, stopSymbols);
    }

    private static void recover(Context context, List<Symbol> stopSymbols) {
        int safePosition = context.getPosition();
        while (context.lookAhead() != null) {
            Token next = context.lookAhead();

            if (next instanceof SymbolToken symbolToken && stopSymbols.contains(symbolToken.symbol)) {
                break;
            }

            context.getToken();
            safePosition = context.getPosition();
        }

        context.setPosition(safePosition);
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

    private static boolean isPotentialGeneric(Context context) {
        int initialPosition = context.getPosition();

        if (!Rule.isTypeName(context)) {
            context.setPosition(initialPosition);
            return false;
        }

        if (!(context.lookAhead() instanceof SymbolToken token && token.symbol.equals(Symbol.LESS_THAN))) {
            context.setPosition(initialPosition);
            return false;
        }

        context.getToken();

        int angleBracketCount = 1;
        while (angleBracketCount > 0) {
            Token next = context.lookAhead();
            if (next instanceof IdentifierToken) {
                context.getToken();
            } else if (next instanceof SymbolToken symbolToken) {
                if (symbolToken.symbol.equals(Symbol.LESS_THAN)) {
                    angleBracketCount++;
                    context.getToken();
                } else if (symbolToken.symbol.equals(Symbol.GREATER_THAN)) {
                    angleBracketCount--;
                    context.getToken();
                } else if (symbolToken.symbol.equals(Symbol.COMMA)) {
                    context.getToken();
                } else {
                    context.setPosition(initialPosition);
                    return false;
                }
            } else {
                context.setPosition(initialPosition);
                return false;
            }
        }

        return true;
    }
}
