package syntax.node.symbol;


import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.MemberSymbol;
import syspro.tm.symbols.SymbolKind;
import syspro.tm.symbols.TypeSymbol;
import syspro.tm.symbols.TypeLikeSymbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltInTypes {
    private final Map<String, TypeSymbol> types = new HashMap<>();

    public BuiltInTypes() {
        addType("Int32");
        addType("Int64");
        addType("UInt32");
        addType("UInt64");
        addType("Boolean");
        addType("Rune");
    }

    private void addType(String name) {
        types.put(name, new BuiltInTypeSymbol(name));
    }

    public TypeSymbol getType(String name) {
        return types.get(name);
    }

    public static class BuiltInTypeSymbol implements TypeSymbol {
        private final String name;

        public BuiltInTypeSymbol(String name) {
            this.name = name;
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
        public List<? extends TypeLikeSymbol> typeArguments() {
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
            return null;
        }
    }
}
