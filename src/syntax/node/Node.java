package syntax.node;

import java.util.ArrayList;
import java.util.List;

import syntax.code.Code;
import syspro.tm.lexer.Token;
import syspro.tm.parser.*;

public class Node implements SyntaxNode {
    private final AnySyntaxKind kind;
    private final Token token;
    private final List<SyntaxNode> children;
    private final List<Diagnostic> diagnostics;

    public Node(SyntaxKind kind) {
        this(kind, null);
    }

    public Node(AnySyntaxKind kind, Token token) {
        this.kind = kind;
        this.token = token;
        this.children = new ArrayList<>();
        this.diagnostics = new ArrayList<>();
    }

    public void addChild(SyntaxNode child) {
        children.add(child);
    }

    public void addInvalidRange(TextSpan span, String message) {
        Diagnostic diagnostic = new Diagnostic(
                new DiagnosticInfo(Code.SYNTAX, new Object[]{message}),
                span,
                List.of()
        );
        diagnostics.add(diagnostic);
    }

    @Override
    public AnySyntaxKind kind() {
        return kind;
    }

    @Override
    public int slotCount() {
        return children.size();
    }

    @Override
    public SyntaxNode slot(int index) {
        if (index < 0 || index >= children.size()) {
            return null;
        }
        return children.get(index);
    }

    @Override
    public Token token() {
        return token;
    }

    public List<Diagnostic> collectDiagnostics() {
        List<Diagnostic> allDiagnostics = new ArrayList<>(diagnostics);
        for (SyntaxNode child : children) {
            if (child instanceof Node) {
                allDiagnostics.addAll(((Node) child).collectDiagnostics());
            }
        }
        return allDiagnostics;
    }
}
