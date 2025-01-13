package semantic.processor;

import syspro.tm.parser.SyntaxKind;
import syntax.node.Node;

public class SymbolProcessorFactory {

    public static SymbolProcessor getProcessor(Node node) {
        SyntaxKind kind = (SyntaxKind) node.kind();

        return switch (kind) {
            case TYPE_DEFINITION -> new TypeSymbolProcessor();
            case FUNCTION_DEFINITION -> new FunctionSymbolProcessor();
            case VARIABLE_DEFINITION -> new VariableSymbolProcessor();
            default -> throw new IllegalArgumentException("Unsupported syntax kind: " + kind);
        };
    }
}