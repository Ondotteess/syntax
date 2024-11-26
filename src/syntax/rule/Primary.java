package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Keyword;
import syspro.tm.lexer.Symbol;
import syspro.tm.lexer.Token;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

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
            return new Node(SyntaxKind.BAD, context.getToken());
        } else if (Rule.isOpenParan(token)) {
            return parseParensExpression(context);
        } else {
            return null;    //TODO: наверное invalid range надо оформить
        }


    }

    private static Node parseParensExpression(Context context) {
        Token openParen = context.getToken();
        SyntaxNode expression = Expression.parse(context);
        Token closeParen = null;
        if (Rule.isCloseParen(context.lookAhead())) {
            closeParen = context.getToken();
        }else {
            context.invalidRange();
        }

        Node parens = new Node(SyntaxKind.PARENTHESIZED_EXPRESSION);
        parens.addChild(new Node(Symbol.OPEN_PAREN, openParen));
        parens.addChild(expression);
        parens.addChild(new Node(Symbol.CLOSE_PAREN, closeParen));
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
                    context.invalidRange();
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
                    } else {
                        invocation.addChild(null);
                    }

                    invocation.addChild(
                            parseArguments(context));

                    if (Rule.isCloseParen(context.lookAhead())){
                        invocation.addChild(new Node(Symbol.CLOSE_PAREN, context.getToken()));
                    } else {
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
                } else {
                    context.invalidRange();
                }
                Node indexAccess = new Node(SyntaxKind.INDEX_EXPRESSION);
                indexAccess.addChild(base);
                indexAccess.addChild(new Node(Symbol.OPEN_BRACKET, opBrack));
                indexAccess.addChild(indexExpression);
                indexAccess.addChild(new Node(Symbol.CLOSE_PAREN, closeBrack));
                base = indexAccess;

            } else if (Rule.isOpenParan(next)) {
                // это invocation без member access
                // TODO: копипасту убрать
                Node invocation = new Node(SyntaxKind.INVOCATION_EXPRESSION);
                invocation.addChild(base);

                if (Rule.isOpenParan(context.lookAhead())){
                    invocation.addChild(new Node(Symbol.OPEN_PAREN, context.getToken()));
                } else {
                    invocation.addChild(null);
                }

                invocation.addChild(parseArguments(context));

                if (Rule.isCloseParen(context.lookAhead())){
                    invocation.addChild(new Node(Symbol.CLOSE_PAREN, context.getToken()));
                } else {
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
                    context.invalidRange();


                }
            }
        }


        return arguments.slotCount() > 0 ? arguments : null;
    }
}

