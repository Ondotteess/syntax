package semantic;

import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.MemberSymbol;
import syspro.tm.symbols.SymbolKind;
import syspro.tm.symbols.TypeLikeSymbol;
import syspro.tm.symbols.TypeSymbol;

import java.util.List;

public class TypeSymbolGen implements TypeSymbol {
    private final String name;
    private final boolean isAbstract;
    private final List<TypeSymbol> baseTypes;
    private final List<TypeLikeSymbol> typeParameters;
    private final SyntaxNode definition;

    public TypeSymbolGen(String name, boolean isAbstract, List<TypeSymbol> baseTypes,
                         List<TypeLikeSymbol> typeParameters, SyntaxNode definition) {
        this.name = name;
        this.isAbstract = isAbstract;
        this.baseTypes = baseTypes;
        this.typeParameters = typeParameters;
        this.definition = definition;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public List<? extends TypeSymbol> baseTypes() {
        return baseTypes != null ? baseTypes : List.of();
    }

    @Override
    public List<? extends TypeLikeSymbol> typeArguments() {
        return typeParameters != null ? typeParameters : List.of();
    }

    @Override
    public TypeSymbol originalDefinition() {
        return this;
    }

    @Override
    public TypeSymbol construct(List<? extends TypeLikeSymbol> arguments) {
        return this;
    }

    @Override
    public List<? extends MemberSymbol> members() {
        return List.of();
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.CLASS;
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