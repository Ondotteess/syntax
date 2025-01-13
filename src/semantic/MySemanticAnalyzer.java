package semantic;

import semantic.processor.SymbolProcessor;
import semantic.processor.SymbolProcessorFactory;
import syntax.MyParser;
import syntax.node.Node;
import syntax.node.symbol.NodeFunctionSymbol;
import syntax.node.symbol.NodeTypeParameterSymbol;
import syntax.node.symbol.NodeTypeSymbol;
import syntax.node.symbol.NodeVariableSymbol;

import syspro.tm.lexer.Symbol;
import syspro.tm.parser.*;
import syspro.tm.symbols.*;

import java.util.*;

public class MySemanticAnalyzer implements LanguageServer {

    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private final Map<String, TypeSymbol> typeMap = new HashMap<>();

    @Override
    public SemanticModel buildModel(String sourceCode) {
        ParseResult result = new MyParser().parse(sourceCode);
        Node rootNode = (Node) result.root();

        //processNode(rootNode);

        return new SemanticModelImpl(result.root(), result.invalidRanges(), diagnostics, typeMap);
    }

    //private void processNode(Node source) {
        //    for (SyntaxNode typeDef: ((Node)source.slot(0)).children) {
            //
            //        SymbolProcessor processor = SymbolProcessorFactory.getProcessor((Node)typeDef);
            //        if (processor != null) {
                //            ((Node) typeDef).symbol = processor.processSymbol((Node)typeDef);
                //        }
            //    }
        //    //
        //
        //
        //}
}

