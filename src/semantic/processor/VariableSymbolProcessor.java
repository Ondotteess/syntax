package semantic.processor;

import syspro.tm.symbols.SemanticSymbol;
import syntax.node.Node;
import syntax.node.symbol.NodeVariableSymbol;

public class VariableSymbolProcessor implements SymbolProcessor {

    @Override
    public SemanticSymbol processSymbol(Node node) {
        String variableName = node.slot(1).token().toString();
        return new NodeVariableSymbol(variableName, null, null, null, node);
    }
}
