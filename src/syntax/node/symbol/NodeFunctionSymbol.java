package syntax.node.symbol;

import syspro.tm.lexer.Keyword;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.*;

import java.util.List;

public class NodeFunctionSymbol implements FunctionSymbol {
    private final String name;
    private final TypeLikeSymbol returnType;
    private List<VariableSymbol> parameters;
    private final SemanticSymbol owner;
    private final SyntaxNode node;

    private final boolean isAbstract;
    private final boolean isVirtual;
    private final boolean isOverride;
    private final boolean isNative;

    public NodeFunctionSymbol(String name, TypeLikeSymbol returnType, List<VariableSymbol> parameters, SemanticSymbol owner, SyntaxNode node) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.owner = owner;
        this.node = node;

        this.isAbstract = checkIsAbstract();
        this.isVirtual = checkIsVirtual();
        this.isOverride = checkIsOverride();
        this.isNative = checkIsNative();
    }

    private boolean checkIsAbstract() {
        if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
            for (SyntaxNode modifier : node.descendants(false).get(0).descendants(false)) {
                if (modifier.token() != null && modifier.token().toString().equals("abstract")) {
                    return true;
                }
            }
        }
        return owner != null && owner.kind() == SymbolKind.INTERFACE;
    }

    private boolean checkIsVirtual() {
        if (owner != null && owner.kind() == SymbolKind.INTERFACE) {
            return true;
        }
        return false;
    }

    private boolean checkIsOverride() {
        for (SyntaxNode modifier : node.descendants(false).get(0).descendants(false)) {
            if (modifier.token() != null && modifier.token().toString().equals("override")) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIsNative() {
        for (SyntaxNode modifier : node.descendants(false).get(0).descendants(false)) {
            if (modifier.token() != null && modifier.token().toString().equals("native")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isVirtual() {
        return isVirtual;
    }

    @Override
    public boolean isOverride() {
        return isOverride;
    }

    @Override
    public boolean isNative() {
        return isNative;
    }
    @Override
    public List<? extends VariableSymbol> parameters() {
        return parameters;
    }

    @Override
    public TypeLikeSymbol returnType() {
        return returnType;
    }

    @Override
    public List<? extends VariableSymbol> locals() {
        return List.of();
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.FUNCTION;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public SyntaxNode definition() {
        return node;
    }

    @Override
    public SemanticSymbol owner() {
        return owner;
    }

    public void setParameters(List<VariableSymbol> parameters) {
        this.parameters = parameters;
    }
}