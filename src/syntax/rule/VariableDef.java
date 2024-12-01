package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.lexer.*;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class VariableDef implements Rule {

    /**
     *      1)  var||val
     *      2)  IDENTIFIER
     *      3)  COLON
     *      4)  TYPE_NAME
     *      5)  EQUALS
     *      6)  EXPRESSION
     * */


    public static SyntaxNode parse(Context context) {

        Node variableDef = new Node(SyntaxKind.VARIABLE_DEFINITION);


        if (Rule.isVarOrVal(context.lookAhead())) {
            variableDef.addChild(new Node(
                    ((KeywordToken) context.lookAhead()).keyword,
                    context.getToken())
            );

        } else {
            return null;
        }


        if (context.lookAhead() instanceof IdentifierToken) {
            variableDef.addChild(new Node(SyntaxKind.IDENTIFIER, context.getToken()));
        } else {
            return null;
        }


        if (Rule.isColon(context.lookAhead())) {
            variableDef.addChild(new Node(Symbol.COLON, context.getToken()));
            variableDef.addChild(TypeName.parse(context));
        }
        else {
            variableDef.addChild(null);
            variableDef.addChild(null);

        }

        if (Rule.isEquals(context.lookAhead())) {
            variableDef.addChild(new Node(Symbol.EQUALS, context.getToken()));
            variableDef.addChild(Expression.parse(context));
        } else {
            variableDef.addChild(null);
            variableDef.addChild(null);

        }

        return variableDef;
    }



}

