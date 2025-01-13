package syntax.node.symbol.processor;

import syntax.node.Node;
import syntax.node.symbol.NodeTypeParameterSymbol;
import syntax.node.symbol.NodeTypeSymbol;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.*;

import java.util.ArrayList;
import java.util.Collection;
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


        SyntaxNode typeParamsNode = node.slot(3);
        if (typeParamsNode != null && typeParamsNode.kind() == SyntaxKind.SEPARATED_LIST) {
            List<TypeParameterSymbol> typeParameters = ((Node) typeParamsNode)
                    .parseTypeParameters(typeParamsNode, typeSymbol, node);

            for (TypeParameterSymbol typeParameter : typeParameters) {
                node.addSymbol(typeParameter.name(), typeParameter);
            }

            typeSymbol.addTypeParameters(typeParameters);
        }


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

                    for (var a: ((Node)node.slot(5).slot(1)).children) {
                        GenericNameExpressionProcessor pr = new GenericNameExpressionProcessor();
                        TypeSymbol baseType = (TypeSymbol) pr.processSymbol((Node) a);
                        ((Node) a).cachedSymbol = baseType;
                        updatedTypeArguments.addAll((Collection<? extends TypeParameterSymbol>) baseType.typeArguments());
                        var b = 11;
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


        if (node.slotCount() > 7 && node.slot(7) != null) {
            for (SyntaxNode child : ((Node)node.slot(7)).children) {
                if (child instanceof Node childNode) {
                    if (childNode.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                        childNode.setParent(node);
                        childNode.type = (node);
                        SemanticSymbol functionSymbol = childNode.createFunctionSymbol(typeSymbol, typeSymbol.typeParameters);
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
        node.parent.addSymbol(typeName, typeSymbol);
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