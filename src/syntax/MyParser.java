package syntax;

import lexer.MyLexer;
import syntax.node.Node;
import syntax.rule.SourceText;
import syspro.tm.lexer.Lexer;
import syspro.tm.lexer.Token;
import syspro.tm.parser.Diagnostic;
import syspro.tm.parser.ParseResult;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.parser.TextSpan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyParser implements syspro.tm.parser.Parser {

    private List<Token> tokens;
    private int position = 0;
    private Context util;

    public MyParser(){

    }

    public static void printTree(SyntaxNode node, String indent) {
        if (node == null) {
            return;
        }

        System.out.println(indent + node.kind());

        for (int i = 0; i < node.slotCount(); i++) {
            SyntaxNode child = node.slot(i);
            if (child != null) {
                printTree(child, indent + "    ");
            }
        }
    }

    public static void printTree(SyntaxNode root) {
        printTree(root, "");
    }

    @Override
    public ParseResult parse(String s) {

        System.out.println(s);

        Lexer lexer = new MyLexer();
        this.tokens = lexer.lex(s);
        this.util = new Context(this.tokens);

        SyntaxNode root = SourceText.parse(util);

        //System.out.println(tokens);

        List<Diagnostic> diagnostics = new ArrayList<>();
        if (root instanceof Node) {
            diagnostics = ((Node) root).collectDiagnostics();
        }

        List<Diagnostic> finalDiagnostics = diagnostics;
        return new ParseResult() {
            @Override
            public SyntaxNode root() {
                return root;
            }

            @Override
            public Collection<TextSpan> invalidRanges() {
                return finalDiagnostics.stream().map(Diagnostic::location).toList();
            }

            @Override
            public Collection<Diagnostic> diagnostics() {
                return finalDiagnostics;
            }
        };
    }


}
