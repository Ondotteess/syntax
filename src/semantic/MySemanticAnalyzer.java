package semantic;

import syntax.MyParser;
import syntax.node.Node;
import syspro.tm.lexer.Symbol;
import syspro.tm.parser.*;
import syspro.tm.symbols.*;

import java.util.*;


public class MySemanticAnalyzer implements LanguageServer {

    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private final Map<String, TypeSymbol> typeMap = new HashMap<>();
    private final Map<String, Symbol> symbolTable = new HashMap<>();

    @Override
    public SemanticModel buildModel(String sourceCode) {
        MyParser parser = new MyParser();
        ParseResult result = parser.parse(sourceCode);

        analyzeSyntaxTree((Node) result.root());

        return new SemanticModelImpl(result.root(), result.invalidRanges(), diagnostics, typeMap);
    }

    private void analyzeSyntaxTree(Node node) {
        Node types = (Node) node.slot(0);

        for (SyntaxNode child : types.getChildren()) {
            if (child.kind() == SyntaxKind.TYPE_DEFINITION) {
                analyzeTypeDefinition((Node) child, typeMap, symbolTable);
            }
        }
    }

    private void analyzeTypeDefinition(Node typeNode, Map<String, TypeSymbol> typeMap, Map<String, Symbol> symbolTable) {
        Node nameNode = (Node) typeNode.getChildren().get(1);
        String typeName = nameNode.token().toString();

        if (typeMap.containsKey(typeName)) {
            // diagnostics.add();
            return;
        }

        boolean isAbstract = false;

        List<TypeSymbol> baseTypes = extractBaseTypes(typeNode);

        List<TypeLikeSymbol> typeParameters = extractTypeParameters(typeNode);

        TypeSymbol typeSymbol = new TypeSymbolGen(typeName, isAbstract, baseTypes, typeParameters, typeNode);

        typeMap.put(typeName, typeSymbol);
    }


    private List<TypeSymbol> extractBaseTypes(Node typeNode) {
        SyntaxNode typeBoundNode = typeNode.slot(5);

        if (typeBoundNode == null) {
            return List.of();
        }

        List<TypeSymbol> baseTypes = new ArrayList<>();
        Node a = (Node) typeBoundNode.slot(0);
        for (SyntaxNode baseType :a.getChildren()) {
            String baseTypeName = baseType.token().toString();
            baseTypes.add(new TypeSymbolGen(baseTypeName, false, List.of(), List.of(), baseType));
        }
        return baseTypes;
    }

    private List<TypeLikeSymbol> extractTypeParameters(Node typeNode) {
        SyntaxNode typeParamNode = typeNode.slot(3);

        if (typeParamNode == null) {
            return List.of();
        }

        List<TypeLikeSymbol> parameters = new ArrayList<>();
        Node params = (Node) typeParamNode.slot(0);
        for (SyntaxNode param : params.getChildren()) {
            parameters.add(new TypeParameterSymbol() {
                @Override
                public SymbolKind kind() {
                    return SymbolKind.TYPE_PARAMETER;
                }

                @Override
                public String name() {
                    return param.token().toString();
                }

                @Override
                public SyntaxNode definition() {
                    return param;
                }

                @Override
                public SemanticSymbol owner() {
                    return null;
                }

                @Override
                public List<? extends TypeLikeSymbol> bounds() {
                    return List.of();
                }
            });
        }
        return parameters;
    }



}
