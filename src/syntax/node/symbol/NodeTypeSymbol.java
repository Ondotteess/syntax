package syntax.node.symbol;

import syntax.node.Node;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.*;

import java.util.ArrayList;
import java.util.List;

public class NodeTypeSymbol implements TypeSymbol {
    public  String name;
    public SyntaxNode definition;
    public  List<MemberSymbol> members = new ArrayList<>();
    public  List<TypeParameterSymbol> typeParameters = new ArrayList<>();
    public  List<TypeSymbol> baseTypes = new ArrayList<>();


    public boolean isAbstract;

    public NodeTypeSymbol(String name, SyntaxNode definition) {
        this.name = name;
        this.definition = definition;
        this.isAbstract = determineAbstract();
    }

    public NodeTypeSymbol(NodeTypeSymbol nts) {
        this.name = nts.name;
        this.definition = nts.definition;
        this.members = nts.members;
        this.typeParameters= nts.typeParameters;
        this.baseTypes = nts.baseTypes;
        this.isAbstract = nts.isAbstract;
    }

    public void setDefinition(SyntaxNode definition) {
        this.definition = definition;
        this.isAbstract = determineAbstract();
    }

    public void addBaseType(TypeSymbol baseType) {
        baseTypes.add(baseType);
    }

    public void addMember(MemberSymbol member) {

        members.add(member);

        if (member instanceof FunctionSymbol function && function.isAbstract()) {
            isAbstract = true;
        }
    }

    public void addTypeParameters(List<TypeParameterSymbol> typeParameters) {
        this.typeParameters = (typeParameters);
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
        return this;
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
}