package syntax.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import syntax.code.Code;
import syntax.node.symbol.*;
import syntax.node.symbol.processor.*;
import syspro.tm.lexer.Token;
import syspro.tm.parser.*;
import syspro.tm.symbols.*;


public class Node implements SyntaxNodeWithSymbols {
    private final AnySyntaxKind kind;
    private final Token token;
    private final ArrayList<SyntaxNode> children;
    private final List<Diagnostic> diagnostics;
    private final Map<String, List<SemanticSymbol>> localSymbolTable = new HashMap<>();
    private SemanticSymbol cachedSymbol;

    public Node parent;
    public Node type;

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }

    public void addSymbol(String name, SemanticSymbol symbol) {
        localSymbolTable.computeIfAbsent(name, k -> new ArrayList<>()).add(symbol);
    }

    public Node(SyntaxKind kind) {
        this(kind, null);
    }

    public Node(AnySyntaxKind kind, Token token) {
        this.kind = kind;
        this.token = token;
        this.children = new ArrayList<>();
        this.diagnostics = new ArrayList<>();
        if (kind instanceof SyntaxKind && ((SyntaxKind) kind).equals(SyntaxKind.TYPE_DEFINITION)) {
            type = this;
        }
    }


    public void addChild(SyntaxNode child) {
        children.add(child);
        if (child instanceof Node childNode) {
            childNode.setParent(this);
        }
    }

    public List<SemanticSymbol> getSymbols(String name) {

        return localSymbolTable.getOrDefault(name, List.of());
    }

    public boolean isDuplicate(String name, SemanticSymbol symbol) {
        List<SemanticSymbol> symbols = getSymbols(name);
        for (SemanticSymbol existing : symbols) {
            if (existing.kind() == symbol.kind() &&
                    (symbol instanceof FunctionSymbol && existing instanceof FunctionSymbol)) {
                return areParametersConflicting((FunctionSymbol) existing, (FunctionSymbol) symbol);
            }
            if (existing.kind() == symbol.kind() && existing.name().equals(symbol.name())) {
                return true;
            }
        }
        return false;
    }

    private boolean areParametersConflicting(FunctionSymbol existing, FunctionSymbol newFunction) {
        List<? extends VariableSymbol> existingParams = existing.parameters();
        List<? extends VariableSymbol> newParams = newFunction.parameters();
        if (existingParams.size() != newParams.size()) {
            return false;
        }
        for (int i = 0; i < existingParams.size(); i++) {
            if (!existingParams.get(i).type().equals(newParams.get(i).type())) {
                return false;
            }
        }
        return true;
    }


    public List<SyntaxNode> getChildren(){
        return children;
    }

    public void addInvalidRange(TextSpan span, String message) {
        Diagnostic diagnostic = new Diagnostic(
                new DiagnosticInfo(Code.SYNTAX, new Object[]{message}),
                span,
                List.of()
        );
        diagnostics.add(diagnostic);
    }

    @Override
    public AnySyntaxKind kind() {
        return kind;
    }

    @Override
    public int slotCount() {
        return children.size();
    }

    @Override
    public SyntaxNode slot(int index) {
        if (index < 0 || index >= children.size()) {
            return null;
        }
        return children.get(index);
    }

    @Override
    public Token token() {
        return token;
    }

    public List<Diagnostic> collectDiagnostics() {
        List<Diagnostic> allDiagnostics = new ArrayList<>(diagnostics);
        for (SyntaxNode child : children) {
            if (child instanceof Node) {
                allDiagnostics.addAll(((Node) child).collectDiagnostics());
            }
        }
        return allDiagnostics;
    }

    private void setTypes(SyntaxNode type) {
        for (SyntaxNode child: type.descendants(false)) {
            if (child instanceof Node ) {
                ((Node) child).setParent(this.type);
                ((Node) child).type = this.type;
                setTypes(child);
            } else {
                setTypes(child);
            }
        }
    }

    @Override
    public SemanticSymbol symbol() {
        if (cachedSymbol == null) {
            SymbolProcessor processor = getSymbolProcessorForKind(kind());
            if (processor != null) {
                SemanticSymbol newSymbol = processor.processSymbol(this);

                if (newSymbol instanceof NodeTypeSymbol typeSymbol) {
                    updatePlaceholders(typeSymbol);
                }

                cachedSymbol = newSymbol;
            }
        }

        return cachedSymbol;

    }

    private void updatePlaceholders(NodeTypeSymbol definedType) {
        List<SemanticSymbol> placeholders = getSymbols(definedType.name());

        for (SemanticSymbol placeholder : placeholders) {
            if (placeholder instanceof NodeTypeSymbol placeholderType) {
                if (placeholderType.definition() == null) {
                    placeholderType.setDefinition(definedType.definition());
                }
            }
        }
    }



    private SymbolProcessor getSymbolProcessorForKind(AnySyntaxKind kind) {
        if (kind == SyntaxKind.TYPE_DEFINITION) {
            return new TypeSymbolProcessor();
        } else if (kind == SyntaxKind.FUNCTION_DEFINITION) {
            return new FunctionSymbolProcessor();
        } else if (kind == SyntaxKind.TYPE_PARAMETER_DEFINITION) {
            return new TypeParameterSymbolProcessor();
        } else if (kind == SyntaxKind.IDENTIFIER_NAME_EXPRESSION) {
            return new IdentifierNameExpressionProcessor();
        } else if (kind == SyntaxKind.VARIABLE_DEFINITION) {
            return new VariableDefinitionProcessor();
        }

        return null;
    }


    public List<TypeParameterSymbol> parseTypeParameters(SyntaxNode typeParamsNode, SemanticSymbol owner, Node node) {
        List<TypeParameterSymbol> typeParameters = new ArrayList<>();

        if (typeParamsNode.kind() == SyntaxKind.SEPARATED_LIST) {
            for (SyntaxNode child : typeParamsNode.descendants(false)) {
                if (child.kind() == SyntaxKind.TYPE_PARAMETER_DEFINITION) {
                    ((Node) child).setParent(node);
                    NodeTypeParameterSymbol typeParameterSymbol = parseTypeParameter(child, owner, node);

                    if (node.isDuplicate(typeParameterSymbol.name(), typeParameterSymbol)) {
                        node.addInvalidRange(child.span(), "Duplicate type parameter: " + typeParameterSymbol.name());
                    } else {
                        node.addSymbol(typeParameterSymbol.name(), typeParameterSymbol);
                        typeParameters.add(typeParameterSymbol);
                    }
                }
            }
        }

        return typeParameters;
    }


    private NodeTypeParameterSymbol parseTypeParameter(SyntaxNode typeParameterNode, SemanticSymbol owner, Node node) {

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


    public SemanticSymbol createFunctionSymbol(TypeSymbol owner) {
        SyntaxNode nameNode = children.size() > 2 ? children.get(2) : null;

        if (nameNode == null || nameNode.token() == null) {
            return null;
        }

        setTypes(this);

        String functionName = nameNode.token().toString();

        SyntaxNode returnTypeNode = children.size() > 7 ? children.get(7) : null;

        TypeLikeSymbol returnType = null;

        if (returnTypeNode instanceof SyntaxNodeWithSymbols nodeWithSymbols) {
            returnType = (TypeLikeSymbol) nodeWithSymbols.symbol();
        }

        if (returnType == null && returnTypeNode != null) {
            String typeName = returnTypeNode.slot(0).token().toString();
            returnType = resolveTypeFromDefinitions(typeName);
        }

        //
        // if (returnType != null) {
        //     returnType = adjustTypeOwner(returnType, owner);
        // }

        SyntaxNode parametersNode = children.size() > 4 ? children.get(4) : null;

        NodeFunctionSymbol functionSymbol = new NodeFunctionSymbol(functionName, returnType, List.of(), owner, this);

        List<VariableSymbol> parameters = (parametersNode != null)
                ? parseParameters(parametersNode, functionSymbol)
                : List.of();

        functionSymbol.setParameters(parameters);

        return functionSymbol;
    }

    private TypeLikeSymbol adjustTypeOwner(TypeLikeSymbol type, SemanticSymbol newOwner) {
        if (type instanceof NodeTypeParameterSymbol param) {
            return new NodeTypeParameterSymbol(
                    param.name(),
                    newOwner,
                    param.definition(),
                    (List<TypeLikeSymbol>) param.bounds()
            );
        } else if (type instanceof NodeTypeSymbol baseType) {
            List<TypeParameterSymbol> updatedArguments = new ArrayList<>();
            for (TypeLikeSymbol argument : baseType.typeArguments()) {
                if (argument instanceof TypeParameterSymbol parameterSymbol) {
                    updatedArguments.add((TypeParameterSymbol) adjustTypeOwner(parameterSymbol, newOwner));
                }
            }
            NodeTypeSymbol adjustedType = new NodeTypeSymbol(baseType.name(), baseType.definition());
            adjustedType.addTypeParameters(updatedArguments);
            return adjustedType;
        }
        return type;
    }



    private TypeLikeSymbol resolveTypeFromDefinitions(String typeName) {
        Node root = this.type.parent;

        for (SyntaxNode child : root.getChildren()) {
            if (child instanceof Node childNode && childNode.kind() == SyntaxKind.TYPE_DEFINITION) {
                SyntaxNode nameNode = childNode.slot(1);
                if (nameNode != null && nameNode.token() != null) {
                    String definedTypeName = nameNode.token().toString();
                    if (definedTypeName.equals(typeName)) {
                        return (TypeLikeSymbol) childNode.symbol();
                    }
                }
            }
        }

        return createPlaceholderType(typeName);
    }

    private NodeTypeSymbol createPlaceholderType(String typeName) {
        NodeTypeSymbol placeholderType = new NodeTypeSymbol(typeName, null);
        this.addSymbol(typeName, placeholderType);
        return placeholderType;
    }

    public SemanticSymbol createVariableSymbol(TypeSymbol owner) {
        SyntaxNode nameNode = children.size() > 1 ? children.get(1) : null;

        if (nameNode == null || nameNode.token() == null) {
            return null;
        }

        String variableName = nameNode.token().toString();

        SyntaxNode typeNode = children.size() > 2 ? children.get(2) : null;

        TypeLikeSymbol type = null;
        if (typeNode instanceof SyntaxNodeWithSymbols nodeWithSymbols) {
            type = (TypeLikeSymbol) nodeWithSymbols.symbol();
        }

        return new NodeVariableSymbol(variableName, type, owner, SymbolKind.FIELD, this);
    }


    private List<VariableSymbol> parseParameters(SyntaxNode parametersNode, FunctionSymbol owner) {
        List<VariableSymbol> parameters = new ArrayList<>();

        if (parametersNode.kind() == SyntaxKind.SEPARATED_LIST) {
            for (SyntaxNode child : parametersNode.descendants(false)) {
                if (child.kind() == SyntaxKind.PARAMETER_DEFINITION) {
                    SyntaxNode nameNode = child.slot(0);
                    SyntaxNode typeNode = child.slot(2);

                    if (nameNode != null && typeNode != null && nameNode.token() != null) {
                        String parameterName = nameNode.token().toString();

                        TypeLikeSymbol parameterType = null;

                        if (typeNode instanceof SyntaxNodeWithSymbols nodeWithSymbols) {
                            parameterType = (TypeLikeSymbol) nodeWithSymbols.symbol();
                        }

                        if (parameterType == null) {
                            String typeName = typeNode.slot(0).token().toString();
                            parameterType = resolveTypeFromDefinitions(typeName);
                        }

                        NodeVariableSymbol param = new NodeVariableSymbol(
                                parameterName,
                                parameterType,
                                owner,
                                SymbolKind.PARAMETER,
                                child
                        );

                        if (child instanceof Node childNode) {
                            childNode.cachedSymbol = param;
                            childNode.addSymbol(parameterName, param);
                        }

                        parameters.add(param);
                    }
                }
            }
        }

        return parameters;
    }





}