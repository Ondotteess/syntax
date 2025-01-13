package semantic.processor;

import syspro.tm.symbols.SemanticSymbol;
import syntax.node.Node;

public interface SymbolProcessor {

    SemanticSymbol processSymbol(Node node);
}
