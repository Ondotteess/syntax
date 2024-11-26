package syntax.rule;

import syntax.Context;
import syntax.node.Node;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class VariableDefStmt implements Rule{

    public static SyntaxNode parse(Context context){
        Node vds = new Node(SyntaxKind.VARIABLE_DEFINITION_STATEMENT);
        SyntaxNode vd = VariableDef.parse(context);
        vds.addChild(vd);
        return vds;
    }

}
