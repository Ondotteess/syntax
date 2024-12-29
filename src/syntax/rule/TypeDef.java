package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.*;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class TypeDef implements Rule {

    public static SyntaxNode parse(Context context) {

        SyntaxNode _keyword = null;         // 0
        SyntaxNode _ident = null;           // 1
        SyntaxNode lessThan = null;         // 2
        SyntaxNode typeParam = null;        // 3
        SyntaxNode greaterThan = null;      // 4
        SyntaxNode typeBound = null;        // 5
        SyntaxNode indent = null;           // 6
        SyntaxNode memberBlock = null;      // 7
        SyntaxNode dedent = null;           // 8

        Node typeDef = new Node(SyntaxKind.TYPE_DEFINITION);

        Token keyword = context.getToken();
        if (Rule.isContextual(keyword)) {
            _keyword = new Node(((IdentifierToken) keyword).contextualKeyword, toKeyword((IdentifierToken) keyword));
        } else {
            return null;
        }
        typeDef.addChild(_keyword);

        Token identifier = context.getToken();
        if (Rule.isIdentifier(identifier)) {
            _ident = new Node(SyntaxKind.IDENTIFIER, identifier);
        } else {
            return null;
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

        typeDef.addChild(memberBlock);

        if (Rule.isDedent(context.lookAhead()) && indent != null && memberBlock != null) {
            dedent = new Node(SyntaxKind.DEDENT, context.getToken());
        }

        typeDef.addChild(dedent);


        return typeDef;
    }

    private static KeywordToken toKeyword(IdentifierToken keyword) {
        return new KeywordToken(
                keyword.start,
                keyword.end,
                keyword.leadingTriviaLength,
                keyword.trailingTriviaLength,
                keyword.contextualKeyword);
    }
}
