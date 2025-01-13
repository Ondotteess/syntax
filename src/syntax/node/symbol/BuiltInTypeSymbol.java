package syntax.node.symbol;

import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.MemberSymbol;
import syspro.tm.symbols.SymbolKind;
import syspro.tm.symbols.TypeLikeSymbol;
import syspro.tm.symbols.TypeSymbol;

import java.util.List;

public class BuiltInTypeSymbol implements TypeSymbol {
    private final String name;

    public BuiltInTypeSymbol(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public SyntaxNode definition() {
        return null;
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.CLASS;
    }

    @Override
    public List<? extends TypeLikeSymbol> typeArguments() {
        return List.of();
    }

    @Override
    public List<? extends MemberSymbol> members() {
        return List.of();
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public List<? extends TypeSymbol> baseTypes() {
        return List.of();
    }

    @Override
    public TypeSymbol originalDefinition() {
        return this;
    }

    @Override
    public TypeSymbol construct(List<? extends TypeLikeSymbol> list) {
        return null;
    }

}

