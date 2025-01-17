package syntax.node.symbol.processor;

import syntax.node.Node;
import syspro.tm.symbols.SemanticSymbol;
import syspro.tm.symbols.TypeParameterSymbol;
import syspro.tm.symbols.TypeSymbol;

import java.util.List;

public class FunctionSymbolProcessor implements SymbolProcessor {
    @Override
    public SemanticSymbol processSymbol(Node node) {
        SemanticSymbol owner = node.getParent().symbol();
        if (!(owner instanceof TypeSymbol typeOwner)) {
            return null;
        }

        return node.createFunctionSymbol(typeOwner, (List<TypeParameterSymbol>) ((TypeSymbol) owner).typeArguments());
    }
}
