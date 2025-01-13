package syntax.node.symbol.processor;

import syntax.node.Node;
import syntax.node.symbol.BuiltInTypes;
import syspro.tm.symbols.SemanticSymbol;
import syspro.tm.symbols.TypeSymbol;

public class IdentifierNameExpressionProcessor implements SymbolProcessor {

    private final BuiltInTypes builtInTypes = new BuiltInTypes();

    @Override
    public SemanticSymbol processSymbol(Node node) {
        try {
            node = (Node) node.descendants(false).getFirst();
            String identifierName = node.token().toString();

            TypeSymbol builtInType = builtInTypes.get(identifierName);
            if (builtInType != null) {
                return builtInType;
            }

            if (node.getParent() == null) {
                node.addInvalidRange(node.span(), "Unresolved identifier: " + identifierName);
                return null;
            }

            SemanticSymbol symbol = node.getParent()
                    .getSymbols(identifierName).stream()
                    .findFirst()
                    .orElse(null);

            if (symbol == null) {
                node.addInvalidRange(node.span(), "Unresolved identifier: " + identifierName);
            }

            return symbol;
        } catch (Exception e) {
            node.addInvalidRange(node.span(), "Error resolving identifier.");
            return null;
        }
    }
}