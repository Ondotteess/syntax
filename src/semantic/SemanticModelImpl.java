package semantic;

import syntax.node.Node;
import syspro.tm.parser.Diagnostic;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.parser.TextSpan;
import syspro.tm.symbols.SemanticModel;
import syspro.tm.symbols.TypeSymbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class SemanticModelImpl implements SemanticModel {
    private final SyntaxNode rootNode;
    private final Collection<TextSpan> invalidRanges;
    private final List<Diagnostic> diagnostics;
    private final Map<String, TypeSymbol> typeMap;
    private final List<TypeSymbol> typeSymbols;

    public SemanticModelImpl(SyntaxNode rootNode, Collection<TextSpan> invalidRanges,
                             List<Diagnostic> diagnostics, Map<String, TypeSymbol> typeMap) {
        this.rootNode = rootNode;
        this.invalidRanges = invalidRanges;
        this.diagnostics = diagnostics;
        this.typeMap = typeMap;
        this.typeSymbols = new ArrayList<>(typeMap.values());
    }

    @Override
    public SyntaxNode root() {
        return rootNode;
    }

    @Override
    public Collection<TextSpan> invalidRanges() {
        return invalidRanges;
    }

    @Override
    public Collection<Diagnostic> diagnostics() {
        return diagnostics;
    }

    @Override
    public List<? extends TypeSymbol> typeDefinitions() {
        ArrayList<TypeSymbolGen> definitions = new ArrayList<>();
        for (SyntaxNode def: ((Node) root().slot(0)).children) {
            String name = def.slot(1).token().toString();
            //definitions.add(new TypeSymbolGen(name, def));
        }
        return definitions;
    }

    @Override
    public TypeSymbol lookupType(String name) {
        return typeMap.get(name);
    }
}