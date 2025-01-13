package semantic.processor;

import semantic.TypeSymbolGen;
import syspro.tm.symbols.SemanticSymbol;
import syntax.node.Node;
import syntax.node.symbol.NodeTypeSymbol;

public class TypeSymbolProcessor implements SymbolProcessor {

    @Override
    public SemanticSymbol processSymbol(Node node) {
        String typeName = node.slot(1).token().toString();
        TypeSymbolGen typeSymbol = new TypeSymbolGen(typeName, node);

        return typeSymbol;
    }

}