package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Keyword;
import syspro.tm.lexer.Symbol;
import syspro.tm.lexer.Token;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.parser.TextSpan;

public class Primary implements Rule {

    public static SyntaxNode parse(Context context) {
        Node base = parseAtom(context);
        if (base == null) {
            return null;
        }

        return parseExtensions(context, base);
    }

    private static Node parseAtom(Context context) {
        Token token = context.lookAhead();
        if (token == null) {
            return null;
        }

        if (Rule.isTypeName(context)){
            return (Node) TypeName.parse(context);
        } else if (Rule.isThis(token)) {
            Node bn = new Node(SyntaxKind.THIS_EXPRESSION);
            bn.addChild(new Node(Keyword.THIS, context.getToken()));
            return bn;
        } else if (Rule.isSuper(token)) {
            Node bn = new Node(SyntaxKind.SUPER_EXPRESSION);
            bn.addChild(new Node(Keyword.SUPER, context.getToken()));
            return bn;
        } else if (Rule.isIdentifier(token)) {
            Node bn = new Node(SyntaxKind.IDENTIFIER_NAME_EXPRESSION);
            bn.addChild(new Node(SyntaxKind.IDENTIFIER, context.getToken()));
            return bn;
        } else if (Rule.isIntegerLiteral(token)) {
            Node bn = new Node(SyntaxKind.INTEGER_LITERAL_EXPRESSION);
            bn.addChild(new Node(SyntaxKind.INTEGER, context.getToken()));
            return bn;
        } else if (Rule.isRuneLiteral(token)) {
            Node bn = new Node(SyntaxKind.RUNE_LITERAL_EXPRESSION);
            bn.addChild(new Node(SyntaxKind.RUNE, context.getToken()));
            return bn;
        } else if (Rule.isBooleanLiteral(token)) {
            if (Rule.isTrue(context.lookAhead())) {
                Node bn = new Node(SyntaxKind.TRUE_LITERAL_EXPRESSION);
                bn.addChild(new Node(SyntaxKind.BOOLEAN, context.getToken()));
                return bn;
            } else {
                Node bn = new Node(SyntaxKind.FALSE_LITERAL_EXPRESSION);
                bn.addChild(new Node(SyntaxKind.BOOLEAN, context.getToken()));
                return bn;
            }
        } else if (Rule.isStringLiteral(token)) {
            Node bn = new Node(SyntaxKind.STRING_LITERAL_EXPRESSION);
            bn.addChild(new Node(SyntaxKind.STRING, context.getToken()));
            return bn;
        } else if (Rule.isBad(token)) {
            int start = context.lookAhead().start + context.lookAhead().leadingTriviaLength;
            int end = context.lookAhead().end - context.lookAhead().trailingTriviaLength + 1;
            Node bn = new Node(SyntaxKind.BAD, context.getToken());
            bn.addInvalidRange(
                    TextSpan.fromBounds(start, end),
                    "Invalid token"
            );
            return bn;
        } else if (Rule.isOpenParan(token)) {
            return parseParensExpression(context);
        } else {
            return null;
        }


    }

    private static Node parseParensExpression(Context context) {
        Token openParen = context.getToken();
        SyntaxNode expression = Expression.parse(context);
        Token closeParen = null;

        if (Rule.isCloseParen(context.lookAhead())) {
            closeParen = context.getToken();
        }

        Node parens = new Node(SyntaxKind.PARENTHESIZED_EXPRESSION);
        parens.addChild(new Node(Symbol.OPEN_PAREN, openParen));
        parens.addChild(expression);
        if (closeParen != null) {
            parens.addChild(new Node(Symbol.CLOSE_PAREN, closeParen));
        } else {
            parens.addChild(null);
            parens.addInvalidRange(
                    TextSpan.fromBounds(expression.lastTerminal().position(), expression.lastTerminal().position() + 1),
                    "expected ') "
            );
        }
        return parens;
    }


