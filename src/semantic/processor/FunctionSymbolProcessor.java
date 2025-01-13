package semantic.processor;

import syspro.tm.symbols.SemanticSymbol;
import syntax.node.Node;
import syntax.node.symbol.NodeFunctionSymbol;

import java.util.List;

public class FunctionSymbolProcessor implements SymbolProcessor {

    @Override
    public SemanticSymbol processSymbol(Node node) {
        String functionName = node.slot(2).token().toString();
        NodeFunctionSymbol functionSymbol = new NodeFunctionSymbol(functionName, null, List.of(), null, node);

        processParameters(node, functionSymbol);

        return functionSymbol;
    }

    private void processParameters(Node node, NodeFunctionSymbol functionSymbol) {

    }
}