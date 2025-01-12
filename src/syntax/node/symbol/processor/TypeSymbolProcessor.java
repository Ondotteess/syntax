package syntax.node.symbol.processor;

import syntax.node.Node;
import syntax.node.symbol.NodeTypeParameterSymbol;
import syntax.node.symbol.NodeTypeSymbol;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.*;

import java.util.ArrayList;
import java.util.List;

import static syspro.tm.parser.SyntaxKind.TYPE_BOUND;

public class TypeSymbolProcessor implements SymbolProcessor {
    @Override
    public SemanticSymbol processSymbol(Node node) {
        SyntaxNode nameNode = node.slot(1);
        if (nameNode == null || nameNode.token() == null) {
            return null;
        }

        setTypes(node, node);

        String typeName = nameNode.token().toString();
        NodeTypeSymbol typeSymbol = new NodeTypeSymbol(typeName, node);

        if (node.isDuplicate(typeName, typeSymbol)) {
            List<SemanticSymbol> existingSymbols = node.getSymbols(typeName);
            for (SemanticSymbol existingSymbol : existingSymbols) {
                if (existingSymbol instanceof NodeTypeSymbol) {
                    return existingSymbol;
                }
            }
            node.addInvalidRange(nameNode.span(), "Duplicate type definition: " + typeName);
            return null;
        }

        node.addSymbol(typeName, typeSymbol);

        SyntaxNode typeBoundNode = node.slot(5);
        if (typeBoundNode != null && typeBoundNode.kind() == TYPE_BOUND) {
            SyntaxNode baseTypeNode = typeBoundNode.slot(1).slot(0);
            if (baseTypeNode != null && baseTypeNode.slot(0).token() != null) {
                String baseTypeName = baseTypeNode.slot(0).token().toString();

                Node rootNode = node.getParent();

                TypeSymbol baseTypeSymbol = null;
                for (SyntaxNode sibling : rootNode.getChildren()) {
                    if (sibling instanceof Node siblingNode && siblingNode.kind() == SyntaxKind.TYPE_DEFINITION) {
                        Node siblingNameNode = (Node) siblingNode.slot(1);
                        if (siblingNameNode != null && siblingNameNode.token() != null) {
                            String siblingTypeName = siblingNameNode.token().toString();
                            if (siblingTypeName.equals(baseTypeName)) {
                                baseTypeSymbol = (TypeSymbol) siblingNode.symbol();
                                break;
                            }
                        }
                    }
                }

                if (baseTypeSymbol != null) {
                    List<TypeParameterSymbol> updatedTypeArguments = new ArrayList<>();
                    for (TypeLikeSymbol typeArgument : baseTypeSymbol.typeArguments()) {
                        if (typeArgument instanceof NodeTypeParameterSymbol param) {
                            if (param.owner() == typeSymbol) {
                                node.addInvalidRange(baseTypeNode.span(), "Recursive type parameter detected: " + param.name());
                                continue;
                            }
                            NodeTypeParameterSymbol updatedParam = new NodeTypeParameterSymbol(
                                    param.name(),
                                    (SemanticSymbol) typeSymbol,
                                    param.definition(),
                                    (List<TypeLikeSymbol>) param.bounds()
                            );
                            updatedTypeArguments.add(updatedParam);
                        }
                    }

                    NodeTypeSymbol updatedBaseTypeSymbol = new NodeTypeSymbol(baseTypeSymbol.name(), baseTypeSymbol.definition());
                    updatedBaseTypeSymbol.addTypeParameters(updatedTypeArguments);

                    typeSymbol.addBaseType(updatedBaseTypeSymbol);
                } else {
                    node.addInvalidRange(baseTypeNode.span(), "Base type not found: " + baseTypeName);
                }
            } else {
                node.addInvalidRange(baseTypeNode != null ? baseTypeNode.span() : node.span(), "Invalid base type.");
            }
        }

        SyntaxNode typeParamsNode = node.slot(3);
        if (typeParamsNode != null && typeParamsNode.kind() == SyntaxKind.SEPARATED_LIST) {
            List<TypeParameterSymbol> typeParameters = ((Node) typeParamsNode)
                    .parseTypeParameters(typeParamsNode, typeSymbol, node);

            for (TypeParameterSymbol typeParameter : typeParameters) {
                node.addSymbol(typeParameter.name(), typeParameter);
            }

            typeSymbol.addTypeParameters(typeParameters);
        }

        if (node.slotCount() > 7 && node.slot(7) != null) {
            for (SyntaxNode child : node.slot(7).descendants(false)) {
                if (child instanceof Node childNode) {
                    if (childNode.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                        childNode.setParent(node);
                        childNode.type = (node);
                        SemanticSymbol functionSymbol = childNode.createFunctionSymbol(typeSymbol);
                        if (functionSymbol instanceof MemberSymbol memberSymbol) {
                            typeSymbol.addMember(memberSymbol);
                        }
                    } else if (childNode.kind() == SyntaxKind.VARIABLE_DEFINITION) {
                        SemanticSymbol variableSymbol = childNode.createVariableSymbol(typeSymbol);
                        if (variableSymbol instanceof MemberSymbol memberSymbol) {
                            typeSymbol.addMember(memberSymbol);
                        }
                    }
                }
            }
        }

        for (var a : typeSymbol.members()){
            if (a.kind() == SymbolKind.FUNCTION) {
            } else if (a.kind() == SymbolKind.FIELD) {

            }
        }

        return typeSymbol;
    }

    private void setTypes(SyntaxNode type, Node node) {
        for (SyntaxNode child: type.descendants(false)) {
            if (child instanceof Node ) {
                ((Node) child).setParent(node.type);
                ((Node) child).type = node.type;
                setTypes(child, node);
            } else {
                setTypes(child, node);
            }
        }
    }
}
