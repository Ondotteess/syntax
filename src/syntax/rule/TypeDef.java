package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.*;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class TypeDef implements Rule {

    public static SyntaxNode parse(Context context) {

        SyntaxNode _keyword = null;
        SyntaxNode _ident = null;
        SyntaxNode lessThan = null;
        SyntaxNode typeParam = null;
        SyntaxNode greaterThan = null;
        SyntaxNode typeBound = null;
        SyntaxNode indent = null;
        SyntaxNode memberBlock = null;
        SyntaxNode dedent = null;

        Node typeDef = new Node(SyntaxKind.TYPE_DEFINITION);

        Token keyword = context.getToken();
        if (Rule.isContextual(keyword)) {
            _keyword = new Node(((IdentifierToken) keyword).contextualKeyword, toKeyword((IdentifierToken) keyword));
        } else {
            context.invalidRange();
            return null;
        }
        typeDef.addChild(_keyword);

        Token identifier = context.getToken();
        if (Rule.isIdentifier(identifier)) {
            _ident = new Node(SyntaxKind.IDENTIFIER, identifier);
        } else {
            context.invalidRange();
        }
        typeDef.addChild(_ident);

        Token next = context.lookAhead();
        if (Rule.isLessThan(next)) {
            lessThan = new Node(Symbol.LESS_THAN, context.getToken());
            typeParam = TypeParams.parse(context);

            next = context.lookAhead();
            if (next instanceof SymbolToken && ((SymbolToken) next).symbol.equals(Symbol.GREATER_THAN)) {
                greaterThan = new Node(Symbol.GREATER_THAN, context.getToken());
            }
        }
        typeDef.addChild(lessThan);
        typeDef.addChild(typeParam);
        typeDef.addChild(greaterThan);

        next = context.lookAhead();
        if (Rule.isBound(next)) {
            typeBound = TypeBound.parse(context);
        }
        typeDef.addChild(typeBound);

        if (Rule.isIndent(context.lookAhead())) {
            indent = new Node(SyntaxKind.INDENT, context.getToken());
        }
        typeDef.addChild(indent);

        memberBlock = MemberBlock.parse(context);

        memberBlock = indent != null ? memberBlock : null;
        //TODO: задиагностировать

        typeDef.addChild(memberBlock);

        if (Rule.isDedent(context.lookAhead())) {
            dedent = new Node(SyntaxKind.DEDENT, context.getToken());
        }

        typeDef.addChild(dedent);


        return typeDef;
    }

    private static KeywordToken toKeyword(IdentifierToken keyword) {
        return new KeywordToken(        // TODO: куда нибудь унести
                keyword.start,
                keyword.end,
                keyword.leadingTriviaLength,
                keyword.trailingTriviaLength,
                keyword.contextualKeyword);
    }
}
