package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.Symbol;
import syspro.tm.lexer.SymbolToken;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class TypeParams implements Rule {

    public static SyntaxNode parse(Context context) {

        SyntaxNode typeParamList = null;

        Node typeParams = new Node(SyntaxKind.SEPARATED_LIST);

        typeParamList = parseTypeParamList(context);
        typeParams.addChild(typeParamList);


        return typeParams;
    }

    private static SyntaxNode parseTypeParamList(Context context) {
        Node typeParamList = new Node(SyntaxKind.TYPE_PARAMETER_DEFINITION);

        SyntaxNode typeParam = TypeParam.parse(context);
        if (typeParam != null) {
            typeParamList.addChild(typeParam);

            int paramCount = 1;

            while (Rule.isComma(context.lookAhead())) {
                typeParamList.addChild(new Node(Symbol.COMMA, context.getToken()));

                SyntaxNode nextTypeParam = TypeParam.parse(context);
                if (nextTypeParam != null) {
                    typeParamList.addChild(nextTypeParam);
                    paramCount++;
                } else {
                    context.invalidRange();
                }
            }

            if (paramCount == 1) {
                typeParamList.addChild(null);
            }
        }

        return typeParamList;
    }

}
