package syntax.node.symbol.processor;

import syntax.node.Node;
import syntax.node.symbol.NodeTypeParameterSymbol;
import syntax.node.symbol.NodeTypeSymbol;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.SemanticSymbol;

import syspro.tm.symbols.TypeLikeSymbol;
import syspro.tm.symbols.TypeParameterSymbol;
import syspro.tm.symbols.TypeSymbol;

import java.util.ArrayList;
import java.util.List;

public class GenericNameExpressionProcessor implements SymbolProcessor {

    @Override
    public SemanticSymbol processSymbol(Node node) {
        if (node == null || node.slotCount() == 0) {
            return null;
        }

        Node nameNode = (Node) node.slot(0);
        if (nameNode == null || nameNode.token() == null) {
            node.addInvalidRange(node.span(), "Invalid generic name expression: Missing identifier.");
            return null;
        }

        String typeName = nameNode.token().toString();

        SemanticSymbol baseSymbol = resolveSymbol(node, typeName);
        if (baseSymbol == null) {
            node.addInvalidRange(nameNode.span(), "Unknown type: " + typeName);
            return null;
        }

        if (!(baseSymbol instanceof TypeSymbol baseTypeSymbol)) {
            node.addInvalidRange(nameNode.span(), typeName + " is not a type.");
            return null;
        }

        if (node.slotCount() > 2) {
            Node typeArgumentsNode = (Node) node.slot(2);
            if (typeArgumentsNode != null) {
                TypeSymbol constructedType = processTypeArguments(baseTypeSymbol, typeArgumentsNode, node);
                return constructedType != null ? constructedType : baseTypeSymbol;
            }
        }

        return baseTypeSymbol;
    }

    private SemanticSymbol resolveSymbol(Node node, String typeName) {


        for (SemanticSymbol symbol : node.type.parent.getSymbols(typeName)) {
            if (symbol instanceof TypeSymbol || symbol instanceof NodeTypeParameterSymbol) {

                if (node.kind() == SyntaxKind.GENERIC_NAME_EXPRESSION) {
                    NodeTypeSymbol ts = new NodeTypeSymbol(symbol.name(), symbol.definition());
                    ArrayList<TypeParameterSymbol> ar = new ArrayList<>();

                    for (SyntaxNode child : ((Node) node.slot(2)).children) {
                        if (child.slot(0).token().toString().equals("T")) {
                            
                        }
                    }


                    ts.addTypeParameters(ar);
                    return ts;
                }

                TypeSymbolProcessor b = new TypeSymbolProcessor();
                return symbol;
            }
        }

        return null;
    }

    private TypeSymbol processTypeArguments(TypeSymbol baseType, Node typeArgumentsNode, Node node) {
        List<TypeLikeSymbol> typeArguments = new ArrayList<>();

        for (SyntaxNode child : typeArgumentsNode.getChildren()) {
            if (child instanceof Node typeArgumentNode) {
                SemanticSymbol typeArgumentSymbol = typeArgumentNode.symbol();
                if (typeArgumentSymbol instanceof TypeLikeSymbol) {
                    typeArguments.add((TypeLikeSymbol) typeArgumentSymbol);
                } else {
                    node.addInvalidRange(typeArgumentNode.span(), "Invalid type argument.");
                    return null;
                }
            }
        }

        return baseType.construct(typeArguments);
    }
}

