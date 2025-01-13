package syntax.node.symbol;


import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.SemanticSymbol;
import syspro.tm.symbols.SymbolKind;
import syspro.tm.symbols.TypeLikeSymbol;
import syspro.tm.symbols.TypeParameterSymbol;

import java.util.List;

public class NodeTypeParameterSymbol implements TypeParameterSymbol {
    private final String name;
    public SemanticSymbol owner;
    private final SyntaxNode definition;
    private final List<TypeLikeSymbol> bounds;

    public NodeTypeParameterSymbol(String name, SemanticSymbol owner, SyntaxNode definition, List<TypeLikeSymbol> bounds) {
        this.name = name;
        this.owner = owner;
        this.definition = definition;
        this.bounds = bounds;
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.TYPE_PARAMETER;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public SyntaxNode definition() {
        return definition;
    }

    @Override
    public SemanticSymbol owner() {
        return owner;
    }

    @Override
    public List<? extends TypeLikeSymbol> bounds() {
        return bounds;
    }
}


