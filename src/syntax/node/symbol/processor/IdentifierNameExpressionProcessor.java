package syntax.node.symbol.processor;

import syntax.node.Node;
import syspro.tm.symbols.SemanticSymbol;

public class IdentifierNameExpressionProcessor implements SymbolProcessor {
    @Override
    public SemanticSymbol processSymbol(Node node) {

        try {
            node = (Node) node.descendants(false).getFirst();
            String identifierName = node.token().toString();
        } catch (Exception e) {
            System.out.println("Cant got name");
        }
        String identifierName = node.token().toString();

        if (node.getParent() == null) {
            System.out.println("Cant got parent");
        }

        SemanticSymbol symbol = node.getParent()
                .getSymbols(identifierName).stream()
                .findFirst()
                .orElse(null);

        if (symbol == null) {
            node.addInvalidRange(node.span(), "Unresolved identifier: " + identifierName);
            return null;
        }

        return symbol;
    }


}