package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.*;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.parser.TextSpan;

public class FunctionDef implements Rule {

    /**
     * Function Definition:
     *      1)  List of modifiers || null
     *      2)  'def'
     *      3)  this || identifier
     *      4)  (
     *      5)  List of params    || null
     *      6)  )
     *      7)  :
     *      8)  IdentifierNameExpression
     *      9)  INDENT
     *      10) List of statement
     *      11) DEDENT
     * */

    public static SyntaxNode parse(Context context) {

        Node functionDef = new Node(SyntaxKind.FUNCTION_DEFINITION);
        Node modifiers = null;


        while (Rule.isModifier(context.lookAhead())) {
            if (modifiers == null) {
                modifiers = new Node(SyntaxKind.LIST);
            }
            modifiers.addChild(new Node(((KeywordToken) context.lookAhead()).keyword, context.getToken()));
        }

        functionDef.addChild(modifiers);

        if (Rule.isDef(context.lookAhead())) {
            functionDef.addChild(new Node(Keyword.DEF, context.getToken()));
        } else if (Rule.isThis(context.lookAhead())) {
            functionDef.addChild(new Node(Keyword.THIS, context.getToken()));
        } else {
            return null;
        }


        if (Rule.isName(context.lookAhead())) {
            functionDef.addChild(new Node(SyntaxKind.IDENTIFIER, context.getToken()));
        } else {
            return null;
        }


        if (Rule.isOpenParan(context.lookAhead())) {
            functionDef.addChild(new Node(Symbol.OPEN_PAREN, context.getToken()));
            functionDef.addChild(parseParams(context));
            if (Rule.isCloseParen(context.lookAhead())) {
                functionDef.addChild(new Node(Symbol.CLOSE_PAREN, context.getToken()));
            } else {
                functionDef.addInvalidRange(
                        TextSpan.fromBounds(context.getPosition(), context.getPosition() + 1),
                        "Expected ')' "
                );
                functionDef.addChild(null);
            }
        }

        if (Rule.isColon(context.lookAhead())) {
            functionDef.addChild(new Node(Symbol.COLON, context.getToken()));
            functionDef.addChild(TypeName.parse(context));
        } else {
            functionDef.addChild(null);
            functionDef.addChild(null);
        }

        Node indent = null;
        Node dedent = null;

        if (Rule.isIndent(context.lookAhead())) {
            indent = new Node(SyntaxKind.INDENT, context.getToken());
        }


        Node stblc = (Node) StatementBlock.parse(context);
        stblc = indent != null ? stblc : null;

        if (Rule.isDedent(context.lookAhead()) && indent != null) {
            dedent = new Node(SyntaxKind.DEDENT, context.getToken());
        } else if (context.isEndOfFile() && indent != null && stblc != null ) {
            // после stmt_block только индентации,
            while (Rule.isIndent(context.lookAhead())){
                context.getToken();
            }
            dedent = new Node(SyntaxKind.DEDENT, context.getToken());
        } else if (indent != null && !Rule.isDedent(context.lookAhead())) {
            indent = null;
            stblc = null;
            while (!(Rule.isDedent(context.lookAhead()))) {
                context.getToken();
            }
        }

        functionDef.addChild(indent);
        functionDef.addChild(stblc);
        functionDef.addChild(dedent);

        return functionDef;
    }

    private static SyntaxNode parseParams(Context context) {
        Node paramList = new Node(SyntaxKind.SEPARATED_LIST);

        SyntaxNode param = Param.parse(context);
        if (param != null) {
            paramList.addChild(param);


            while (Rule.isComma(context.lookAhead())) {
                paramList.addChild(new Node(Symbol.COMMA, context.getToken()));
                SyntaxNode nextParam = Param.parse(context);
                if (nextParam != null) {
                    paramList.addChild(nextParam);
                } else {
                    paramList.addInvalidRange(
                            TextSpan.fromBounds(param.lastTerminal().position() , paramList.lastTerminal().position() + 1),
                            "Expected a expression after ','"
                    );
                }
            }
        }
        // null если никого
        return paramList.slotCount() > 0 ? paramList : null;
    }


}
