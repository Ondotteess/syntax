package semantic;

import syntax.node.Node;
import syntax.node.symbol.NodeFunctionSymbol;
import syntax.node.symbol.NodeTypeParameterSymbol;
import syntax.node.symbol.NodeTypeSymbol;
import syntax.node.symbol.NodeVariableSymbol;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.*;

import java.util.ArrayList;
import java.util.List;

public class TypeSymbolGen implements TypeSymbol {
    private String name;
    private SyntaxNode definition;
    private  List<MemberSymbol> members = new ArrayList<>();
    private  List<TypeParameterSymbol> typeParameters = new ArrayList<>();
    private  List<TypeSymbol> baseTypes = new ArrayList<>();
    private  boolean isAbstract;


    public TypeSymbolGen(String name, SyntaxNode definition) {
        this.name = name;
        this.definition = definition;

        this.members = processMembers();
        this.baseTypes = processBaseTypes();
        this.typeParameters = processParameters();

        this.isAbstract = determineAbstract();
    }


    private List<TypeParameterSymbol> processParameters() {
        SyntaxNode typeParamsNode = definition.slot(3);
        if (typeParamsNode == null) {
            return List.of();
        }

        List<TypeParameterSymbol> parameters = new ArrayList<>();
        for (SyntaxNode paramNode : ((Node) typeParamsNode).getChildren()) {
            if (paramNode instanceof Node paramSyntaxNode) {
                String paramName = paramSyntaxNode.slot(0).token().toString();
                NodeTypeParameterSymbol paramSymbol = new NodeTypeParameterSymbol(
                        paramName,
                        this,
                        paramSyntaxNode,
                        List.of()
                );
                parameters.add(paramSymbol);
            }
        }
        return parameters;
    }


    private List<TypeSymbol> processBaseTypes() {
        SyntaxNode typeBoundNode = definition.slot(5);
        if (typeBoundNode == null) {
            return List.of();
        }

        List<TypeSymbol> baseTypesList = new ArrayList<>();
        for (SyntaxNode baseTypeNode : ((Node)((Node) typeBoundNode).getChildren().get(1)).children) {
            if (baseTypeNode instanceof Node baseNode) {
                TypeSymbol baseTypeSymbol = processBaseType(baseNode);
                if (baseTypeSymbol != null) {
                    baseTypesList.add(baseTypeSymbol);
                }
            }
        }
        return baseTypesList;
    }

    private TypeSymbol processBaseType(SyntaxNode node) {
        return null;
    }

    private List<MemberSymbol> processMembers() {
        SyntaxNode membersNode = definition.slot(7);
        if (membersNode == null) {
            return List.of();
        }

        List<MemberSymbol> memberSymbols = new ArrayList<>();
        for (SyntaxNode memberNode : ((Node) membersNode).getChildren()) {
            if (memberNode instanceof Node memberSyntaxNode) {
                MemberSymbol memberSymbol = processMember(memberSyntaxNode);
                if (memberSymbol != null) {
                    memberSymbols.add(memberSymbol);
                }
            }
        }
        return memberSymbols;
    }

    private MemberSymbol processMember(Node memberNode) {
        if (memberNode.kind() == SyntaxKind.FUNCTION_DEFINITION) {
            return processFunction(memberNode);
        } else if (memberNode.kind() == SyntaxKind.VARIABLE_DEFINITION) {
            return processVariable(memberNode);
        }
        return null;
    }

    private FunctionSymbol processFunction(Node functionNode) {
        SyntaxNode nameNode = functionNode.slot(2);
        if (nameNode == null || nameNode.token() == null) {
            return null;
        }

        String functionName = nameNode.token().toString();
        //NodeFunctionSymbol functionSymbol = new NodeFunctionSymbol(functionName, null, List.of(), this, functionNode);

       // List<VariableSymbol> parameters = processFunctionParameters(functionNode, functionSymbol);
        //functionSymbol.setParameters(parameters);

        return null;
    }

    private List<VariableSymbol> processFunctionParameters(Node functionNode, FunctionSymbol owner) {
        SyntaxNode paramsNode = functionNode.slot(4);
        if (paramsNode == null) {
            return List.of();
        }

        List<VariableSymbol> parameters = new ArrayList<>();
        for (SyntaxNode paramNode : ((Node) paramsNode).getChildren()) {
            if ((paramNode.kind()) == SyntaxKind.PARAMETER_DEFINITION ) {
                String paramName = paramNode.slot(0).token().toString();
                NodeVariableSymbol paramSymbol = new NodeVariableSymbol(
                        paramName,
                        null,
                        owner,
                        SymbolKind.PARAMETER,
                        paramNode
                );
                parameters.add(paramSymbol);
            }
        }
        return parameters;
    }

    private VariableSymbol processVariable(Node variableNode) {
        SyntaxNode nameNode = variableNode.slot(1);
        if (nameNode == null || nameNode.token() == null) {
            return null;
        }

        String variableName = nameNode.token().toString();
        return new NodeVariableSymbol(variableName, null, this, SymbolKind.FIELD, variableNode);
    }

    private TypeSymbol resolveBaseTypeSymbol(String baseTypeName, Node baseTypeNode) {
        return null; // TODO: Реализовать
    }

    @Override
    public List<? extends TypeLikeSymbol> typeArguments() {
        return typeParameters;
    }

    @Override
    public List<? extends MemberSymbol> members() {
        return members;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    private boolean determineAbstract() {
        if (kind() == SymbolKind.INTERFACE) {
            return true;
        }

        for (MemberSymbol member : members) {
            if (member instanceof FunctionSymbol function && function.isAbstract()) {
                return true;
            }
        }

        for (TypeSymbol baseType : baseTypes()) {
            for (MemberSymbol baseMember : baseType.members()) {
                if (baseMember instanceof FunctionSymbol baseFunction && baseFunction.isAbstract()) {
                    boolean isOverridden = members.stream()
                            .filter(FunctionSymbol.class::isInstance)
                            .map(FunctionSymbol.class::cast)
                            .anyMatch(f -> f.name().equals(baseFunction.name()));
                    if (!isOverridden) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public List<? extends TypeSymbol> baseTypes() {
        return baseTypes;
    }

    @Override
    public TypeSymbol originalDefinition() {
        return this; //
    }

    @Override
    public TypeSymbol construct(List<? extends TypeLikeSymbol> var1) {
        return this;
    }

    @Override
    public SymbolKind kind() {
        String v;
        if (definition != null) {
            v = definition.descendants(false).getFirst().token().toString();
            return switch (v) {
                case "class" -> SymbolKind.CLASS;
                case "object" -> SymbolKind.OBJECT;
                case "interface" -> SymbolKind.INTERFACE;
                default -> SymbolKind.CLASS;
            };
        } else {
            return SymbolKind.CLASS;
        }

    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public SyntaxNode definition() {
        return definition;
    }

    public Iterable<? extends TypeParameterSymbol> typeParameters() {
        return typeParameters;
    }
}
