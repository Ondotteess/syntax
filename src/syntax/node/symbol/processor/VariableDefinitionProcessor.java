package syntax.node.symbol.processor;

import syntax.node.Node;
import syntax.node.symbol.NodeVariableSymbol;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.SemanticSymbol;
import syspro.tm.symbols.SymbolKind;
import syspro.tm.symbols.TypeLikeSymbol;

public class VariableDefinitionProcessor implements SymbolProcessor {
    @Override
    public SemanticSymbol processSymbol(Node node) {
        SyntaxNode nameNode = node.slot(1);
        if (nameNode == null || nameNode.token() == null) {
            node.addInvalidRange(node.span(), "Variable definition is missing a name.");
            return null;
        }

        String variableName = nameNode.token().toString();

        SyntaxNode typeNode = node.slot(3);
        TypeLikeSymbol variableType = null;
        if (typeNode instanceof Node typeNodeWithSymbols) {
            variableType = (TypeLikeSymbol) typeNodeWithSymbols.symbol();
        }

        if (variableType == null) {
            node.addInvalidRange(node.span(), "Variable type is not defined.");
            return null;
        }

        NodeVariableSymbol variableSymbol = new NodeVariableSymbol(
                variableName,
                variableType,
                node.getParent().symbol(),
                SymbolKind.FIELD,
                node
        );

        node.addSymbol(variableName, variableSymbol);
        return variableSymbol;
    }

}

