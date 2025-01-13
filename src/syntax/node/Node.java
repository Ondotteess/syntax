package syntax.node;

import java.util.*;

import syntax.code.Code;
import syntax.node.symbol.*;
import syntax.node.symbol.processor.*;
import syspro.tm.lexer.Token;
import syspro.tm.parser.*;
import syspro.tm.symbols.*;


public class Node implements SyntaxNodeWithSymbols {
    public AnySyntaxKind kind;
    public Token token;
    public ArrayList<SyntaxNode> children;
    public List<Diagnostic> diagnostics;
    public Map<String, List<SemanticSymbol>> localSymbolTable = new HashMap<>();
    public SemanticSymbol cachedSymbol;
    public BuiltInTypes builtInTypes;

    public SemanticSymbol symbol;

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
        this.builtInTypes = new BuiltInTypes();
        this.kind = kind;
        this.token = token;
        this.children = new ArrayList<>();
        this.diagnostics = new ArrayList<>();
        if (kind instanceof SyntaxKind && ((SyntaxKind) kind).equals(SyntaxKind.TYPE_DEFINITION)) {
            type = this;
        }
        fillBuiltIn();
    }

    private void fillBuiltIn() {
        localSymbolTable.put("Int32", createBuiltInType("Int32"));
        localSymbolTable.put("Int64", createBuiltInType("Int64"));
        localSymbolTable.put("UInt32", createBuiltInType("UInt32"));
        localSymbolTable.put("UInt64", createBuiltInType("UInt64"));
        localSymbolTable.put("Boolean", createBuiltInType("Boolean"));
        localSymbolTable.put("Rune", createBuiltInType("Rune"));
    }

    List<SemanticSymbol> createBuiltInType(String name) {
        return Collections.singletonList(new TypeSymbol() {
            @Override
            public boolean isAbstract() {
                return false;
            }

            @Override
            public List<? extends TypeSymbol> baseTypes() {
                return List.of();
            }

            @Override
            public List<? extends TypeLikeSymbol> typeArguments() {
                return List.of();
            }

            @Override
            public TypeSymbol originalDefinition() {
                return null;
            }

            @Override
            public TypeSymbol construct(List<? extends TypeLikeSymbol> list) {
                return null;
            }

            @Override
            public List<? extends MemberSymbol> members() {
                return List.of();
            }

            @Override
            public SymbolKind kind() {
                return null;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public SyntaxNode definition() {
                return null;
            }
        });
    }

    public Node(Node node){
        this.kind = node.kind ;
        this.token= node.token;
        this.children= node.children;
        this.diagnostics= node.diagnostics;
        this.cachedSymbol= null;
        this.parent= node.parent;
        this.type= node.type;
    }


    public void addChild(SyntaxNode child) {
        children.add(child);
        if (child instanceof Node childNode) {
            childNode.setParent(this);
        }
    }

    public List<SemanticSymbol> getSymbols(String name) {

        List<SemanticSymbol> symbols = localSymbolTable.getOrDefault(name, List.of());
        if (!symbols.isEmpty()) {
            return symbols;
        }

        TypeSymbol builtInType = builtInTypes.get(name);
        if (builtInType != null) {
            return List.of(builtInType);
        }

        return List.of();
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

    //@Override
            //public SemanticSymbol symbol() {
        //    return symbol;
        //}

    @Override
    public SemanticSymbol symbol() {
        if (cachedSymbol == null) {
            SymbolProcessor processor = getSymbolProcessorForKind(kind());
            if (processor != null) {
                cachedSymbol = processor.processSymbol(this);
            }
        }
//
        return cachedSymbol;
//
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
        } else if (kind == SyntaxKind.GENERIC_NAME_EXPRESSION) {
            return new GenericNameExpressionProcessor();
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


    public SemanticSymbol createFunctionSymbol(TypeSymbol owner, List<TypeParameterSymbol> typeParams) {
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
            if (returnTypeNode.kind() == SyntaxKind.GENERIC_NAME_EXPRESSION) {
                GenericNameExpressionProcessor pr = new GenericNameExpressionProcessor();
                returnType = (TypeLikeSymbol) pr.processSymbol((Node) returnTypeNode);
            }
        }

        if (returnType == null && returnTypeNode != null) {
            String typeName = returnTypeNode.slot(0).token().toString();
            returnType = resolveTypeFromDefinitions(typeName);
        }

        if (functionName.equals("this")) {

            returnType = owner;
            ((NodeTypeSymbol) returnType).addTypeParameters(typeParams);
        }


        // if (returnType != null) {
        //     returnType = adjustTypeOwner(returnType, owner);
        // }

        SyntaxNode parametersNode = children.size() > 4 ? children.get(4) : null;

        SyntaxNode statements = children.size() > 9 ? children.get(9) : null;



        NodeFunctionSymbol functionSymbol = new NodeFunctionSymbol(functionName, returnType, List.of(), owner, this);


        List<VariableSymbol> locals = collectLocals(statements, functionSymbol);

        functionSymbol.locals = locals;


        List<VariableSymbol> parameters = (parametersNode != null)
                ? parseParameters(parametersNode, functionSymbol)
                : List.of();

        functionSymbol.setParameters(parameters);

        return functionSymbol;
    }

    private List<VariableSymbol> collectLocals(SyntaxNode statementsNode, FunctionSymbol functionOwner) {
        List<VariableSymbol> locals = new ArrayList<>();

        if (statementsNode == null) {
            return locals;
        }

        for (SyntaxNode statement : ((Node)statementsNode).children) {
            if (statement.kind() == SyntaxKind.VARIABLE_DEFINITION_STATEMENT) {
                Node variableNode = (Node) statement.slot(0);
                VariableDefinitionProcessor variableProcessor = new VariableDefinitionProcessor();
                NodeVariableSymbol localVariable = (NodeVariableSymbol) variableProcessor.processSymbol(variableNode, functionOwner, SymbolKind.LOCAL);

                variableNode.cachedSymbol = localVariable;
                localVariable.kind = SymbolKind.LOCAL;
                localVariable.owner = functionOwner;
                locals.add(localVariable);
            } else if (statement.kind() == SyntaxKind.IF_STATEMENT) {

                Node condition = (Node) statement.slot(1);
                Node trueBranch = (Node) statement.slot(3);
                Node falseBranch = (Node) statement.slot(7);

                locals.addAll(collectLocals(trueBranch, functionOwner));

                if (falseBranch != null) {
                    locals.addAll(collectLocals(falseBranch, functionOwner));
                }
            } else if (statement.kind() == SyntaxKind.FOR_STATEMENT) {
                Node initializer = (Node) statement.slot(1);
                Node body = (Node) statement.slot(5);

                //locals.addAll(collectLocals(initializer, functionOwner));

                NodeVariableSymbol localVariable = new NodeVariableSymbol(
                        initializer.slot(0).token().toString(),
                        null,
                        functionOwner,
                        SymbolKind.LOCAL,
                        statement
                );
                ((Node) statement).cachedSymbol = localVariable;

                locals.add(localVariable);
                locals.addAll(collectLocals(body, functionOwner));
            } else if (statement.kind() == SyntaxKind.IDENTIFIER) {

               // locals.add(localVariable);
            } else if (statement.kind() == SyntaxKind.RETURN_STATEMENT) {

            }
        }

        return locals;
    }



    private TypeLikeSymbol adjustTypeOwner(TypeLikeSymbol type, SemanticSymbol newOwner) {
        if (type instanceof NodeTypeParameterSymbol param) {
            if (newOwner instanceof NodeTypeSymbol ownerType) {
                for (TypeLikeSymbol existing : ownerType.typeArguments()) {
                    if (existing.name().equals(param.name())) {
                        return existing;
                    }
                }
            }

            NodeTypeParameterSymbol adjusted = new NodeTypeParameterSymbol(
                    param.name(),
                    newOwner,
                    param.definition(),
                    (List<TypeLikeSymbol>) param.bounds()
            );
            if (newOwner instanceof NodeTypeSymbol ownerType) {
                ownerType.addTypeParameters((List<TypeParameterSymbol>) adjusted);
            }
            return adjusted;
        } else if (type instanceof NodeTypeSymbol baseType) {
            List<TypeParameterSymbol> updatedArguments = new ArrayList<>();
            for (TypeLikeSymbol argument : baseType.typeArguments()) {
                TypeLikeSymbol adjusted = adjustTypeOwner(argument, newOwner);
                if (adjusted instanceof TypeParameterSymbol paramAdjusted) {
                    updatedArguments.add(paramAdjusted);
                }
            }
            ((NodeTypeSymbol) type).addTypeParameters(updatedArguments);
            return type;
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

        return null;
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