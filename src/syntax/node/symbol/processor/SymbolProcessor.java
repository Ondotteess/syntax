package syntax.node.symbol.processor;

import syntax.node.Node;
import syspro.tm.symbols.SemanticSymbol;

public interface SymbolProcessor {
    SemanticSymbol processSymbol(Node node);
}
