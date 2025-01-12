package syntax.node.symbol.processor;

import syntax.node.Node;
import syntax.node.symbol.NodeTypeParameterSymbol;
import syntax.node.symbol.NodeTypeSymbol;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.SemanticSymbol;
import syspro.tm.symbols.SyntaxNodeWithSymbols;
import syspro.tm.symbols.TypeLikeSymbol;

import java.util.ArrayList;
import java.util.List;

public class TypeParameterSymbolProcessor implements SymbolProcessor {

    @Override
    public SemanticSymbol processSymbol(Node node) {
        SemanticSymbol owner = node.getParent().symbol();

        if (owner == null) {
            owner = resolveOwnerFromParent(node.getParent());
            if (owner == null) {
                return null;
            }
        }

        NodeTypeParameterSymbol typeParameterSymbol = parseTypeParameter(node, owner);

        if (node.isDuplicate(typeParameterSymbol.name(), typeParameterSymbol)) {
            node.addInvalidRange(node.span(), "Duplicate type parameter: " + typeParameterSymbol.name());
            return null;
        }

        node.addSymbol(typeParameterSymbol.name(), typeParameterSymbol);

        return typeParameterSymbol;
    }

    private SemanticSymbol resolveOwnerFromParent(Node parent) {
        if (parent == null) {
            return null;
        }

        for (SemanticSymbol symbol : parent.getSymbols(parent.slot(1).token().toString())) {
            if (symbol instanceof NodeTypeSymbol) {
                return symbol;
            }
        }

        return null;
    }

    private NodeTypeParameterSymbol parseTypeParameter(SyntaxNode typeParameterNode, SemanticSymbol owner) {
        if (typeParameterNode.kind() != SyntaxKind.TYPE_PARAMETER_DEFINITION) {
            throw new IllegalArgumentException("Invalid node type: expected TYPE_PARAMETER_DEFINITION");
        }

        SyntaxNode nameNode = typeParameterNode.slot(0);
        if (nameNode == null || nameNode.token() == null) {
            throw new IllegalStateException("Type parameter definition is missing an identifier");
        }

        String parameterName = nameNode.token().toString();

        SyntaxNode boundsNode = typeParameterNode.slot(1);
        List<TypeLikeSymbol> bounds = new ArrayList<>();
        if (boundsNode instanceof SyntaxNodeWithSymbols nodeWithSymbols) {
            bounds.add((TypeLikeSymbol) nodeWithSymbols.symbol());
        }

        return new NodeTypeParameterSymbol(parameterName, owner, typeParameterNode, bounds);
    }
}