    private static SyntaxNode parseExtensions(Context context, Node base) {
        while (true) {
            Token next = context.lookAhead();
            if (next == null) {
                break;
            }

            if (Rule.isDot(next)) {
                Token dot = context.getToken();

                Token identifier = null;

                if (Rule.isIdentifier(context.lookAhead())) {
                    identifier = context.getToken();
                } else {
                    return null;
                }

                if (Rule.isOpenParan(context.lookAhead())) {
                    Node invocation = new Node(SyntaxKind.INVOCATION_EXPRESSION);
                    Node mem_access = new Node(SyntaxKind.MEMBER_ACCESS_EXPRESSION);
                    mem_access.addChild(base);
                    mem_access.addChild(new Node(Symbol.DOT, dot));
                    mem_access.addChild(new Node(SyntaxKind.IDENTIFIER, identifier));

                    invocation.addChild(mem_access);
                    if (Rule.isOpenParan(context.lookAhead())){
                        invocation.addChild(new Node(Symbol.OPEN_PAREN, context.getToken()));
                    }

                    invocation.addChild(parseArguments(context));

                    if (Rule.isCloseParen(context.lookAhead())){
                        invocation.addChild(new Node(Symbol.CLOSE_PAREN, context.getToken()));
                    } else {
                        invocation.addInvalidRange(
                                TextSpan.fromBounds(invocation.lastTerminal().position(), invocation.lastTerminal().position() + 1),
                                "expected ') "
                        );
                        invocation.addChild(null);
                    }
                    base = invocation;
                } else {
                    Node memberAccess = new Node(SyntaxKind.MEMBER_ACCESS_EXPRESSION);
                    memberAccess.addChild(base);
                    memberAccess.addChild(new Node(Symbol.DOT, dot));
                    memberAccess.addChild(new Node(SyntaxKind.IDENTIFIER, identifier));
                    base = memberAccess;
                }

            } else if (Rule.isOpenBracket(next)) {
                Token opBrack = context.getToken();
                Token closeBrack = null;
                SyntaxNode indexExpression = Expression.parse(context);
                if (Rule.isCloseBracket(context.lookAhead())) {
                    closeBrack = context.getToken();
                }

                Node indexAccess = new Node(SyntaxKind.INDEX_EXPRESSION);
                indexAccess.addChild(base);
                indexAccess.addChild(new Node(Symbol.OPEN_BRACKET, opBrack));
                indexAccess.addChild(indexExpression);
                indexAccess.addChild(new Node(Symbol.CLOSE_PAREN, closeBrack));

                if (closeBrack == null) {
                    indexAccess.addInvalidRange(
                            TextSpan.fromBounds(indexAccess.lastTerminal().position(), indexAccess.lastTerminal().position() + 1),
                            "expected '] "
                    );
                }
                base = indexAccess;

            } else if (Rule.isOpenParan(next)) {
                // это invocation без member access
                Node invocation = new Node(SyntaxKind.INVOCATION_EXPRESSION);
                invocation.addChild(base);

                invocation.addChild(new Node(Symbol.OPEN_PAREN, context.getToken()));

                invocation.addChild(parseArguments(context));

                if (Rule.isCloseParen(context.lookAhead())){
                    invocation.addChild(new Node(Symbol.CLOSE_PAREN, context.getToken()));
                } else {
                    invocation.addInvalidRange(
                            TextSpan.fromBounds(invocation.lastTerminal().position(), invocation.lastTerminal().position() + 1),
                            "expected ') "
                    );
                    invocation.addChild(null);
                }
                base = invocation;
            } else {
                return base;
            }
        }

        return base;
    }

    private static SyntaxNode parseArguments(Context context) {
        Node arguments = new Node(SyntaxKind.SEPARATED_LIST);

        SyntaxNode argument = Expression.parse(context);
        if (argument != null) {
            arguments.addChild(argument);


            while (Rule.isComma(context.lookAhead())) {
                Token comma = context.getToken();
                SyntaxNode nextArgument = Expression.parse(context);
                if (nextArgument != null) {
                    arguments.addChild(new Node(Symbol.COMMA, comma));
                    arguments.addChild(nextArgument);
                } else {
                    arguments.addInvalidRange(
                            TextSpan.fromBounds(context.getPosition(), context.getPosition() + 1),
                            "Expected a expression after ','"
                    );

                }
            }
        }


        return arguments.slotCount() > 0 ? arguments : null;
    }
}

