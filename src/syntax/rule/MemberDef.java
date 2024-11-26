package syntax.rule;

import syntax.Context;
import syspro.tm.parser.SyntaxNode;

public class MemberDef implements Rule {

    public static SyntaxNode parse(Context context){
        SyntaxNode variableDef = VariableDef.parse(context);
        if (variableDef != null) {
            return variableDef;
        }

        return FunctionDef.parse(context);
    }

}
