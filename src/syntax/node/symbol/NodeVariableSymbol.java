package syntax.node.symbol;

import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.SemanticSymbol;
import syspro.tm.symbols.SymbolKind;
import syspro.tm.symbols.TypeLikeSymbol;
import syspro.tm.symbols.VariableSymbol;

public class NodeVariableSymbol implements VariableSymbol {
    private final String name;
    private final TypeLikeSymbol type;
    public SemanticSymbol owner;
    public SymbolKind kind;
    private final SyntaxNode definition;

    public NodeVariableSymbol(String name, TypeLikeSymbol type, SemanticSymbol owner, SymbolKind kind, SyntaxNode definition) {
        this.name = name;
        this.type = type;
        this.owner = owner;
        this.kind = kind;
        this.definition = definition;
    }

    @Override
    public TypeLikeSymbol type() {
        return type;
    }

    @Override
    public SymbolKind kind() {
        return kind;
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
}