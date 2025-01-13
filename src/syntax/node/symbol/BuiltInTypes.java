package syntax.node.symbol;


import syspro.tm.parser.SyntaxNode;
import syspro.tm.symbols.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltInTypes {
    private final Map<String, TypeSymbol> builtInTypeMap = new HashMap<>();

    public BuiltInTypes() {
        builtInTypeMap.put("Int32", createBuiltInType("Int32"));
        builtInTypeMap.put("Int64", createBuiltInType("Int64"));
        builtInTypeMap.put("UInt32", createBuiltInType("UInt32"));
        builtInTypeMap.put("UInt64", createBuiltInType("UInt64"));
        builtInTypeMap.put("Boolean", createBuiltInType("Boolean"));
        builtInTypeMap.put("Rune", createBuiltInType("Rune"));
    }

    public TypeSymbol get(String name) {
        if (builtInTypeMap.containsKey(name)) {
            return createBuiltInType(name);
        } else {
            return null;
        }
    }

    private TypeSymbol createBuiltInType(String name) {
        return new TypeSymbol() {
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
            public TypeSymbol construct(List<? extends TypeLikeSymbol> typeArguments) {
                return this;
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
        };
    }
}
